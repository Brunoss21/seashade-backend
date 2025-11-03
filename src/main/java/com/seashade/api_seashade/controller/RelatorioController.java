package com.seashade.api_seashade.controller;

import com.seashade.api_seashade.controller.dto.relatorios.*;
import com.seashade.api_seashade.service.RelatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiosques/{quiosqueId}/relatorios") // Rota base para relatórios
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/vendas-diarias")
    public ResponseEntity<List<VendasDiariasDto>> getVendasDiarias(
            @PathVariable Long quiosqueId,
            @RequestParam(defaultValue = "7") int dias) { // Padrão de 7 dias
        return ResponseEntity.ok(relatorioService.getVendasDiarias(quiosqueId, dias));
    }

    @GetMapping("/faturamento-mensal")
    public ResponseEntity<List<FaturamentoMensalDto>> getFaturamentoMensal(
            @PathVariable Long quiosqueId,
            @RequestParam(defaultValue = "0") int ano) {
        // Usa o ano atual se '0' ou não fornecido
        int anoBusca = (ano == 0) ? LocalDate.now().getYear() : ano;
        return ResponseEntity.ok(relatorioService.getFaturamentoMensal(quiosqueId, anoBusca));
    }

    @GetMapping("/receita-despesa-mensal")
    public ResponseEntity<List<ReceitaDespesaMensalDto>> getReceitaDespesaMensal(
            @PathVariable Long quiosqueId,
            @RequestParam(defaultValue = "0") int ano) {
        int anoBusca = (ano == 0) ? LocalDate.now().getYear() : ano;
        return ResponseEntity.ok(relatorioService.getReceitaDespesaMensal(quiosqueId, anoBusca));
    }

    @GetMapping("/vendas-compras-mensal")
    public ResponseEntity<List<VendasComprasMensalDto>> getVendasComprasMensal(
            @PathVariable Long quiosqueId,
            @RequestParam(defaultValue = "0") int ano) {
        int anoBusca = (ano == 0) ? LocalDate.now().getYear() : ano;
        return ResponseEntity.ok(relatorioService.getVendasComprasMensal(quiosqueId, anoBusca));
    }

    @GetMapping("/pedidos-por-atendente-mensal") 
    public ResponseEntity<List<Map<String, Object>>> getPedidosPorAtendenteMensal(
            @PathVariable Long quiosqueId,
            @RequestParam(defaultValue = "0") int ano) {
        
        int anoBusca = (ano == 0) ? LocalDate.now().getYear() : ano;

        return ResponseEntity.ok(relatorioService.getPedidosPorAtendenteMensal(quiosqueId, anoBusca));
    }

    @GetMapping("/pedidos-mensais")
    public ResponseEntity<List<PedidosMensaisDto>> getPedidosMensais(
            @PathVariable Long quiosqueId,
            @RequestParam(defaultValue = "0") int ano) {
        
        int anoBusca = (ano == 0) ? LocalDate.now().getYear() : ano;
        return ResponseEntity.ok(relatorioService.getPedidosMensais(quiosqueId, anoBusca));
    }

    @GetMapping("/kpis")
    public ResponseEntity<KpiDto> getKpis(@PathVariable Long quiosqueId) {
        return ResponseEntity.ok(relatorioService.getKpis(quiosqueId));
    }

    @GetMapping("/visao-equipe")
    public ResponseEntity<List<VisaoEquipeDto>> getVisaoEquipe(@PathVariable Long quiosqueId) {
        return ResponseEntity.ok(relatorioService.getVisaoEquipe(quiosqueId));
    }

    @GetMapping("/top-itens")
    public ResponseEntity<List<TopItemDto>> getTopItens(@PathVariable Long quiosqueId) {
        return ResponseEntity.ok(relatorioService.getTopItens(quiosqueId));
    }

    @GetMapping("/bottom-itens")
    public ResponseEntity<List<BottomItemDto>> getBottomItens(@PathVariable Long quiosqueId) {
        return ResponseEntity.ok(relatorioService.getBottomItens(quiosqueId));
    }

    @GetMapping("/estoque-critico")
    public ResponseEntity<List<EstoqueCriticoDto>> getEstoqueCritico(@PathVariable Long quiosqueId) {
        return ResponseEntity.ok(relatorioService.getEstoqueCritico(quiosqueId));
    }
}
