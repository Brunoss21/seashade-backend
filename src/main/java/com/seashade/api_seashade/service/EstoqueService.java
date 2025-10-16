package com.seashade.api_seashade.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.seashade.api_seashade.controller.dto.CreateItemComMovimentoDto;
import com.seashade.api_seashade.controller.dto.CreateItemEstoqueDto;
import com.seashade.api_seashade.model.ItemEstoque;
import com.seashade.api_seashade.model.MovimentoEstoque;
import com.seashade.api_seashade.model.Quiosque;
import com.seashade.api_seashade.model.User;
import com.seashade.api_seashade.repository.ItemEstoqueRepository;
import com.seashade.api_seashade.repository.MovimentoEstoqueRepository;
import com.seashade.api_seashade.repository.QuiosqueRepository;
import com.seashade.api_seashade.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class EstoqueService {

    private final ItemEstoqueRepository itemEstoqueRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final QuiosqueRepository quiosqueRepository;
    private final UserRepository userRepository;
    
    public EstoqueService(ItemEstoqueRepository itemEstoqueRepository,
                            MovimentoEstoqueRepository movimentoEstoqueRepository, 
                            QuiosqueRepository quiosqueRepository,
                            UserRepository userRepository) {
        this.itemEstoqueRepository = itemEstoqueRepository;
        this.movimentoEstoqueRepository = movimentoEstoqueRepository;
        this.quiosqueRepository = quiosqueRepository;
        this.userRepository = userRepository;
    }

    public List<ItemEstoque> listarItensEstoque(Long quiosqueId) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));
        return itemEstoqueRepository.findByQuiosqueAndAtivoTrue(quiosque);
    }

    public List<MovimentoEstoque> listarHistoricoMovimentacoes(Long quiosqueId) {
    // Garante que o quiosque existe antes de fazer a busca
    quiosqueRepository.findById(quiosqueId)
            .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));
    return movimentoEstoqueRepository.findMovimentosByQuiosqueId(quiosqueId);
    }


    // REGISTRAR uma nova movimentação
    @Transactional
    public MovimentoEstoque registrarMovimentacao(Long itemEstoqueId, MovimentoEstoque.TipoMovimento tipo, BigDecimal quantidade, String motivo, String observacao, UUID usuarioId) {
        ItemEstoque item = itemEstoqueRepository.findById(itemEstoqueId)
                .orElseThrow(() -> new EntityNotFoundException("Item de estoque não encontrado"));

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        // Atualiza a quantidade atual do item
        if (tipo == MovimentoEstoque.TipoMovimento.ENTRADA) {
            item.setQuantidadeAtual(item.getQuantidadeAtual().add(quantidade));
        } else { // SAIDA
            if (item.getQuantidadeAtual().compareTo(quantidade) < 0) {
                throw new IllegalStateException("Quantidade de saída é maior que o estoque atual.");
            }
            item.setQuantidadeAtual(item.getQuantidadeAtual().subtract(quantidade));
        }
        itemEstoqueRepository.save(item);

        // Cria o registro no histórico
        MovimentoEstoque movimento = new MovimentoEstoque();
        movimento.setItemEstoque(item);
        movimento.setTipoMovimento(tipo);
        movimento.setQuantidade(quantidade);
        movimento.setMotivo(motivo);
        movimento.setObservacao(observacao);
        movimento.setUsuario(usuario);
        movimento.setDataMovimento(LocalDateTime.now());

        return movimentoEstoqueRepository.save(movimento);

    }

    @Transactional
    public ItemEstoque criarItemEstoque(Long quiosqueId, CreateItemEstoqueDto dto) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));
        ItemEstoque novoItem = new ItemEstoque();
        novoItem.setNome(dto.nome());
        novoItem.setDescricao(dto.descricao());
        novoItem.setUnidadeMedida(dto.unidadeMedida());
        novoItem.setQuantidadeAtual(dto.quantidadeInicial());
        novoItem.setCustoUnitario(dto.custoUnitario());
        novoItem.setQuiosque(quiosque);

        return itemEstoqueRepository.save(novoItem);
    }

    @Transactional
    public MovimentoEstoque criarItemComPrimeiroMovimento(Long quiosqueId, CreateItemComMovimentoDto dto, UUID usuarioId) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));

        // 1. Cria o novo ItemEstoque
        ItemEstoque novoItem = new ItemEstoque();
        novoItem.setNome(dto.nome());
        novoItem.setDescricao(dto.descricao());
        novoItem.setUnidadeMedida(dto.unidadeMedida());
        novoItem.setCustoUnitario(dto.custoUnitario());
        novoItem.setQuantidadeAtual(BigDecimal.ZERO); // Começa com zero antes do primeiro movimento
        novoItem.setQuiosque(quiosque);
        ItemEstoque itemSalvo = itemEstoqueRepository.save(novoItem);

        // 2. Registra a primeira movimentação de ENTRADA para este novo item
        return registrarMovimentacao(
            itemSalvo.getId(),
            MovimentoEstoque.TipoMovimento.ENTRADA,
            dto.quantidade(),
            dto.motivo(),
            dto.observacao(),
            usuarioId
        );
    }

    @Transactional
    public void desativarItemEstoque(Long itemEstoqueId) {
        ItemEstoque item = itemEstoqueRepository.findById(itemEstoqueId)
                .orElseThrow(() -> new EntityNotFoundException("Item de estoque não encontrado"));

        item.setAtivo(false);
        
        itemEstoqueRepository.save(item);
    }

}
