package com.seashade.api_seashade.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seashade.api_seashade.model.Despesa;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    // Busca despesas por quiosque e período
    List<Despesa> findByQuiosqueIdAndDataDespesaBetween(Long quiosqueId, LocalDate inicio, LocalDate fim);

    // Busca despesas por quiosque, período e categoria
    List<Despesa> findByQuiosqueIdAndCategoriaAndDataDespesaBetween(Long quiosqueId, String categoria, LocalDate inicio, LocalDate fim);
}
