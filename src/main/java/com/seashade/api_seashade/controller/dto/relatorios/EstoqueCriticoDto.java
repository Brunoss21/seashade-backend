package com.seashade.api_seashade.controller.dto.relatorios;

import java.math.BigDecimal;

// DTO para o card "Estoque Crítico")
public record EstoqueCriticoDto(
    String nome,
    BigDecimal quantidade,
    Integer max          
) {}
