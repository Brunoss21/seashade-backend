package com.seashade.api_seashade.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seashade.api_seashade.model.Atendente;
import com.seashade.api_seashade.model.Quiosque;

public interface AtendenteRepository extends JpaRepository<Atendente, Long> {

    List<Atendente> findByQuiosque(Quiosque quiosque);

}
