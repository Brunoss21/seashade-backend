package com.seashade.api_seashade.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seashade.api_seashade.controller.dto.CreateUserDto;
import com.seashade.api_seashade.controller.dto.UserResponseDto;
import com.seashade.api_seashade.service.UserService;

@RestController
@RequestMapping("/api/auth") 
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> newUser(@RequestBody CreateUserDto dto) {
        UserResponseDto responseDto = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    
}