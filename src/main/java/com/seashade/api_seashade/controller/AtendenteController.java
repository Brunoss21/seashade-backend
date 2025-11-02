package com.seashade.api_seashade.controller;

import com.seashade.api_seashade.controller.dto.CreateAtendenteDto; 
import com.seashade.api_seashade.model.Atendente;
import com.seashade.api_seashade.service.AtendenteService; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; 
import java.util.List;

@RestController
@RequestMapping("/api/quiosques/{quiosqueId}/atendentes") 
public class AtendenteController {

    private final AtendenteService atendenteService; 

    public AtendenteController(AtendenteService atendenteService) {
        this.atendenteService = atendenteService;
    }

    @PostMapping
    public ResponseEntity<Atendente> criarAtendente(@PathVariable Long quiosqueId, @RequestBody CreateAtendenteDto dto) {
        // Chama o método do serviço, passando o ID do quiosque e os dados do DTO
        Atendente novoAtendente = atendenteService.criarAtendente(quiosqueId, dto.nome(), dto.email());
        // Retorna a resposta com o objeto criado e o status HTTP apropriado
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAtendente);
    }

    @GetMapping
    public ResponseEntity<List<Atendente>> listarAtendentes(@PathVariable Long quiosqueId) {
        // Chama o método do serviço para buscar a lista
        List<Atendente> atendentes = atendenteService.listarAtendentesPorQuiosque(quiosqueId);
        // Retorna a lista na resposta
        return ResponseEntity.ok(atendentes);
    }

    @DeleteMapping("/{atendenteId}")
    public ResponseEntity<Void> excluirAtendente(
            @PathVariable Long quiosqueId, 
            @PathVariable Long atendenteId) {
        
        atendenteService.excluirAtendente(atendenteId);
        
        return ResponseEntity.noContent().build(); 
    }
}
