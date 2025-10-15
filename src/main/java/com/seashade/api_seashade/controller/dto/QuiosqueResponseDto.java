package com.seashade.api_seashade.controller.dto;

import com.seashade.api_seashade.model.Quiosque;

public class QuiosqueResponseDto {

    private Long quiosqueId;
    private String name;

    public QuiosqueResponseDto(Quiosque quiosque) {
        this.quiosqueId = quiosque.getQuiosqueId();
        this.name = quiosque.getName();
    }

    public Long getQuiosqueId() {
        return quiosqueId;
    }

    public String getName() {
        return name;
    }
    


}