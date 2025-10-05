package com.seashade.api_seashade.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.seashade.api_seashade.model.Quiosque;
import com.seashade.api_seashade.model.User;
import com.seashade.api_seashade.repository.QuiosqueRepository;
import com.seashade.api_seashade.repository.UserRepository;
import jakarta.transaction.Transactional;
import com.seashade.api_seashade.controller.dto.CreateUserDto;
import com.seashade.api_seashade.model.Role;
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
    public User registerUser(CreateUserDto dto) {
        var userFromDb = userRepository.findByEmail(dto.email());
        if (userFromDb.isPresent()) {
            throw new RuntimeException("Usuário com este e-mail já existe.");
        }
        
        // 1. Criar e salvar o User para que ele tenha um ID
        var user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        user.setRoles(Set.of(basicRole));

        var savedUser = userRepository.save(user);

        // 2. Criar o Quiosque e associá-lo ao User que já tem um ID
        var quiosque = new Quiosque(dto.quiosque(), savedUser);
        quiosque.setName(dto.quiosque()); 
        quiosque.setUser(savedUser); // Associação com o usuário já persistido
        
        // 3. Salvar o Quiosque
        quiosqueRepository.save(quiosque);

        // 4. (Opcional, mas boa prática) Atualizar a referência bidirecional no User
        savedUser.setQuiosque(quiosque);
        
        return savedUser;
    }
}
/*
@Service
public class UserService {

    private final QuiosqueRepository quiosqueRepository;

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(BCryptPasswordEncoder passwordEncoder, UserRepository userRepository, QuiosqueRepository quiosqueRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.quiosqueRepository = quiosqueRepository;
    }

    @Transactional
    public User createUser(User user) {
        // Encriptando a senha antes de salvar
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        Quiosque quiosque = new Quiosque();
        quiosque.setName("Quiosque do " );

        quiosqueRepository.save(quiosque);

        savedUser.setQuiosque(quiosque);

        return savedUser;

    }
}
    */
