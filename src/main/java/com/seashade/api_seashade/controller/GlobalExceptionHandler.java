package com.seashade.api_seashade.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice // Esta anotação torna a classe um "apanhador" global de exceções
public class GlobalExceptionHandler {

    // Cria um logger para imprimir a exceção completa no console
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Este método vai "pegar" qualquer exceção genérica que não foi tratada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        
        // IMPRIME O ERRO COMPLETO NO CONSOLE DO SPRING BOOT - ESTA É A PARTE MAIS IMPORTANTE
        logger.error("Uma exceção não tratada ocorreu!", ex);

        // Cria uma resposta JSON clara para o frontend
        Map<String, String> response = Map.of(
            "message", "Ocorreu um erro interno inesperado no servidor.",
            "error", ex.getClass().getName() // Nos diz o tipo exato da exceção
        );
        
        // Retorna um status 500 Internal Server Error
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}