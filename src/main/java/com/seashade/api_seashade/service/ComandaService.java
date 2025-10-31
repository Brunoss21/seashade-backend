package com.seashade.api_seashade.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.seashade.api_seashade.model.Atendente;
import com.seashade.api_seashade.model.Comanda;
import com.seashade.api_seashade.model.GuardaSol;
import com.seashade.api_seashade.model.ItemPedido;
import com.seashade.api_seashade.model.Produto;
import com.seashade.api_seashade.model.Quiosque;
import com.seashade.api_seashade.model.User;
import com.seashade.api_seashade.repository.AtendenteRepository;
import com.seashade.api_seashade.repository.ComandaRepository;
import com.seashade.api_seashade.repository.GuardaSolRepository;
import com.seashade.api_seashade.repository.ItemPedidoRepository;
import com.seashade.api_seashade.repository.ProdutoRepository;
import com.seashade.api_seashade.repository.QuiosqueRepository;
import com.seashade.api_seashade.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional; 


@Service
public class ComandaService {

    private final ComandaRepository comandaRepository;
    private final GuardaSolRepository guardaSolRepository;
    private final AtendenteRepository atendenteRepository;
    private final QuiosqueRepository quiosqueRepository; 
    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final UserRepository userRepository;

    public ComandaService(ComandaRepository comandaRepository,
                          GuardaSolRepository guardaSolRepository,
                          AtendenteRepository atendenteRepository,
                          QuiosqueRepository quiosqueRepository,
                          UserRepository userRepository,
                          ProdutoRepository produtoRepository,
                          ItemPedidoRepository itemPedidoRepository) { 
        this.comandaRepository = comandaRepository;
        this.guardaSolRepository = guardaSolRepository;
        this.atendenteRepository = atendenteRepository;
        this.quiosqueRepository = quiosqueRepository; 
        this.userRepository = userRepository; 
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Transactional
    public Comanda abrirComanda(Long guardaSolId, String authenticatedPrincipalId, String scope) {
        GuardaSol guardaSol = guardaSolRepository.findById(guardaSolId)
                .orElseThrow(() -> new EntityNotFoundException("Guarda-Sol não encontrado com ID: " + guardaSolId));

        if (guardaSol.getStatus() == GuardaSol.StatusGuardaSol.OCUPADO) {
            Optional<Comanda> comandaExistente = comandaRepository.findByGuardaSolAndStatus(guardaSol, Comanda.StatusComanda.ABERTA);
            if(comandaExistente.isPresent()){
                return comandaExistente.get();
            } else {
                throw new IllegalStateException("Guarda-Sol #" + guardaSolId + " já está ocupado, mas não foi encontrada comanda aberta associada.");
            }
        }

        Atendente atendente = null;

        if (scope.contains("ATENDENTE")) {
            Long atendenteId = Long.parseLong(authenticatedPrincipalId); 
            atendente = atendenteRepository.findById(atendenteId)
                            .orElseThrow(() -> new EntityNotFoundException("Atendente não encontrado com ID: " + atendenteId));
        } else if (scope.contains("BASIC") || scope.contains("ADMIN")) {
            UUID userId = UUID.fromString(authenticatedPrincipalId);
            User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("Usuário (Dono/Admin) não encontrado com ID: " + userId));
        } else {
            throw new IllegalStateException("Tipo de usuário não reconhecido para abrir comanda.");
        }

        Quiosque quiosque = guardaSol.getQuiosque();

        Comanda novaComanda = new Comanda();
        novaComanda.setGuardaSol(guardaSol);
        novaComanda.setQuiosque(quiosque);
        novaComanda.setAtendente(atendente); 
        novaComanda.setDataAbertura(LocalDateTime.now());
        novaComanda.setStatus(Comanda.StatusComanda.ABERTA);
        novaComanda.setNumeroComanda(String.format("%04d", comandaRepository.countByQuiosque(quiosque) + 1));

        Comanda comandaSalva = comandaRepository.save(novaComanda);

        guardaSol.setStatus(GuardaSol.StatusGuardaSol.OCUPADO);
        guardaSolRepository.save(guardaSol);

        return comandaSalva;
    }

    @Transactional
    public ItemPedido adicionarItem(Long comandaId, Long produtoId, Integer quantidade) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada com ID: " + comandaId));

        // --- MUDANÇA: Permitir adicionar item apenas se ABERTA ou PRONTO_PARA_ENTREGA ---
        if (comanda.getStatus() != Comanda.StatusComanda.ABERTA && comanda.getStatus() != Comanda.StatusComanda.PRONTO_PARA_ENTREGA) {
            throw new IllegalStateException("Só é possível adicionar itens a comandas ABERTAS ou PRONTAS PARA ENTREGA. Status atual: " + comanda.getStatus());
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + produtoId));

        ItemPedido novoItem = new ItemPedido();
        novoItem.setComanda(comanda);
        novoItem.setProduto(produto);
        novoItem.setQuantidade(quantidade);
        novoItem.setPrecoUnitario(produto.getPreco());

        ItemPedido itemSalvo = itemPedidoRepository.save(novoItem);

        comanda.getItens().add(itemSalvo);

        if (comanda.getStatus() == Comanda.StatusComanda.PRONTO_PARA_ENTREGA) {
            comanda.setStatus(Comanda.StatusComanda.ABERTA);
        }

        // Recalcula total
        BigDecimal novoTotal = comanda.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        comanda.setValorTotal(novoTotal);
        comandaRepository.save(comanda); // Salva a comanda com o novo status (se mudou) e novo total

        return itemSalvo;
    }


    @Transactional
    public Comanda enviarParaCozinha(Long comandaId) {
        Comanda comanda = comandaRepository.findById(comandaId)
            .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada: " + comandaId));

        // Só pode enviar se estiver ABERTA e tiver itens
        if (comanda.getStatus() != Comanda.StatusComanda.ABERTA) {
            throw new IllegalStateException("Só é possível enviar comandas ABERTAS para a cozinha. Status atual: " + comanda.getStatus());
        }
        if (comanda.getItens() == null || comanda.getItens().isEmpty()) {
            throw new IllegalStateException("Não é possível enviar uma comanda vazia para a cozinha.");
        }

        comanda.setStatus(Comanda.StatusComanda.NA_COZINHA); // Muda para NA_COZINHA
        return comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda marcarComandaEmPreparo(Long comandaId) {
        Comanda comanda = comandaRepository.findById(comandaId)
            .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada: " + comandaId));

        if (comanda.getStatus() != Comanda.StatusComanda.NA_COZINHA) {
             throw new IllegalStateException("Só é possível iniciar o preparo de comandas que estão 'NA_COZINHA'. Status atual: " + comanda.getStatus());
        }
        comanda.setStatus(Comanda.StatusComanda.EM_PREPARO); // Muda para EM_PREPARO
        return comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda marcarComandaPronta(Long comandaId) {
        Comanda comanda = comandaRepository.findById(comandaId)
            .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada: " + comandaId));

        // Permite marcar como pronta se estiver EM_PREPARO
        if (comanda.getStatus() != Comanda.StatusComanda.EM_PREPARO) {
             throw new IllegalStateException("Só é possível marcar como pronta comandas que estão 'EM_PREPARO'. Status atual: " + comanda.getStatus());
        }
        comanda.setStatus(Comanda.StatusComanda.PRONTO_PARA_ENTREGA); // Muda para PRONTO_PARA_ENTREGA
        return comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda finalizarComanda(Long id) {
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada com id: " + id));

        // Permite finalizar se ABERTA ou PRONTO_PARA_ENTREGA
        if (comanda.getStatus() != Comanda.StatusComanda.ABERTA && comanda.getStatus() != Comanda.StatusComanda.PRONTO_PARA_ENTREGA) {
            throw new IllegalStateException("Apenas comandas ABERTAS ou PRONTAS PARA ENTREGA podem ser finalizadas. Status atual: " + comanda.getStatus());
        }

        comanda.setStatus(Comanda.StatusComanda.FECHADA);
        comanda.setDataFechamento(LocalDateTime.now());

        GuardaSol guardaSol = comanda.getGuardaSol();
        if (guardaSol != null && guardaSol.getStatus() == GuardaSol.StatusGuardaSol.OCUPADO) {
            guardaSol.setStatus(GuardaSol.StatusGuardaSol.LIVRE);
            guardaSolRepository.save(guardaSol);
        }

        return comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda cancelarComanda(Long id) {
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada com id: " + id));

        // Não pode cancelar se já estiver FECHADA
        if (comanda.getStatus() == Comanda.StatusComanda.FECHADA) {
            throw new IllegalStateException("Comandas FECHADAS não podem ser canceladas.");
        }

        // Se já foi cancelada, não faz nada (ou lança exceção, opcional)
        if (comanda.getStatus() == Comanda.StatusComanda.CANCELADA) {
            return comanda; // Já está cancelada
        }

        comanda.setStatus(Comanda.StatusComanda.CANCELADA);
        if (comanda.getDataFechamento() == null) {
            comanda.setDataFechamento(LocalDateTime.now());
        }
        /*
        GuardaSol guardaSol = comanda.getGuardaSol();
        // Libera o guarda-sol apenas se a comanda ainda o estava ocupando
        // (pode já ter sido liberado por outra lógica, embora improvável aqui)
        if (guardaSol != null && guardaSol.getStatus() == GuardaSol.StatusGuardaSol.OCUPADO) {
             // Verificação extra (opcional): checar se *esta* comanda era a última ABERTA/NA_COZINHA/EM_PREPARO/PRONTA para este guarda-sol antes de liberar
             // boolean isLastActiveComandaForGuardaSol = !comandaRepository.existsByGuardaSolAndStatusIn(guardaSol,
             //        List.of(StatusComanda.ABERTA, StatusComanda.NA_COZINHA, StatusComanda.EM_PREPARO, StatusComanda.PRONTO_PARA_ENTREGA));
             // if(isLastActiveComandaForGuardaSol) { // Descomente se implementar a verificação acima e o método no repo
                 guardaSol.setStatus(GuardaSol.StatusGuardaSol.LIVRE);
                 guardaSolRepository.save(guardaSol);
             // }
        }
        */
        return comandaRepository.save(comanda);
    }

    // ... (buscarComandaPorId e listarComandasPorQuiosque permanecem iguais) ...
    public Comanda buscarComandaPorId(Long comandaId) {
        Comanda comanda = comandaRepository.findByIdWithItensAndProdutos(comandaId) 
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada com ID: " + comandaId));
        return comanda;
    }

    public List<Comanda> listarComandasPorQuiosque(Long quiosqueId, List<Comanda.StatusComanda> statuses) { 
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado com ID: " + quiosqueId));

        List<Comanda> comandas;

        if (statuses != null && !statuses.isEmpty()) {
             comandas = comandaRepository.findByQuiosqueAndStatusInOrderByDataAberturaDesc(quiosque, statuses);
        } else {
             comandas = comandaRepository.findByQuiosqueOrderByDataAberturaDesc(quiosque);
        }

        comandas.forEach(comanda -> {
            comanda.getItens().size();
            if (comanda.getGuardaSol() != null) comanda.getGuardaSol().getIdentificacao(); // ou .getNumero()
        });

        return comandas;
    }

}

