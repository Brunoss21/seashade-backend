package com.seashade.api_seashade.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.seashade.api_seashade.model.Produto;
import com.seashade.api_seashade.model.Quiosque;

import java.util.List;


public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByQuiosque(Quiosque quiosque);

}
