package com.seashade.api_seashade.repository;

// Interface para mapear os resultados da query nativa de baixa saída
public interface BottomItemProjection {
    String getNome();
    Long getQtdVendida(); 
}
