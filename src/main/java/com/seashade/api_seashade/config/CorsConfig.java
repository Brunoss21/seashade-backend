package com.seashade.api_seashade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Aplica a todos os endpoints que começam com /api
                    .allowedOrigins("https://seashadefront-1.onrender.com") // Permite requisições aplicação React
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                    .allowedHeaders("*") // Permite todos os cabeçalhos
                    .allowCredentials(true); // Permite o envio de credenciais (cookies, etc.)
            }
        };
    }
}
