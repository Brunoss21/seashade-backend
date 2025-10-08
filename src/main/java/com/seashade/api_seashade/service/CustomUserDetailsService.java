package com.seashade.api_seashade.service;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.seashade.api_seashade.model.User;
import com.seashade.api_seashade.repository.UserRepository;

@Service // <-- MUITO IMPORTANTE: Transforma a classe em um Bean do Spring
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // O "username" que o Spring Security usa aqui é, no nosso caso, o e-mail
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + username));
        
        // Converte as nossas Roles para o formato que o Spring Security entende (GrantedAuthority)
        var authorities = user.getRoles()
                              .stream()
                              .map(role -> new SimpleGrantedAuthority(role.getName()))
                              .collect(Collectors.toSet());

        // Retorna um objeto UserDetails que o Spring Security usa para a autenticação
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities // As permissões do usuário
        );
    }
}
