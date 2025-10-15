package com.seashade.api_seashade.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice 
public class GlobalExceptionHandler {

    // Logger para imprimir a exceção completa no console
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Método para capturar qualquer exceção genérica que não foi tratada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        
        logger.error("Uma exceção não tratada ocorreu!", ex);

        Map<String, String> response = Map.of(
            "message", "Ocorreu um erro interno inesperado no servidor.",
            "error", ex.getClass().getName() // Nos diz o tipo exato da exceção
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}