package com.seashade.api_seashade.controller;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.seashade.api_seashade.controller.dto.AtendenteLoginRequestDto;
import com.seashade.api_seashade.controller.dto.LoginRequest;
import com.seashade.api_seashade.controller.dto.LoginResponse;
import com.seashade.api_seashade.model.Atendente;
import com.seashade.api_seashade.repository.AtendenteRepository;
import com.seashade.api_seashade.repository.UserRepository;

@RestController
public class TokenController {

    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AtendenteRepository atendenteRepository; 

    // 2. ATUALIZAR O CONSTRUTOR
    public TokenController(JwtEncoder jwtEncoder,
                           UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           AtendenteRepository atendenteRepository) { 
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.atendenteRepository = atendenteRepository; 
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> loginUsuario(@RequestBody LoginRequest loginRequest) {
        var user = userRepository.findByEmail(loginRequest.email());

        if (user.isEmpty() || !user.get().isLoginCorrent(loginRequest, bCryptPasswordEncoder)) {
            throw new BadCredentialsException("Usuário ou senha inválidos!");
        }

        var now = Instant.now();
        var expiresIn = 3600L; 

        String scopes = user.get().getRoles().stream()
                            .map(role -> role.getName())
                            .collect(Collectors.joining(" "));

        var claims = JwtClaimsSet.builder()
                .issuer("seashade")
                .subject(user.get().getUserId().toString()) // Subject é o ID do User
                .claim("scope", scopes) 
                .expiresAt(now.plusSeconds(expiresIn))
                .issuedAt(now)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return ResponseEntity.ok(new LoginResponse(jwtValue, expiresIn, jwtValue, jwtValue, expiresIn));
    }

    @PostMapping("/api/atendentes/login")
    public ResponseEntity<LoginResponse> loginAtendente(@RequestBody AtendenteLoginRequestDto loginRequest) {
        var atendenteOpt = atendenteRepository.findByCodigo(loginRequest.codigo());
        if (atendenteOpt.isEmpty()) {
            throw new BadCredentialsException("Código de acesso inválido!");
        }
        Atendente atendente = atendenteOpt.get();

        var now = Instant.now();
        var expiresIn = 3600L; 

        var claims = JwtClaimsSet.builder()
                .claim("quiosqueId", atendente.getQuiosque().getId()) 
                .expiresAt(now.plusSeconds(expiresIn))
                .issuedAt(now)
                .build();
        
        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        Long quiosqueIdDoAtendente = atendente.getQuiosque().getId();
        
        return ResponseEntity.ok(new LoginResponse(
            jwtValue, 
            expiresIn, 
            atendente.getNome(), 
            "ATENDENTE", 
            quiosqueIdDoAtendente 
        ));
    }
}
