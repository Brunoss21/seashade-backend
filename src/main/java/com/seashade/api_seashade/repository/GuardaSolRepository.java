package com.seashade.api_seashade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seashade.api_seashade.model.GuardaSol;
import com.seashade.api_seashade.model.Quiosque;

public interface GuardaSolRepository extends JpaRepository<GuardaSol, Long> {

    // Método para buscar todos os guarda-sóis de um quiosque
    List<GuardaSol> findByQuiosque(Quiosque quiosque);

    // Método para buscar apenas os guarda-sóis livres ou ocupados de um quiosque
    List<GuardaSol> findByQuiosqueAndStatus(Quiosque quiosque, GuardaSol.StatusGuardaSol status);

}
