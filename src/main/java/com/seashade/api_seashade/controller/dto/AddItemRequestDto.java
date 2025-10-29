package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal; 

public record AddItemRequestDto(
    Long produtoId, 
    Integer quantidade
    // BigDecimal precoUnitario; // Opcional
) {}
