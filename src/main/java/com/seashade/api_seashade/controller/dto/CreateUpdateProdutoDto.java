package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal;

public record CreateUpdateProdutoDto(
    String nome,
    String descricao,
    BigDecimal preco,
    Integer estoque,
    String categoria
) {
}
