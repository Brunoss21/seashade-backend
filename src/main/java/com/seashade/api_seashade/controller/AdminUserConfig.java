package com.seashade.api_seashade.controller;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.seashade.api_seashade.model.Role;
import com.seashade.api_seashade.model.User;
import com.seashade.api_seashade.repository.RoleRepository;
import com.seashade.api_seashade.repository.UserRepository;

import jakarta.transaction.Transactional;

@Configuration
public class AdminUserConfig implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserConfig(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        // Busca pelas roles necessárias na inicialização
        var roleAdmin = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
        var roleBasic = roleRepository.findByName("BASIC").orElseGet(() -> roleRepository.save(new Role("BASIC")));

        // Verifica se o usuário admin já existe
        var userAdminOptional = userRepository.findByEmail("admin@seashade.com");

        // Cria o admin apenas se ele não existir
        userAdminOptional.ifPresentOrElse(
                user -> System.out.println("Admin já existente."),
                () -> {
                    var user = new User();
                    user.setName("Administrador");
                    user.setEmail("admin@seashade.com");
                    user.setPassword(passwordEncoder.encode("0969"));
                    user.setRoles(Set.of(roleAdmin));
                    userRepository.save(user);
                    System.out.println("Usuário admin criado com sucesso.");
                }
        );
    }
}