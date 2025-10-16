package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal;

public record CreateItemEstoqueDto(
    String nome,
    String descricao,
    String unidadeMedida, // Ex: "kg", "L", "unidade"
    BigDecimal quantidadeInicial,
    BigDecimal custoUnitario
) {}
