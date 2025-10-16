package com.seashade.api_seashade.controller;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.seashade.api_seashade.controller.dto.LoginRequest;
import com.seashade.api_seashade.controller.dto.LoginResponse;


@RestController
public class TokenController {

    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager authenticationManager; 

    public TokenController(JwtEncoder jwtEncoder, AuthenticationManager authenticationManager){
        this.jwtEncoder = jwtEncoder;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){

        var usernamePassword = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var now = Instant.now();
        var expiresIn = 18000L;

        var claims = JwtClaimsSet.builder()
            .issuer("seashade")
            .subject(auth.getName()) 
            // Adicionar outras claims aqui, como as roles depois
            .claim("scope", auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" ")))
            .expiresAt(now.plusSeconds(expiresIn))
            .issuedAt(now)
            .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return ResponseEntity.ok(new LoginResponse(jwtValue, expiresIn));
    }
}
