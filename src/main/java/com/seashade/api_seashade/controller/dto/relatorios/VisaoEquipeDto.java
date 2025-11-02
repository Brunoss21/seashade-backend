package com.seashade.api_seashade.controller.dto.relatorios;

public record VisaoEquipeDto(
    String nome,
    long pedidosAtivos,
    long totalAtendidosHoje
) {}
