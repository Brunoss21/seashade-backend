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
        this.quiosqueRepository = quiosqueRepository;
        this.userRepository = userRepository; 
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    /**
     * Abre uma nova comanda para um guarda-sol específico.
     * Identifica se quem abriu foi um User (Dono) ou Atendente pelo ID.
     */
    @Transactional
    public Comanda abrirComanda(Long guardaSolId, String authenticatedPrincipalId, String scope) {
        // 1. Busca o GuardaSol e verifica se está livre
        GuardaSol guardaSol = guardaSolRepository.findById(guardaSolId)
                .orElseThrow(() -> new EntityNotFoundException("Guarda-Sol não encontrado com ID: " + guardaSolId));

        if (guardaSol.getStatus() == GuardaSol.StatusGuardaSol.OCUPADO) {
            // Verifica se já existe uma comanda ABERTA para este guarda-sol (pode ser útil)
             Optional<Comanda> comandaExistente = comandaRepository.findByGuardaSolAndStatus(guardaSol, Comanda.StatusComanda.ABERTA);
             if(comandaExistente.isPresent()){
                 // Se já existe, apenas retorna a comanda existente
                 return comandaExistente.get();
             } else {
                 // Se não existe comanda aberta mas o guarda-sol está ocupado (inconsistência?)
                 throw new IllegalStateException("Guarda-Sol #" + guardaSolId + " já está ocupado, mas não foi encontrada comanda aberta associada.");
             }
        }

        // 2. Identifica e busca o Atendente ou User
        Atendente atendente = null;
        //User user = null; // Se precisar associar o User também

        // Verifica o scope/role para saber quem está abrindo
        if (scope.contains("ATENDENTE")) {
             Long atendenteId = Long.parseLong(authenticatedPrincipalId); // Assume que o ID do atendente é Long
             atendente = atendenteRepository.findById(atendenteId)
                     .orElseThrow(() -> new EntityNotFoundException("Atendente não encontrado com ID: " + atendenteId));
        } else if (scope.contains("BASIC") || scope.contains("ADMIN")) {
            // Se for Dono/Admin, o ID é UUID
            UUID userId = UUID.fromString(authenticatedPrincipalId);
            User user = userRepository.findById(userId)
                     .orElseThrow(() -> new EntityNotFoundException("Usuário (Dono/Admin) não encontrado com ID: " + userId));
            // Aqui você pode decidir se associa o User à comanda ou busca um Atendente padrão, etc.
            // Por simplicidade, vamos permitir abrir sem atendente se for o dono.
        } else {
             throw new IllegalStateException("Tipo de usuário não reconhecido para abrir comanda.");
        }


        // 3. Pega o Quiosque
        Quiosque quiosque = guardaSol.getQuiosque();

        // 4. Cria a nova Comanda
        Comanda novaComanda = new Comanda();
        novaComanda.setGuardaSol(guardaSol);
        novaComanda.setQuiosque(quiosque);
        novaComanda.setAtendente(atendente); // Pode ser null se o Dono abriu e não associamos um atendente
        novaComanda.setDataAbertura(LocalDateTime.now());
        novaComanda.setStatus(Comanda.StatusComanda.ABERTA);
        // Gera um número sequencial para a comanda (exemplo simples)
        novaComanda.setNumeroComanda(String.format("%04d", comandaRepository.countByQuiosque(quiosque) + 1));


        // 5. Salva a Comanda
        Comanda comandaSalva = comandaRepository.save(novaComanda);

        // 6. Atualiza o status do GuardaSol para OCUPADO
        guardaSol.setStatus(GuardaSol.StatusGuardaSol.OCUPADO);
        guardaSolRepository.save(guardaSol);

        return comandaSalva;
    }

        /**
     * Adiciona um produto a uma comanda existente.
     * @param comandaId ID da Comanda onde o item será adicionado.
     * @param produtoId ID do Produto a ser adicionado.
     * @param quantidade Quantidade do produto.
     * @return O ItemPedido recém-criado.
     * @throws EntityNotFoundException Se a Comanda ou o Produto não forem encontrados.
     * @throws IllegalStateException Se a Comanda não estiver ABERTA.
     */
    @Transactional
    public ItemPedido adicionarItem(Long comandaId, Long produtoId, Integer quantidade) { // Use UUID se produtoId for UUID
        // 1. Busca a Comanda e verifica se está aberta
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada com ID: " + comandaId));

        if (comanda.getStatus() != Comanda.StatusComanda.ABERTA) {
            throw new IllegalStateException("Só é possível adicionar itens a comandas ABERTAS.");
        }

        // 2. Busca o Produto
        Produto produto = produtoRepository.findById(produtoId) // Use Long ou UUID aqui
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID: " + produtoId));

        // 3. Cria o novo ItemPedido
        ItemPedido novoItem = new ItemPedido();
        novoItem.setComanda(comanda);
        novoItem.setProduto(produto);
        novoItem.setQuantidade(quantidade);
        novoItem.setPrecoUnitario(produto.getPreco()); // Pega o preço atual do produto

        // 4. Salva o ItemPedido
        ItemPedido itemSalvo = itemPedidoRepository.save(novoItem);

        // 5. Recalcula e atualiza o valor total da Comanda
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

        // Forçar o carregamento dos itens (se for LAZY) para evitar LazyInitializationException no controller
        // Se a relação 'itens' já for EAGER, esta linha não é estritamente necessária, mas não prejudica.
        comanda.getItens().size(); // Acessa a lista para inicializá-la

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
        
        // --- FORÇAR INICIALIZAÇÃO (Now 'comandas' is definitely assigned) ---
        comandas.forEach(comanda -> {
            comanda.getItens().size(); // Initialize items
            if (comanda.getGuardaSol() != null) comanda.getGuardaSol().getIdentificacao(); // Initialize guardaSol
            // Add other lazy fields if needed (e.g., atendente)
            // if (comanda.getAtendente() != null) comanda.getAtendente().getNome(); 
        });
        
        return comandas; 
    }
}

