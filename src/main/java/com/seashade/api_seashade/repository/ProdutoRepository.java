package com.seashade.api_seashade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.seashade.api_seashade.model.Produto;
import com.seashade.api_seashade.model.Quiosque;

import java.time.LocalDateTime;
import java.util.List;


public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByQuiosque(Quiosque quiosque);

    // Query produtos com menor saida
    @Query(value = "SELECT p.nome as nome, COALESCE(SUM(ip.quantidade), 0) as qtdVendida " +
                   "FROM tb_produtos p " +
                   "LEFT JOIN tb_itens_pedido ip ON p.id = ip.produto_id " +
                   "LEFT JOIN tb_comandas c ON ip.comanda_id = c.id " +
                   "    AND c.status = 'FECHADA' " +
                   "    AND c.data_fechamento BETWEEN :inicio AND :fim " +
                   "WHERE p.quiosque_id = :quiosqueId " +
                   "GROUP BY p.id, p.nome " + 
                   "ORDER BY qtdVendida ASC " + 
                   "LIMIT 3",
                   nativeQuery = true)
    List<BottomItemProjection> findBottomVendidosHoje(@Param("quiosqueId") Long quiosqueId,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim);

}
