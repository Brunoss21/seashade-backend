package com.seashade.api_seashade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}
