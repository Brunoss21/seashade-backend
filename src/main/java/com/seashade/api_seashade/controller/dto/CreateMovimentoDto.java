package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal;

import com.seashade.api_seashade.model.MovimentoEstoque;

public record CreateMovimentoDto(
    Long itemEstoqueId,
    MovimentoEstoque.TipoMovimento tipoMovimento,
    BigDecimal quantidade,
    String motivo,
    String observacao
) {}
