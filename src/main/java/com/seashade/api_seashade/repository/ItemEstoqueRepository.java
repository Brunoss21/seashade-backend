package com.seashade.api_seashade.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.seashade.api_seashade.model.ItemEstoque;
import com.seashade.api_seashade.model.Quiosque;

import java.util.List;
import java.util.Optional;


public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {

    // Verifica se um item do estoque está ativo
    List<ItemEstoque> findByQuiosqueAndAtivoTrue(Quiosque quiosque);

    /**
     * Busca os itens de estoque de um quiosque, ordenados pela menor quantidade
     * atual de estoque primeiro. A paginação (Pageable) limita o resultado.
     * * * Assumindo que a entidade ItemEstoque tem um campo 'quiosque'
     * * e um campo 'quantidadeAtual'.
     */
    List<ItemEstoque> findByQuiosqueIdOrderByQuantidadeAtualAsc(Long quiosqueId, Pageable pageable);
    
    Optional<ItemEstoque> findByQuiosqueIdAndNome(Long quiosqueId, String nome);

    /**
     * Encontra todos os itens de estoque de um quiosque.
     */
    List<ItemEstoque> findByQuiosqueId(Long quiosqueId);
}
