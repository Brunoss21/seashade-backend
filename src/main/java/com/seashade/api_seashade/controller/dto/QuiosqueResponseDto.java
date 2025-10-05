package com.seashade.api_seashade.controller.dto;

import java.util.UUID;

import com.seashade.api_seashade.model.Quiosque;

public class QuiosqueResponseDto {

    private UUID quiosqueId;
    private String name;

    public QuiosqueResponseDto(Quiosque quiosque) {
        this.quiosqueId = quiosque.getQuiosqueId();
        this.name = quiosque.getName();
    }

    public UUID getQuiosqueId() {
        return quiosqueId;
    }

    public String getName() {
        return name;
    }
    


}