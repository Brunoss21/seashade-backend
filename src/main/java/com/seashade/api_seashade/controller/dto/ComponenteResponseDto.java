package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal;
import com.seashade.api_seashade.model.ComponenteProduto;

// DTO para EXIBIR um ingrediente da receita no modal
public record ComponenteResponseDto(
    Long id,                 
    String nomeIngrediente,
    String unidadeMedida,
    BigDecimal quantidadeUtilizada
) {
    // Construtor que transforma a Entidade no DTO
    public ComponenteResponseDto(ComponenteProduto comp) {
        this(
            comp.getId(),
            comp.getItemEstoque().getNome(), // nome do ingrediente
            comp.getItemEstoque().getUnidadeMedida(), // unidade (ex: "kg")
            comp.getQuantidadeUtilizada()
        );
    }
}
