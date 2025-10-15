package com.seashade.api_seashade.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seashade.api_seashade.model.Quiosque;

@Repository
public interface QuiosqueRepository extends JpaRepository<Quiosque, Long> {

}
