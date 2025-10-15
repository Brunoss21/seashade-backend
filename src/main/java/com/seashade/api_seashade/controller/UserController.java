package com.seashade.api_seashade.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.seashade.api_seashade.controller.dto.UserResponseDto;
import com.seashade.api_seashade.repository.UserRepository;


@RestController
@RequestMapping("/api/users") 
public class UserController {

    private final UserRepository userRepository;


    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile() {
        // Pega o email do usuário a partir do token JWT que foi validado pelo Spring Security
        var userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Busca o usuário completo no banco de dados
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        // Retorna os dados do usuário em um DTO
        return ResponseEntity.ok(new UserResponseDto(user));
    }
}

