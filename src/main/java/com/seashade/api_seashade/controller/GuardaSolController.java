package com.seashade.api_seashade.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seashade.api_seashade.controller.dto.CreateGuardaSolDto;
import com.seashade.api_seashade.controller.dto.UpdateGuardaSolStatusDto;
import com.seashade.api_seashade.model.GuardaSol;
import com.seashade.api_seashade.service.GuardaSolService;

@RestController
@RequestMapping("/api/quiosques/{quiosqueId}/guardasois") 
public class GuardaSolController {

    private final GuardaSolService guardaSolService;

    public GuardaSolController(GuardaSolService guardaSolService) {
        this.guardaSolService = guardaSolService;
    }

    // Endpoint para LISTAR todos os guarda-sóis de um quiosque
    // Ex: GET /api/quiosques/uuid-do-quiosque/guardasois
    @GetMapping
    public ResponseEntity<List<GuardaSol>> listarGuardaSois(@PathVariable Long quiosqueId) {
        List<GuardaSol> guardaSois = guardaSolService.listarGuardaSoisPorQuiosque(quiosqueId);
        return ResponseEntity.ok(guardaSois);
    }

    // Endpoint para CRIAR um novo guarda-sol para um quiosque
    // Ex: POST /api/quiosques/uuid-do-quiosque/guardasois
    @PostMapping
    public ResponseEntity<GuardaSol> criarGuardaSol(@PathVariable Long quiosqueId, @RequestBody CreateGuardaSolDto dto) {
        GuardaSol novoGuardaSol = guardaSolService.criarGuardaSol(quiosqueId, dto.identificacao());
        return ResponseEntity.status(HttpStatus.CREATED).body(novoGuardaSol);
    }

    // Endpoint para MUDAR O STATUS de um guarda-sol específico
    // Ex: PATCH /api/quiosques/uuid-do-quiosque/guardasois/123/status
    @PatchMapping("/{guardaSolId}/status")
    public ResponseEntity<GuardaSol> mudarStatusGuardaSol(@PathVariable Long quiosqueId,
                                                         @PathVariable Long guardaSolId,
                                                         @RequestBody UpdateGuardaSolStatusDto dto) {
        GuardaSol guardaSolAtualizado = guardaSolService.mudarStatus(guardaSolId, dto.status());
        return ResponseEntity.ok(guardaSolAtualizado);
    }
}