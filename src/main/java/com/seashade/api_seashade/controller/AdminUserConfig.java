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

    private final PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;
    private UserRepository userRepository;


    
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
        
        var roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());

        var userAdmin = userRepository.findByEmail("admin@seashade.com");

        userAdmin.ifPresentOrElse(
                user -> {
                    System.out.println("admin já existente");
                },
                () -> {
                    var user = new User();
                    user.setName("Administrador");
                    user.setEmail("admin@seashade.com");
                    user.setPassword(passwordEncoder.encode("0969"));
                    user.setRoles(Set.of(roleAdmin));
                    userRepository.save(user);


                }
        );
      
    }


}
