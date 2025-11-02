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
import com.seashade.api_seashade.model.StatusItem;
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
        // MODIFICADO: Busca comanda com itens para evitar LazyException
        Comanda comanda = comandaRepository.findByIdWithItensAndProdutos(comandaId)
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada com ID: " + comandaId));

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
        novoItem.setStatus(StatusItem.PENDENTE); 

        novoItem.setStatus(StatusItem.PENDENTE);
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
        comandaRepository.save(comanda);

        return itemSalvo;
    }


    @Transactional
    public Comanda enviarParaCozinha(Long comandaId) {
        // Busca comanda com itens para evitar LazyException
        Comanda comanda = comandaRepository.findByIdWithItensAndProdutos(comandaId)
            .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada: " + comandaId));

        if (comanda.getStatus() != Comanda.StatusComanda.ABERTA) {
            throw new IllegalStateException("Só é possível enviar comandas ABERTAS para a cozinha. Status atual: " + comanda.getStatus());
        }

        // Encontra todos os itens PENDENTES
        List<ItemPedido> itensParaEnviar = comanda.getItens().stream()
            .filter(item -> item.getStatus() == StatusItem.PENDENTE)
            .toList();

        if (itensParaEnviar.isEmpty()) {
            throw new IllegalStateException("Não há novos itens PENDENTES para enviar à cozinha.");
        }

        // Atualiza o status de cada item
        for (ItemPedido item : itensParaEnviar) {
            item.setStatus(StatusItem.NA_COZINHA);
        }
        itemPedidoRepository.saveAll(itensParaEnviar); // Salva todos os itens

        comanda.setStatus(Comanda.StatusComanda.NA_COZINHA); // Muda status da comanda
        return comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda marcarComandaEmPreparo(Long comandaId) {
        // Busca comanda com itens para evitar LazyException
        Comanda comanda = comandaRepository.findByIdWithItensAndProdutos(comandaId)
            .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada: " + comandaId));

        if (comanda.getStatus() != Comanda.StatusComanda.NA_COZINHA) {
             throw new IllegalStateException("Só é possível iniciar o preparo de comandas que estão 'NA_COZINHA'. Status atual: " + comanda.getStatus());
        }
        comanda.setStatus(Comanda.StatusComanda.EM_PREPARO); // Muda status da comanda
        
        List<ItemPedido> itensParaPreparo = comanda.getItens().stream()
            .filter(item -> item.getStatus() == StatusItem.NA_COZINHA)
            .toList();

        for (ItemPedido item : itensParaPreparo) {
            item.setStatus(StatusItem.EM_PREPARO);
        }
        itemPedidoRepository.saveAll(itensParaPreparo);

        return comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda marcarComandaPronta(Long comandaId) {
        // Busca comanda com itens para evitar LazyException
        Comanda comanda = comandaRepository.findByIdWithItensAndProdutos(comandaId)
            .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada: " + comandaId));

        if (comanda.getStatus() != Comanda.StatusComanda.EM_PREPARO) {
             throw new IllegalStateException("Só é possível marcar como pronta comandas que estão 'EM_PREPARO'. Status atual: " + comanda.getStatus());
        }
        comanda.setStatus(Comanda.StatusComanda.PRONTO_PARA_ENTREGA); // Muda status da comanda
        
        // Atualiza status dos itens
        List<ItemPedido> itensProntos = comanda.getItens().stream()
            .filter(item -> item.getStatus() == StatusItem.EM_PREPARO)
            .toList();

        for (ItemPedido item : itensProntos) {
            item.setStatus(StatusItem.PRONTO);
        }
        itemPedidoRepository.saveAll(itensProntos);

        return comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda finalizarComanda(Long id) {
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada com id: " + id));

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

        // Se já foi cancelada, não faz nada
        if (comanda.getStatus() == Comanda.StatusComanda.CANCELADA) {
            return comanda; // Já está cancelada
        }

        comanda.setStatus(Comanda.StatusComanda.CANCELADA);
        if (comanda.getDataFechamento() == null) {
            comanda.setDataFechamento(LocalDateTime.now());
        }

        // --- INÍCIO DA LÓGICA GUARDA-SOL ---
        GuardaSol guardaSol = comanda.getGuardaSol();
        if (guardaSol != null && guardaSol.getStatus() == GuardaSol.StatusGuardaSol.OCUPADO) {
            
            // Lista de status que mantêm o guarda-sol ocupado
            List<Comanda.StatusComanda> statusAtivos = List.of(
                Comanda.StatusComanda.ABERTA,
                Comanda.StatusComanda.NA_COZINHA,
                Comanda.StatusComanda.EM_PREPARO,
                Comanda.StatusComanda.PRONTO_PARA_ENTREGA
            );

            // Verifica se existe ALGUMA OUTRA comanda ativa para este guarda-sol
            boolean outrasComandasAtivas = comandaRepository.existsByGuardaSolAndStatusInAndIdNot(
                guardaSol, 
                statusAtivos, 
                comanda.getId() 
            );

            if (!outrasComandasAtivas) {
                // Nenhuma outra comanda ativa foi encontrada. PODE liberar o guarda-sol.
                guardaSol.setStatus(GuardaSol.StatusGuardaSol.LIVRE);
                guardaSolRepository.save(guardaSol);
            }
        
        }

        return comandaRepository.save(comanda);
    }


    // --- MARCA ITEM COMO ENTREGUE ---
    @Transactional
    public ItemPedido marcarItemEntregue(Long itemId) {
        ItemPedido item = itemPedidoRepository.findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException("Item não encontrado: " + itemId));
        
        if (item.getStatus() != StatusItem.PRONTO) {
             throw new IllegalStateException("Só é possível marcar como 'ENTREGUE' itens que estão 'PRONTO'. Status atual: " + item.getStatus());
        }
        item.setStatus(StatusItem.ENTREGUE);
        return itemPedidoRepository.save(item);
    }
  
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
            if (comanda.getGuardaSol() != null) comanda.getGuardaSol().getIdentificacao(); 
        });

        return comandas;
    }

}