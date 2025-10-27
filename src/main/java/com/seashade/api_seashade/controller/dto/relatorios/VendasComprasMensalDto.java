package com.seashade.api_seashade.controller.dto.relatorios;

import java.math.BigDecimal;

public record VendasComprasMensalDto(
        String mes, 
        long vendas,  
        BigDecimal compras){
}
