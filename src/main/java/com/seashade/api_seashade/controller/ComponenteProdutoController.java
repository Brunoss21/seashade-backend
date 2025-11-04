package com.seashade.api_seashade.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seashade.api_seashade.controller.dto.ComponenteResponseDto;
import com.seashade.api_seashade.controller.dto.CreateComponenteDto;
import com.seashade.api_seashade.service.ComponenteProdutoService;

@RestController
@RequestMapping("/api/produtos")
public class ComponenteProdutoController {
    
    private final ComponenteProdutoService componenteService;

    public ComponenteProdutoController(ComponenteProdutoService componenteService) {
        this.componenteService = componenteService;
    }

    /**
     * Busca a receita (lista de ingredientes) de um produto específico.
     */
    @GetMapping("/{produtoId}/componentes")
    public ResponseEntity<List<ComponenteResponseDto>> getComponentes(@PathVariable Long produtoId) {
        List<ComponenteResponseDto> componentes = componenteService.getComponentesPorProduto(produtoId);
        return ResponseEntity.ok(componentes);
    }

    /**
     * Adiciona um novo ingrediente (componente) à receita de um produto.
     */
    @PostMapping("/{produtoId}/componentes")
    public ResponseEntity<ComponenteResponseDto> addComponente(
        @PathVariable Long produtoId, 
        @RequestBody CreateComponenteDto dto) {
        
        ComponenteResponseDto novoComponente = componenteService.adicionarComponente(produtoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoComponente);
    }

    /**
     * Remove um ingrediente (componente) da receita.
     */
    @DeleteMapping("/componentes/{componenteId}")
    public ResponseEntity<Void> removeComponente(@PathVariable Long componenteId) {
        componenteService.removerComponente(componenteId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}
