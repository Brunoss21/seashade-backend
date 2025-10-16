package com.seashade.api_seashade.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seashade.api_seashade.model.ItemEstoque;
import com.seashade.api_seashade.model.Quiosque;

import java.util.List;


public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {

    List<ItemEstoque> findByQuiosque(Quiosque quiosque);
}
