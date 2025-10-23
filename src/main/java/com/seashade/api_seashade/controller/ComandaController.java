package com.seashade.api_seashade.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.seashade.api_seashade.controller.dto.AddItemRequestDto;
import com.seashade.api_seashade.controller.dto.ComandaResponseDto;
import com.seashade.api_seashade.controller.dto.ItemPedidoResponseDto; 
import com.seashade.api_seashade.model.Comanda;
import com.seashade.api_seashade.model.ItemPedido;
import com.seashade.api_seashade.service.ComandaService;

@RestController
@RequestMapping("/api/comandas")
public class ComandaController {

    private final ComandaService comandaService;

    public ComandaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    public record OpenComandaRequestDto(Long guardaSolId) {}

    // --- CORREÇÃO 1: Retorna ComandaResponseDto ---
    @PostMapping
    public ResponseEntity<ComandaResponseDto> abrirNovaComanda(@RequestBody OpenComandaRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalId = authentication.getName(); 
        String scope = authentication.getAuthorities().stream()
                                  .map(GrantedAuthority::getAuthority)
                                  .collect(Collectors.joining(" ")); 

        Comanda novaComanda = comandaService.abrirComanda(requestDto.guardaSolId(), principalId, scope);

        // Converte a entidade para DTO antes de retornar
        return ResponseEntity.status(HttpStatus.CREATED).body(new ComandaResponseDto(novaComanda));
    }

    // --- CORREÇÃO 2: Retorna ItemPedidoResponseDto ---
    @PostMapping("/{comandaId}/itens")
    public ResponseEntity<ItemPedidoResponseDto> adicionarItemNaComanda(
            @PathVariable Long comandaId,
            @RequestBody AddItemRequestDto requestDto) {

        ItemPedido novoItem = comandaService.adicionarItem(
            comandaId,
            requestDto.produtoId(),
            requestDto.quantidade()
        );

        // Converte a entidade para DTO antes de retornar
        return ResponseEntity.status(HttpStatus.CREATED).body(new ItemPedidoResponseDto(novoItem));
    }

    // --- CORREÇÃO 3: Retorna ComandaResponseDto ---
    @GetMapping("/{comandaId}")
    public ResponseEntity<ComandaResponseDto> buscarComanda(@PathVariable Long comandaId) {
        Comanda comanda = comandaService.buscarComandaPorId(comandaId);
        
        // Converte a entidade para DTO antes de retornar
        return ResponseEntity.ok(new ComandaResponseDto(comanda));
    }

    // Método de listagem (já estava correto)
    @GetMapping
    public ResponseEntity<List<ComandaResponseDto>> listarComandas( 
            @RequestParam Long quiosqueId,
            @RequestParam(required = false) Comanda.StatusComanda status
    ) {
        List<Comanda> comandasEntidades = comandaService.listarComandasPorQuiosque(quiosqueId, status);
        
        List<ComandaResponseDto> comandasDto = comandasEntidades.stream()
                .map(ComandaResponseDto::new) 
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(comandasDto); 
    }
}


