package com.seashade.api_seashade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seashade.api_seashade.model.ComponenteProduto;


@Repository
public interface ComponenteProdutoRepository extends JpaRepository<ComponenteProduto, Long> {

    /**
     * Encontra todos os componentes (ingredientes) de um Produto pelo ID do produto.
     */
    List<ComponenteProduto> findByProdutoId(Long produtoId);
}
