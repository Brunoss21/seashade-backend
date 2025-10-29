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

    // ... (Endpoint POST / para abrir comanda) ...
    public record OpenComandaRequestDto(Long guardaSolId) {}
    @PostMapping
    public ResponseEntity<ComandaResponseDto> abrirNovaComanda(@RequestBody OpenComandaRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalId = authentication.getName();
        String scope = authentication.getAuthorities().stream()
                                  .map(GrantedAuthority::getAuthority)
                                  .collect(Collectors.joining(" "));

        Comanda novaComanda = comandaService.abrirComanda(requestDto.guardaSolId(), principalId, scope);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ComandaResponseDto(novaComanda));
    }


    // ... (Endpoint POST /{comandaId}/itens para adicionar item) ...
     @PostMapping("/{comandaId}/itens")
    public ResponseEntity<ItemPedidoResponseDto> adicionarItemNaComanda(
            @PathVariable Long comandaId,
            @RequestBody AddItemRequestDto requestDto) {

        ItemPedido novoItem = comandaService.adicionarItem(
            comandaId,
            requestDto.produtoId(),
            requestDto.quantidade()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new ItemPedidoResponseDto(novoItem));
    }


    // ... (Endpoint GET /{comandaId} para buscar comanda) ...
    @GetMapping("/{comandaId}")
    public ResponseEntity<ComandaResponseDto> buscarComanda(@PathVariable Long comandaId) {
        Comanda comanda = comandaService.buscarComandaPorId(comandaId);
        return ResponseEntity.ok(new ComandaResponseDto(comanda));
    }

    @GetMapping
    public ResponseEntity<List<ComandaResponseDto>> listarComandas(
            @RequestParam Long quiosqueId,
            // Aceita uma lista de status separados por vírgula (ex: status=NA_COZINHA,EM_PREPARO)
            @RequestParam(required = false) List<Comanda.StatusComanda> status
    ) {
        List<Comanda> comandasEntidades = comandaService.listarComandasPorQuiosque(quiosqueId, status);

        List<ComandaResponseDto> comandasDto = comandasEntidades.stream()
                .map(ComandaResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(comandasDto);
    }

    @PatchMapping("/{comandaId}/enviar-cozinha")
    public ResponseEntity<ComandaResponseDto> enviarCozinha(@PathVariable Long comandaId) {
        Comanda comandaAtualizada = comandaService.enviarParaCozinha(comandaId);
        return ResponseEntity.ok(new ComandaResponseDto(comandaAtualizada));
    }

    @PatchMapping("/{comandaId}/marcar-em-preparo")
    public ResponseEntity<ComandaResponseDto> marcarEmPreparo(@PathVariable Long comandaId) {
        Comanda comandaAtualizada = comandaService.marcarComandaEmPreparo(comandaId);
        return ResponseEntity.ok(new ComandaResponseDto(comandaAtualizada));
    }

    @PatchMapping("/{comandaId}/marcar-pronta")
    public ResponseEntity<ComandaResponseDto> marcarPronta(@PathVariable Long comandaId) {
        Comanda comandaAtualizada = comandaService.marcarComandaPronta(comandaId);
        return ResponseEntity.ok(new ComandaResponseDto(comandaAtualizada));
    }

     @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ComandaResponseDto> finalizarComanda(@PathVariable Long id) { // Retorna DTO
        Comanda comandaAtualizada = comandaService.finalizarComanda(id);
        return ResponseEntity.ok(new ComandaResponseDto(comandaAtualizada)); // Retorna DTO
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ComandaResponseDto> cancelarComanda(@PathVariable Long id) { // Retorna DTO
        Comanda comandaCancelada = comandaService.cancelarComanda(id);
        return ResponseEntity.ok(new ComandaResponseDto(comandaCancelada)); // Retorna DTO
    }

} 


