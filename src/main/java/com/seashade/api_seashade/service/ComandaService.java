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
    private final QuiosqueRepository quiosqueRepository; // <-- ERRO 2 CORRIGIDO: Campo adicionado
    private final UserRepository userRepository; 
    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

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
        this.quiosqueRepository = quiosqueRepository; // <-- Agora isso funciona
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

        if (comanda.getStatus() != Comanda.StatusComanda.ABERTA) {
            throw new IllegalStateException("Só é possível adicionar itens a comandas ABERTAS.");
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
        BigDecimal novoTotal = comanda.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        comanda.setValorTotal(novoTotal);
        comandaRepository.save(comanda);

        return itemSalvo;
    }

    public Comanda buscarComandaPorId(Long comandaId) {
        Comanda comanda = comandaRepository.findById(comandaId)
            .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada com ID: " + comandaId));
        comanda.getItens().size(); 

        // --- MUDANÇA PRINCIPAL ---
        // 3. Itera sobre os itens e ACESSA o produto de cada um.
        // Isso força o Hibernate a carregar o Produto (que é LAZY)
        // ANTES de sair do método (e fechar a sessão).
        for (ItemPedido item : comanda.getItens()) {
        item.getProduto().getNome(); // Esta linha força o "lazy loading"
        }
        // --- FIM DA MUDANÇA ---
        return comanda;
    }
 
    public List<Comanda> listarComandasPorQuiosque(Long quiosqueId, Comanda.StatusComanda status) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado com ID: " + quiosqueId));

        List<Comanda> comandas; 

        if (status != null) {
            comandas = comandaRepository.findByQuiosqueAndStatusOrderByDataAberturaDesc(quiosque, status);
        } else {
            comandas = comandaRepository.findByQuiosqueOrderByDataAberturaDesc(quiosque);
        }
        
        comandas.forEach(comanda -> {
            comanda.getItens().size(); 
            if (comanda.getGuardaSol() != null) comanda.getGuardaSol().getIdentificacao(); 
        });
        
        return comandas; 
    }

    @Transactional 
    public Comanda finalizarComanda(Long id) {
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada com id: " + id));

        if (comanda.getStatus() != Comanda.StatusComanda.ABERTA) {
            throw new IllegalStateException("Apenas comandas ABERTAS podem ser finalizadas.");
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

        if (comanda.getStatus() == Comanda.StatusComanda.FECHADA) {
            throw new IllegalStateException("Comandas FECHADAS não podem ser canceladas.");
        }

        comanda.setStatus(Comanda.StatusComanda.CANCELADA);
        if (comanda.getDataFechamento() == null) {
            comanda.setDataFechamento(LocalDateTime.now());
        }

        GuardaSol guardaSol = comanda.getGuardaSol();
        if (guardaSol != null && guardaSol.getStatus() == GuardaSol.StatusGuardaSol.OCUPADO) {
            guardaSol.setStatus(GuardaSol.StatusGuardaSol.LIVRE);
            guardaSolRepository.save(guardaSol);
        }
        
        return comandaRepository.save(comanda);
    } 

} 