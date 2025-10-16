package com.seashade.api_seashade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.seashade.api_seashade.model.ItemEstoque;
import com.seashade.api_seashade.model.MovimentoEstoque;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

    List<MovimentoEstoque> findByItemEstoqueOrderByDataMovimentoDesc(ItemEstoque itemEstoque);

    @Query("SELECT m FROM MovimentoEstoque m WHERE m.itemEstoque.quiosque.id = :quiosqueId ORDER BY m.dataMovimento DESC")
    List<MovimentoEstoque> findMovimentosByQuiosqueId(@Param("quiosqueId") Long quiosqueId);

}

