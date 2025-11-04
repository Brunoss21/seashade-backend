package com.seashade.api_seashade.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.seashade.api_seashade.controller.dto.ItemEstoqueDropdownDto;
import com.seashade.api_seashade.model.ItemEstoque;
import com.seashade.api_seashade.repository.ItemEstoqueRepository;

@Service
public class ItemEstoqueService {

    private final ItemEstoqueRepository itemEstoqueRepository;

    public ItemEstoqueService(ItemEstoqueRepository itemEstoqueRepository) {
        this.itemEstoqueRepository = itemEstoqueRepository;
    }

    /**
     * Lista todos os itens de estoque de um quiosque para usar em dropdowns.
     */
    public List<ItemEstoqueDropdownDto> listarItensParaDropdown(Long quiosqueId) {
        List<ItemEstoque> itens = itemEstoqueRepository.findByQuiosqueId(quiosqueId);
        
        return itens.stream()
            .filter(ItemEstoque::isAtivo) // Só mostra itens ativos no dropdown
            .map(ItemEstoqueDropdownDto::new) // Converte para o DTO
            .collect(Collectors.toList());
    }
}
