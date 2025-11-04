package com.seashade.api_seashade.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seashade.api_seashade.controller.dto.ComponenteResponseDto;
import com.seashade.api_seashade.controller.dto.CreateComponenteDto;
import com.seashade.api_seashade.model.ComponenteProduto;
import com.seashade.api_seashade.model.ItemEstoque;
import com.seashade.api_seashade.model.Produto;
import com.seashade.api_seashade.repository.ComponenteProdutoRepository;
import com.seashade.api_seashade.repository.ItemEstoqueRepository;
import com.seashade.api_seashade.repository.ProdutoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ComponenteProdutoService {
    private final ComponenteProdutoRepository componenteRepo;
    private final ProdutoRepository produtoRepo;
    private final ItemEstoqueRepository itemEstoqueRepo;

    public ComponenteProdutoService(ComponenteProdutoRepository componenteRepo,
                                    ProdutoRepository produtoRepo,
                                    ItemEstoqueRepository itemEstoqueRepo) {
        this.componenteRepo = componenteRepo;
        this.produtoRepo = produtoRepo;
        this.itemEstoqueRepo = itemEstoqueRepo;
    }

    // Lógica para o (GET /api/produtos/{id}/componentes)
    public List<ComponenteResponseDto> getComponentesPorProduto(Long produtoId) {
        if (!produtoRepo.existsById(produtoId)) {
            throw new EntityNotFoundException("Produto não encontrado: " + produtoId);
        }
        return componenteRepo.findByProdutoId(produtoId).stream()
            .map(ComponenteResponseDto::new) // Converte para DTO
            .collect(Collectors.toList());
    }

    // Lógica para o (POST /api/produtos/{id}/componentes)
    @Transactional
    public ComponenteResponseDto adicionarComponente(Long produtoId, CreateComponenteDto dto) {
        Produto produto = produtoRepo.findById(produtoId)
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + produtoId));
        
        ItemEstoque itemEstoque = itemEstoqueRepo.findById(dto.itemEstoqueId())
            .orElseThrow(() -> new EntityNotFoundException("Item de Estoque não encontrado: " + dto.itemEstoqueId()));

        // TODO: Adicionar verificação se este ingrediente já existe nesta receita

        ComponenteProduto novoComponente = new ComponenteProduto();
        novoComponente.setProduto(produto);
        novoComponente.setItemEstoque(itemEstoque);
        novoComponente.setQuantidadeUtilizada(dto.quantidadeUtilizada());

        ComponenteProduto salvo = componenteRepo.save(novoComponente);
        return new ComponenteResponseDto(salvo); // Retorna o DTO do componente criado
    }

    // Lógica para o (DELETE /api/componentes/{id})
    @Transactional
    public void removerComponente(Long componenteId) {
        if (!componenteRepo.existsById(componenteId)) {
            throw new EntityNotFoundException("Componente (ingrediente da receita) não encontrado: " + componenteId);
        }
        componenteRepo.deleteById(componenteId);
    }

}
