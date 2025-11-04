package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal;

// DTO para ADICIONAR um ingrediente a uma receita
public record CreateComponenteDto(
    Long itemEstoqueId,      // ID do ingrediente (ex: "Batata Congelada")
    BigDecimal quantidadeUtilizada // (ex: 0.5 para 0.5 kg)
) {}
