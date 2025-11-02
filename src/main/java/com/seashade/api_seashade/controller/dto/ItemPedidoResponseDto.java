package com.seashade.api_seashade.controller.dto;

import java.math.BigDecimal;
import com.seashade.api_seashade.model.ItemPedido;
import com.seashade.api_seashade.model.StatusItem; 

public record ItemPedidoResponseDto(
    Long id,
    Integer quantidade,
    BigDecimal precoUnitario,
    Long produtoId, 
    String produtoNome,
    StatusItem status 
) {
    public ItemPedidoResponseDto(ItemPedido item) {
        this(
            item.getId(),
            item.getQuantidade(),
            item.getPrecoUnitario(),
            item.getProduto() != null ? item.getProduto().getId() : null,
            item.getProduto() != null ? item.getProduto().getNome() : null,
            item.getStatus() 
        );
    }
}
