package com.seashade.api_seashade.controller.dto;

import com.seashade.api_seashade.model.Role;
import com.seashade.api_seashade.model.User;

import java.util.Set;
import java.util.UUID;


public class UserResponseDto {

    private UUID userId;
    private String name;
    private String email;
    private QuiosqueResponseDto quiosque;
    private Set<Role> roles;

    public UserResponseDto(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        //this.quiosque = new QuiosqueResponseDto(user.getQuiosque());
        if (user.getQuiosque() != null) {
            this.quiosque = new QuiosqueResponseDto(user.getQuiosque());
        }
        this.roles = user.getRoles(); 
        
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

     public QuiosqueResponseDto getQuiosque() {
        return quiosque;
    }

    public Set<Role> getRoles() { return roles; }

}
