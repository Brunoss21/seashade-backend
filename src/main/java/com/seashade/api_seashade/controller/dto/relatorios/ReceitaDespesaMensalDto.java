package com.seashade.api_seashade.controller.dto.relatorios;

import java.math.BigDecimal;

public record ReceitaDespesaMensalDto(
        String mes, 
        BigDecimal receita,
        BigDecimal despesa) {
}