package com.seashade.api_seashade.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seashade.api_seashade.controller.dto.ItemEstoqueDropdownDto;
import com.seashade.api_seashade.service.ItemEstoqueService;

import java.util.List;

@RestController
@RequestMapping("/api/quiosques/{quiosqueId}/itens-estoque")
public class ItemEstoqueController {

    private final ItemEstoqueService itemEstoqueService;

    public ItemEstoqueController(ItemEstoqueService itemEstoqueService) {
        this.itemEstoqueService = itemEstoqueService;
    }

    /**
     * Endpoint para o frontend buscar a lista de ingredientes
     * para o dropdown do modal de receitas.
     */
    @GetMapping
    public ResponseEntity<List<ItemEstoqueDropdownDto>> getItensEstoqueParaDropdown(@PathVariable Long quiosqueId) {
        List<ItemEstoqueDropdownDto> lista = itemEstoqueService.listarItensParaDropdown(quiosqueId);
        return ResponseEntity.ok(lista);
    }

}
