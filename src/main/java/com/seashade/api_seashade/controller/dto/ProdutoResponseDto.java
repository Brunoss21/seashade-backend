package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal;

import com.seashade.api_seashade.model.Produto;

public record ProdutoResponseDto(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    String CategoriaProduto, 
    Integer estoque
) {
    public ProdutoResponseDto(Produto produto) {
        this(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getPreco(),
            produto.getCategoria().name(),
            produto.getEstoque()
           
        );
    }
}
