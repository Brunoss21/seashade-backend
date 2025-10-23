package com.seashade.api_seashade.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.seashade.api_seashade.model.Quiosque;
import com.seashade.api_seashade.model.User;
import com.seashade.api_seashade.repository.QuiosqueRepository;
import com.seashade.api_seashade.repository.UserRepository;
import jakarta.transaction.Transactional;
import com.seashade.api_seashade.controller.dto.CreateUserDto;
import com.seashade.api_seashade.controller.dto.UserResponseDto;
import com.seashade.api_seashade.repository.RoleRepository;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final QuiosqueRepository quiosqueRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, QuiosqueRepository quiosqueRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.quiosqueRepository = quiosqueRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDto registerUser(CreateUserDto dto) { 
        var userFromDb = userRepository.findByEmail(dto.email());
        if (userFromDb.isPresent()) {
            throw new RuntimeException("Usuário com este e-mail já existe.");
        }
        
        var user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        /*var basicRole = roleRepository.findByName(Role.Values.BASIC.name());*/
        var basicRole = roleRepository.findByName("BASIC")
            .orElseThrow(() -> new RuntimeException("Erro: Role 'BASIC' não encontrada.")); // <-- CORREÇÃO
            user.setRoles(Set.of(basicRole));
        var savedUser = userRepository.save(user);

        var quiosque = new Quiosque(dto.quiosque(), savedUser);
        quiosqueRepository.save(quiosque);
        
        savedUser.setQuiosque(quiosque);

        return new UserResponseDto(savedUser); 
    }
}
