package com.seashade.api_seashade.service;

import com.seashade.api_seashade.controller.dto.relatorios.*;
import com.seashade.api_seashade.model.Comanda;
import com.seashade.api_seashade.model.Despesa;
import com.seashade.api_seashade.model.ItemEstoque;
import com.seashade.api_seashade.model.Quiosque;
import com.seashade.api_seashade.model.Atendente;
import com.seashade.api_seashade.repository.AtendenteRepository;
import com.seashade.api_seashade.repository.BottomItemProjection;
import com.seashade.api_seashade.repository.ComandaRepository;
import com.seashade.api_seashade.repository.DespesaRepository;
import com.seashade.api_seashade.repository.ItemEstoqueRepository;
import com.seashade.api_seashade.repository.ProdutoRepository;
import com.seashade.api_seashade.repository.QuiosqueRepository;
import com.seashade.api_seashade.repository.TopItemProjection;

import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;


@Service
public class RelatorioService {

    private final ProdutoRepository produtoRepository;

    private final QuiosqueRepository quiosqueRepository;

    private final ComandaRepository comandaRepository;
    private final DespesaRepository despesaRepository;
    private final AtendenteRepository atendenteRepository; 
    private final ItemEstoqueRepository itemEstoqueRepository;

    private static final Locale LOCALE_BR = new Locale.Builder().setLanguage("pt").setRegion("BR").build(); 
    private static final DateTimeFormatter MES_FORMATTER = DateTimeFormatter.ofPattern("MMM", LOCALE_BR);
    private static final TextStyle DIA_SEMANA_STYLE = TextStyle.SHORT;


    public RelatorioService(ComandaRepository comandaRepository,
                            DespesaRepository despesaRepository,
                            AtendenteRepository atendenteRepository,
                            QuiosqueRepository quiosqueRepository,
                            ProdutoRepository produtoRepository,
                            ItemEstoqueRepository itemEstoqueRepository) {
        this.comandaRepository = comandaRepository;
        this.despesaRepository = despesaRepository;
        this.atendenteRepository = atendenteRepository;
        this.quiosqueRepository = quiosqueRepository;
        this.produtoRepository = produtoRepository;
        this.itemEstoqueRepository = itemEstoqueRepository;
    }

    private static final List<Comanda.StatusComanda> STATUS_ATIVOS = List.of(
            Comanda.StatusComanda.ABERTA,
            Comanda.StatusComanda.NA_COZINHA,
            Comanda.StatusComanda.EM_PREPARO,
            Comanda.StatusComanda.PRONTO_PARA_ENTREGA
    );

    // --- Vendas Diárias ---
    public List<VendasDiariasDto> getVendasDiarias(Long quiosqueId, int dias) {
        LocalDateTime fim = LocalDateTime.now();
        LocalDateTime inicio = fim.minusDays(dias).withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<Comanda> comandasFechadas = comandaRepository.findByQuiosqueIdAndStatusAndDataFechamentoBetween(
                quiosqueId, Comanda.StatusComanda.FECHADA, inicio, fim);

        Map<DayOfWeek, Long> vendasPorDia = comandasFechadas.stream()
                .filter(c -> c.getDataFechamento() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDataFechamento().getDayOfWeek(), // Agrupa por DayOfWeek Enum
                        Collectors.counting()
                ));

        // Cria DTOs ordenados de Seg a Dom
        return Arrays.stream(DayOfWeek.values()) // Itera sobre os dias da semana (SEG a DOM)
                     .map(dia -> new VendasDiariasDto(
                             dia.getDisplayName(DIA_SEMANA_STYLE, LOCALE_BR), // Obtem nome curto ("Seg", "Ter"...)
                             vendasPorDia.getOrDefault(dia, 0L))) // Pega contagem ou 0
                     .collect(Collectors.toList());
    }

    public List<EstoqueCriticoDto> getEstoqueCritico(Long quiosqueId) {
        
        Pageable limit = PageRequest.of(0, 3); 

        List<ItemEstoque> itens = itemEstoqueRepository.findByQuiosqueIdOrderByQuantidadeAtualAsc(
            quiosqueId, limit
        );

        return itens.stream()
            .map(item -> new EstoqueCriticoDto(
                item.getNome(),      
                item.getQuantidadeAtual(),    
                item.getEstoqueMaximo()    
            ))
            .collect(Collectors.toList());
    }

    // --- Faturamento Mensal ---
    public List<FaturamentoMensalDto> getFaturamentoMensal(Long quiosqueId, int ano) {
        LocalDateTime inicioAno = LocalDateTime.of(ano, 1, 1, 0, 0);
        LocalDateTime fimAno = LocalDateTime.of(ano, 12, 31, 23, 59, 59);

        List<Comanda> comandasFechadas = comandaRepository.findByQuiosqueIdAndStatusAndDataFechamentoBetween(
                quiosqueId, Comanda.StatusComanda.FECHADA, inicioAno, fimAno);

        Map<Month, BigDecimal> faturamentoPorMes = comandasFechadas.stream()
                .filter(c -> c.getDataFechamento() != null && c.getValorTotal() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDataFechamento().getMonth(), // Agrupa por Month Enum
                        Collectors.mapping(Comanda::getValorTotal, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // Cria DTOs ordenados de Jan a Dez
        return Arrays.stream(Month.values()) // Itera sobre os meses (JAN a DEC)
                     .map(mes -> new FaturamentoMensalDto(
                             mes.getDisplayName(TextStyle.SHORT, LOCALE_BR), // "Jan", "Fev"...
                             faturamentoPorMes.getOrDefault(mes, BigDecimal.ZERO)))
                     .collect(Collectors.toList());
    }

    // --- Receita x Despesa Mensal ---
    public List<ReceitaDespesaMensalDto> getReceitaDespesaMensal(Long quiosqueId, int ano) {
        LocalDate inicioAno = LocalDate.of(ano, 1, 1);
        LocalDate fimAno = LocalDate.of(ano, 12, 31);
        LocalDateTime inicioAnoTime = inicioAno.atStartOfDay();
        LocalDateTime fimAnoTime = fimAno.atTime(23, 59, 59);

        // Busca Receitas (Comandas Fechadas)
        List<Comanda> comandasFechadas = comandaRepository.findByQuiosqueIdAndStatusAndDataFechamentoBetween(
                quiosqueId, Comanda.StatusComanda.FECHADA, inicioAnoTime, fimAnoTime);

        Map<Month, BigDecimal> receitaPorMes = comandasFechadas.stream()
                .filter(c -> c.getDataFechamento() != null && c.getValorTotal() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDataFechamento().getMonth(),
                        Collectors.mapping(Comanda::getValorTotal, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // Busca Despesas
        List<Despesa> despesas = despesaRepository.findByQuiosqueIdAndDataDespesaBetween(quiosqueId, inicioAno, fimAno);

        Map<Month, BigDecimal> despesaPorMes = despesas.stream()
                .filter(d -> d.getValor() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getDataDespesa().getMonth(),
                        Collectors.mapping(Despesa::getValor, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // Combina os resultados
        return Arrays.stream(Month.values())
                     .map(mes -> new ReceitaDespesaMensalDto(
                             mes.getDisplayName(TextStyle.SHORT, LOCALE_BR),
                             receitaPorMes.getOrDefault(mes, BigDecimal.ZERO),
                             despesaPorMes.getOrDefault(mes, BigDecimal.ZERO)))
                     .collect(Collectors.toList());
    }

    // --- Vendas x Compras Mensal ---
    public List<VendasComprasMensalDto> getVendasComprasMensal(Long quiosqueId, int ano) {
        LocalDate inicioAno = LocalDate.of(ano, 1, 1);
        LocalDate fimAno = LocalDate.of(ano, 12, 31);
        LocalDateTime inicioAnoTime = inicioAno.atStartOfDay();
        LocalDateTime fimAnoTime = fimAno.atTime(23, 59, 59);

        // Contagem de Vendas (Comandas Fechadas)
        List<Comanda> comandasFechadas = comandaRepository.findByQuiosqueIdAndStatusAndDataFechamentoBetween(
                quiosqueId, Comanda.StatusComanda.FECHADA, inicioAnoTime, fimAnoTime);

        Map<Month, Long> vendasPorMes = comandasFechadas.stream()
                .filter(c -> c.getDataFechamento() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDataFechamento().getMonth(),
                        Collectors.counting()
                ));

        // Somatório de Compras (Exemplo: Despesas com categoria "FORNECEDOR")
        List<Despesa> comprasFornecedor = despesaRepository.findByQuiosqueIdAndCategoriaAndDataDespesaBetween(
                quiosqueId, "FORNECEDOR", inicioAno, fimAno);

        Map<Month, BigDecimal> comprasPorMes = comprasFornecedor.stream()
                 .filter(d -> d.getValor() != null)
                 .collect(Collectors.groupingBy(
                         d -> d.getDataDespesa().getMonth(),
                         Collectors.mapping(Despesa::getValor, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                 ));


        // Combina os resultados
        return Arrays.stream(Month.values())
                     .map(mes -> new VendasComprasMensalDto(
                             mes.getDisplayName(TextStyle.SHORT, LOCALE_BR),
                             vendasPorMes.getOrDefault(mes, 0L),
                             comprasPorMes.getOrDefault(mes, BigDecimal.ZERO)))
                     .collect(Collectors.toList());
    }

    // --- Pedidos Atendidos por Funcionário (Mensal) ---
    public List<PedidosPorAtendenteDto> getPedidosPorAtendente(Long quiosqueId, int ano, int mes) {
        YearMonth anoMes = YearMonth.of(ano, mes);
        LocalDateTime inicioMes = anoMes.atDay(1).atStartOfDay();
        LocalDateTime fimMes = anoMes.atEndOfMonth().atTime(23, 59, 59);

        List<Comanda> comandasFechadas = comandaRepository.findByQuiosqueIdAndStatusAndAtendenteIsNotNullAndDataFechamentoBetween(
                quiosqueId, Comanda.StatusComanda.FECHADA, inicioMes, fimMes);

        Map<Long, Long> contagemPorAtendenteId = comandasFechadas.stream()
                .filter(c -> c.getAtendente() != null) 
                .collect(Collectors.groupingBy(
                        c -> c.getAtendente().getId(), 
                        Collectors.counting()
                ));

        // Busca os nomes dos atendentes e cria os DTOs
        return contagemPorAtendenteId.entrySet().stream()
                .map(entry -> {
                    String nomeAtendente = atendenteRepository.findById(entry.getKey())
                                                             .map(atendente -> atendente.getNome())
                                                             .orElse("ID: " + entry.getKey()); 
                    return new PedidosPorAtendenteDto(nomeAtendente, entry.getValue());
                })
                .sorted(Comparator.comparingLong(PedidosPorAtendenteDto::quantidade).reversed()) 
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPedidosPorAtendenteMensal(Long quiosqueId, int ano) {
        LocalDateTime inicioAno = LocalDateTime.of(ano, 1, 1, 0, 0);
        LocalDateTime fimAno = LocalDateTime.of(ano, 12, 31, 23, 59, 59);

        // 1. Busca todas as comandas relevantes do ano
        List<Comanda> comandasFechadas = comandaRepository.findByQuiosqueIdAndStatusAndAtendenteIsNotNullAndDataFechamentoBetween(
                quiosqueId, Comanda.StatusComanda.FECHADA, inicioAno, fimAno);

        // 2. Cria um conjunto com todos os atendentes que fizeram vendas
        Set<Atendente> atendentes = comandasFechadas.stream()
                                    .map(Comanda::getAtendente)
                                    .collect(Collectors.toSet());

        // 3. Agrupa os dados por Mês, e depois por Nome do Atendente, e conta
        Map<Month, Map<String, Long>> dadosAgrupados = comandasFechadas.stream()
            .collect(Collectors.groupingBy(
                c -> c.getDataFechamento().getMonth(), // Chave Externa: Mês (JAN, FEV...)
                Collectors.groupingBy(
                    c -> c.getAtendente().getNome(), // Chave Interna: Nome ("Bruno", "Ana"...)
                    Collectors.counting() // Valor: Contagem (10, 12...)
                )
            ));

        // 4. Formata a saída para a lista de Mapas que o Recharts espera
        List<Map<String, Object>> resultadoFormatado = new ArrayList<>();

        for (Month mes : Month.values()) { // Itera de JAN a DEZ
            Map<String, Object> entryMes = new LinkedHashMap<>();
            // Adiciona a coluna "mes" (ex: "Jan", "Fev")
            entryMes.put("mes", mes.getDisplayName(TextStyle.SHORT, LOCALE_BR)); 
            
            // Pega o mapa de vendas daquele mês (ou um mapa vazio se não houver vendas)
            Map<String, Long> dadosDoMes = dadosAgrupados.getOrDefault(mes, Collections.emptyMap());

            // Adiciona uma coluna para cada atendente (ex: "Bruno": 10)
            for (Atendente atendente : atendentes) {
                entryMes.put(atendente.getNome(), dadosDoMes.getOrDefault(atendente.getNome(), 0L));
            }
            
            resultadoFormatado.add(entryMes);
        }

        return resultadoFormatado;
    }

    // --- MÉTODO: Pedidos Totais por Mês ---
    public List<PedidosMensaisDto> getPedidosMensais(Long quiosqueId, int ano) {
        LocalDateTime inicioAno = LocalDateTime.of(ano, 1, 1, 0, 0);
        LocalDateTime fimAno = LocalDateTime.of(ano, 12, 31, 23, 59, 59);

        // 1. Busca as mesmas comandas de faturamento
        List<Comanda> comandasFechadas = comandaRepository.findByQuiosqueIdAndStatusAndDataFechamentoBetween(
                quiosqueId, Comanda.StatusComanda.FECHADA, inicioAno, fimAno);

        // 2. Agrupa por Mês e CONTA as ocorrências
        Map<Month, Long> pedidosPorMes = comandasFechadas.stream()
                .filter(c -> c.getDataFechamento() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDataFechamento().getMonth(), // Agrupa por Month Enum
                        Collectors.counting() // <-- ÚNICA MUDANÇA LÓGICA (em vez de somar valorTotal)
                ));

        // 3. Cria os DTOs
        return Arrays.stream(Month.values()) // Itera de JAN a DEZ
                     .map(mes -> new PedidosMensaisDto(
                             mes.getDisplayName(TextStyle.SHORT, LOCALE_BR), // "Jan", "Fev"...
                             pedidosPorMes.getOrDefault(mes, 0L))) // Pega a contagem ou 0
                     .collect(Collectors.toList());
    }

    public KpiDto getKpis(Long quiosqueId) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));

        // 1. Pedidos Ativos (Conta quantos estão ABERTOS, NA_COZINHA, etc.)
        long pedidosAtivos = comandaRepository.countByQuiosqueAndStatusIn(quiosque, STATUS_ATIVOS);

        // 2. Pedidos Finalizados Hoje (Define o intervalo de "Hoje")
        LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);
        
        long pedidosFinalizadosHoje = comandaRepository.countByQuiosqueAndStatusAndDataFechamentoBetween(
            quiosque, Comanda.StatusComanda.FECHADA, inicioDoDia, fimDoDia
        );

        // 3. Ticket Médio (Faturamento de Hoje / Pedidos de Hoje)
        List<Comanda> comandasDeHoje = comandaRepository.findByQuiosqueIdAndStatusAndDataFechamentoBetween(
            quiosqueId, Comanda.StatusComanda.FECHADA, inicioDoDia, fimDoDia
        );
        
        BigDecimal faturamentoTotalHoje = comandasDeHoje.stream()
            .map(Comanda::getValorTotal)
            .filter(Objects::nonNull) // Ignora valores nulos
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (pedidosFinalizadosHoje > 0) {
            // Calcula: Faturamento / Qtd Pedidos, com 2 casas decimais
            ticketMedio = faturamentoTotalHoje.divide(BigDecimal.valueOf(pedidosFinalizadosHoje), 2, RoundingMode.HALF_UP);
        }

        return new KpiDto(ticketMedio, pedidosAtivos, pedidosFinalizadosHoje,faturamentoTotalHoje);
    }

    public List<TopItemDto> getTopItens(Long quiosqueId) {
        LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);

        List<TopItemProjection> projections = comandaRepository.findTopVendidosHoje(
            quiosqueId, inicioDoDia, fimDoDia
        );

        return projections.stream()
            .map(p -> new TopItemDto(p.getNome(), p.getQtd()))
            .collect(Collectors.toList());
    }

    public List<VisaoEquipeDto> getVisaoEquipe(Long quiosqueId) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));

        List<Atendente> atendentes = atendenteRepository.findByQuiosque(quiosque);

        LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);

        List<VisaoEquipeDto> visaoEquipe = new ArrayList<>();

        for (Atendente atendente : atendentes) {
            long pedidosAtivos = comandaRepository.countByAtendenteAndStatusIn(atendente, STATUS_ATIVOS);
            
            long totalAtendidosHoje = comandaRepository.countByAtendenteAndStatusAndDataFechamentoBetween(
                atendente, 
                Comanda.StatusComanda.FECHADA, 
                inicioDoDia, 
                fimDoDia
            );
            visaoEquipe.add(new VisaoEquipeDto(atendente.getNome(), pedidosAtivos, totalAtendidosHoje));
        }
        return visaoEquipe;
    }

    public List<BottomItemDto> getBottomItens(Long quiosqueId) {
        // Define o intervalo de "Hoje"
        LocalDateTime inicioDoDia = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime fimDoDia = LocalDateTime.now().with(LocalTime.MAX);

        // Chama a nova query nativa
        List<BottomItemProjection> projections = produtoRepository.findBottomVendidosHoje(
            quiosqueId, inicioDoDia, fimDoDia
        );

        // Converte a Projeção para o DTO
        return projections.stream()
            .map(p -> new BottomItemDto(p.getNome(), p.getQtdVendida()))
            .collect(Collectors.toList());
    }
}