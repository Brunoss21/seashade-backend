package com.seashade.api_seashade.controller.dto;

public record LoginResponse(String accessToken, Long ExpiresIn, String userName, String userRole, Long quiosqueId) {

}
