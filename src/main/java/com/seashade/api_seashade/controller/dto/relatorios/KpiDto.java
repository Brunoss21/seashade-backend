package com.seashade.api_seashade.controller.dto.relatorios;

import java.math.BigDecimal;

public record KpiDto(
    BigDecimal ticketMedio,
    long pedidosAtivos,
    long pedidosFinalizadosHoje,
    BigDecimal faturamentoHoje
) {}
