package com.seashade.api_seashade.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.seashade.api_seashade.controller.dto.CreateUserDto;
import com.seashade.api_seashade.controller.dto.UserResponseDto;
import com.seashade.api_seashade.repository.UserRepository;
import com.seashade.api_seashade.service.UserService;

/*
@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> newUser(@RequestBody CreateUserDto dto) {

        UserResponseDto responseDto = userService.registerUser(dto); 
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
*/

@RestController
@RequestMapping("/api/users") // Nova rota base para operações de usuário autenticado
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

