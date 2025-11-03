package com.seashade.api_seashade.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.seashade.api_seashade.model.Atendente;
import com.seashade.api_seashade.model.Comanda;
import com.seashade.api_seashade.model.GuardaSol;
import com.seashade.api_seashade.model.Quiosque;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {

    // Método para buscar todas as comandas de um quiosque, ordenadas pela mais recente
    List<Comanda> findByQuiosqueOrderByDataAberturaDesc(Quiosque quiosque);

    // Método para buscar todas as comandas de um quiosque com um status específico
    List<Comanda> findByQuiosqueAndStatusOrderByDataAberturaDesc(Quiosque quiosque, Comanda.StatusComanda status);

    // Método útil para encontrar a comanda ABERTA para um guarda-sol específico
    Optional<Comanda> findByGuardaSolAndStatus(GuardaSol guardaSol, Comanda.StatusComanda status);

    long countByQuiosque(Quiosque quiosque);

    @Query("SELECT c FROM Comanda c LEFT JOIN FETCH c.itens i LEFT JOIN FETCH i.produto p WHERE c.id = :comandaId")
    Optional<Comanda> findByIdWithItensAndProdutos(@Param("comandaId") Long comandaId);

    /**
     * Busca comandas de um quiosque que estejam em qualquer um dos status fornecidos,
     * ordenadas pela data de abertura descendente.
     */
    List<Comanda> findByQuiosqueAndStatusInOrderByDataAberturaDesc(Quiosque quiosque, List<Comanda.StatusComanda> statuses);

    // Busca comandas fechadas em um período específico para um quiosque
    List<Comanda> findByQuiosqueIdAndStatusAndDataFechamentoBetween(Long quiosqueId, Comanda.StatusComanda status, LocalDateTime inicio, LocalDateTime fim);

    // Busca comandas fechadas, com atendente, em um período específico para um quiosque
    List<Comanda> findByQuiosqueIdAndStatusAndAtendenteIsNotNullAndDataFechamentoBetween(Long quiosqueId, Comanda.StatusComanda status, LocalDateTime inicio, LocalDateTime fim);

    // Verifica se tem atendente com comanda em aberto
    boolean existsByAtendenteAndStatusIn(Atendente atendente, List<Comanda.StatusComanda> status);

    // Verifica se tem guarda sol sem status
    boolean existsByGuardaSolAndStatusInAndIdNot(GuardaSol guardaSol, List<Comanda.StatusComanda> statuses, Long comandaIdToExclude);

    /**
     * Conta quantas comandas um QUIOSQUE possui em uma lista de status.
     * (Usado para "Pedidos Ativos")
     */
    long countByQuiosqueAndStatusIn(Quiosque quiosque, List<Comanda.StatusComanda> statuses);

    /**
     * Conta quantas comandas um QUIOSQUE finalizou (FECHADA) 
     * dentro de um intervalo de datas.
     * (Usado para "Pedidos Finalizados Hoje")
     */
    long countByQuiosqueAndStatusAndDataFechamentoBetween(Quiosque quiosque, Comanda.StatusComanda status, LocalDateTime start, LocalDateTime end);

    long countByAtendenteAndStatusIn(Atendente atendente, List<Comanda.StatusComanda> statuses);

    long countByAtendenteAndStatusAndDataFechamentoBetween(Atendente atendente, Comanda.StatusComanda status, LocalDateTime start, LocalDateTime end);

    // Busca os itens mais vendidos
    @Query(value = "SELECT p.nome as nome, SUM(ip.quantidade) as qtd " +
                   "FROM tb_itens_pedido ip " +
                   "JOIN tb_produtos p ON ip.produto_id = p.id " +
                   "JOIN tb_comandas c ON ip.comanda_id = c.id " +
                   "WHERE c.quiosque_id = :quiosqueId " +
                   "AND c.status = 'FECHADA' " +
                   "AND c.data_fechamento BETWEEN :inicio AND :fim " +
                   "GROUP BY p.nome " +
                   "ORDER BY qtd DESC " +
                   "LIMIT 4", 
                   nativeQuery = true)
    List<TopItemProjection> findTopVendidosHoje(@Param("quiosqueId") Long quiosqueId, 
                                            @Param("inicio") LocalDateTime inicio, 
                                            @Param("fim") LocalDateTime fim);

}

