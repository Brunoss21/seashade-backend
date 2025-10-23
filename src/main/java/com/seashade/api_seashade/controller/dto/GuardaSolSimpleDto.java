package com.seashade.api_seashade.controller.dto;

import com.seashade.api_seashade.model.GuardaSol;

public record GuardaSolSimpleDto(
    Long id,
    String identificacao
) {
    public GuardaSolSimpleDto(GuardaSol gs) {
        this(gs.getId(), gs.getIdentificacao());
    }
}
