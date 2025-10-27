package com.seashade.api_seashade.controller;

import com.seashade.api_seashade.controller.dto.relatorios.*;
import com.seashade.api_seashade.service.RelatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
// TODO: Adicionar segurança para garantir que o usuário só acesse dados do seu quiosque
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/vendas-diarias")
    public ResponseEntity<List<VendasDiariasDto>> getVendasDiarias(
            @RequestParam Long quiosqueId,
            @RequestParam(defaultValue = "7") int dias) { // Padrão para os últimos 7 dias
        // TODO: Validar se quiosqueId pertence ao usuário autenticado
        List<VendasDiariasDto> dados = relatorioService.getVendasDiarias(quiosqueId, dias);
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/faturamento-mensal")
    public ResponseEntity<List<FaturamentoMensalDto>> getFaturamentoMensal(
            @RequestParam Long quiosqueId,
            @RequestParam(required = false) Integer ano) {
        // TODO: Validar se quiosqueId pertence ao usuário autenticado
        int anoAtual = (ano != null) ? ano : LocalDate.now().getYear(); // Usa ano atual se não fornecido
        List<FaturamentoMensalDto> dados = relatorioService.getFaturamentoMensal(quiosqueId, anoAtual);
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/receita-despesa-mensal")
    public ResponseEntity<List<ReceitaDespesaMensalDto>> getReceitaDespesaMensal(
            @RequestParam Long quiosqueId,
            @RequestParam(required = false) Integer ano) {
        // TODO: Validar se quiosqueId pertence ao usuário autenticado
        int anoAtual = (ano != null) ? ano : LocalDate.now().getYear();
        List<ReceitaDespesaMensalDto> dados = relatorioService.getReceitaDespesaMensal(quiosqueId, anoAtual);
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/vendas-compras-mensal")
    public ResponseEntity<List<VendasComprasMensalDto>> getVendasComprasMensal(
            @RequestParam Long quiosqueId,
            @RequestParam(required = false) Integer ano) {
        // TODO: Validar se quiosqueId pertence ao usuário autenticado
        int anoAtual = (ano != null) ? ano : LocalDate.now().getYear();
        List<VendasComprasMensalDto> dados = relatorioService.getVendasComprasMensal(quiosqueId, anoAtual);
        return ResponseEntity.ok(dados);
    }

     @GetMapping("/pedidos-por-atendente")
    public ResponseEntity<List<PedidosPorAtendenteDto>> getPedidosPorAtendente(
            @RequestParam Long quiosqueId,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes) {
        // TODO: Validar se quiosqueId pertence ao usuário autenticado
        LocalDate hoje = LocalDate.now();
        int anoAtual = (ano != null) ? ano : hoje.getYear();
        int mesAtual = (mes != null) ? mes : hoje.getMonthValue(); // Usa mes atual se não fornecido

        List<PedidosPorAtendenteDto> dados = relatorioService.getPedidosPorAtendente(quiosqueId, anoAtual, mesAtual);
        return ResponseEntity.ok(dados);
    }

}