package com.seashade.api_seashade.controller.dto;

import com.seashade.api_seashade.model.ItemEstoque;

public record ItemEstoqueDropdownDto(
    Long id,    // O valor (value) do dropdown
    String nome // O rótulo (label)
) {
    public ItemEstoqueDropdownDto(ItemEstoque item) {
        this(
            item.getId(), 
            // Concatena nome e unidade para clareza no dropdown
            String.format("%s (%s)", item.getNome(), item.getUnidadeMedida()) 
        );
    }
}
