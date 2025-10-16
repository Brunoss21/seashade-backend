package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal;

public record CreateItemComMovimentoDto(
    // Dados para criar o ItemEstoque
    String nome,
    String descricao,
    String unidadeMedida,
    BigDecimal custoUnitario,

    // Dados para a primeira movimentação de ENTRADA
    BigDecimal quantidade,
    String motivo,
    String observacao
) {}
