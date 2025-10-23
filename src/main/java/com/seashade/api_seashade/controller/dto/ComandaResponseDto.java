package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.seashade.api_seashade.model.Comanda;

public record ComandaResponseDto(
    Long id,
    String numeroComanda,
    LocalDateTime dataAbertura,
    Comanda.StatusComanda status,
    BigDecimal valorTotal,
    GuardaSolSimpleDto guardaSol, 
    List<ItemPedidoResponseDto> itens
    // String nomeAtendente; // Poderia adicionar se necessário
) {
    // Construtor para conversão
    public ComandaResponseDto(Comanda comanda) {
        this(
            comanda.getId(),
            comanda.getNumeroComanda(),
            comanda.getDataAbertura(),
            comanda.getStatus(),
            comanda.getValorTotal(),
            comanda.getGuardaSol() != null ? new GuardaSolSimpleDto(comanda.getGuardaSol()) : null,
            comanda.getItens().stream().map(ItemPedidoResponseDto::new).collect(Collectors.toList())
            // comanda.getAtendente() != null ? comanda.getAtendente().getNome() : null
        );
    }
}
