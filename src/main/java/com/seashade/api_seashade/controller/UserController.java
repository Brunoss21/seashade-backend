package com.seashade.api_seashade.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.seashade.api_seashade.controller.dto.UserResponseDto;
import com.seashade.api_seashade.model.User;
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
    // 1. Pega o ID do usuário (UUID como String) a partir do token JWT
    String userIdString = SecurityContextHolder.getContext().getAuthentication().getName();
    
    // 2. Converte a String do ID para o tipo UUID
    UUID userId = UUID.fromString(userIdString);

    // 3. Busca o usuário no banco de dados PELO ID
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

    // Retorna os dados do usuário em um DTO
    return ResponseEntity.ok(new UserResponseDto(user));
}
}

