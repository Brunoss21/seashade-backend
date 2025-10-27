package com.seashade.api_seashade.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 

import com.seashade.api_seashade.controller.dto.CreateItemComMovimentoDto;
import com.seashade.api_seashade.controller.dto.CreateItemEstoqueDto;
import com.seashade.api_seashade.controller.dto.CreateMovimentoDto;
import com.seashade.api_seashade.model.ItemEstoque;
import com.seashade.api_seashade.model.MovimentoEstoque;
import com.seashade.api_seashade.model.User;
import com.seashade.api_seashade.repository.UserRepository;
import com.seashade.api_seashade.service.EstoqueService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/quiosques/{quiosqueId}/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(EstoqueController.class);

    public EstoqueController(EstoqueService estoqueService, UserRepository userRepository) {
        this.estoqueService = estoqueService;
        this.userRepository = userRepository;
    }

    // Endpoint para LISTAR todos os itens do estoque (para a aba "Lista de produtos")
    @GetMapping
    public ResponseEntity<List<ItemEstoque>> listarEstoque(@PathVariable Long quiosqueId) {
        List<ItemEstoque> itens = estoqueService.listarItensEstoque(quiosqueId);
        return ResponseEntity.ok(itens);
    }

    // Endpoint para REGISTRAR uma nova movimentação 
    @PostMapping("/movimentacoes")
    public ResponseEntity<MovimentoEstoque> registrarMovimentacao(@PathVariable Long quiosqueId, @RequestBody CreateMovimentoDto dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName(); 

        UUID usuarioId; 
        try {
            usuarioId = UUID.fromString(userIdString); 
        } catch (IllegalArgumentException e) {
            logger.error("ID do usuário no token não é um UUID válido: '{}'", userIdString);
            throw new BadCredentialsException("Token de usuário inválido.");
        }

        logger.info("Tentando encontrar usuário pelo ID do token para movimentação: '{}'", usuarioId);

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário do token (ID: " + usuarioId + ") não encontrado no banco"));
     
        MovimentoEstoque novoMovimento = estoqueService.registrarMovimentacao(
                dto.itemEstoqueId(),
                dto.tipoMovimento(),
                dto.quantidade(),
                dto.motivo(),
                dto.observacao(),
                usuarioId 
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(novoMovimento);
    }
    
    // Endpoint para LISTAR o histórico de movimentações (aba "Histórico")
    @GetMapping("/historico")
    public ResponseEntity<List<MovimentoEstoque>> getHistorico(@PathVariable Long quiosqueId) {
        List<MovimentoEstoque> historico = estoqueService.listarHistoricoMovimentacoes(quiosqueId);
        return ResponseEntity.ok(historico);
    }

    @PostMapping
    public ResponseEntity<ItemEstoque> criarItemEstoque(@PathVariable Long quiosqueId, @RequestBody CreateItemEstoqueDto dto) {
        ItemEstoque novoItem = estoqueService.criarItemEstoque(quiosqueId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoItem);
    }

    @PostMapping("/novo-com-movimento")
    public ResponseEntity<MovimentoEstoque> criarItemComMovimento(@PathVariable Long quiosqueId, @RequestBody CreateItemComMovimentoDto dto) {
        String userIdString = SecurityContextHolder.getContext().getAuthentication().getName(); 

        UUID usuarioId; 
        try {
            usuarioId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            logger.error("ID do usuário no token não é um UUID válido: '{}'", userIdString);
            throw new BadCredentialsException("Token de usuário inválido."); 
        }

        logger.info("Tentando encontrar usuário pelo ID do token: '{}'", usuarioId);

        User usuario = userRepository.findById(usuarioId) 
                .orElseThrow(() -> new EntityNotFoundException("Usuário do token (ID: " + usuarioId + ") não encontrado no banco"));

        MovimentoEstoque movimentoInicial = estoqueService.criarItemComPrimeiroMovimento(quiosqueId, dto, usuarioId); 

        return ResponseEntity.status(HttpStatus.CREATED).body(movimentoInicial);
    }

    @PatchMapping("/{itemEstoqueId}/desativar")
    public ResponseEntity<Void> desativarItemEstoque(@PathVariable Long quiosqueId, @PathVariable Long itemEstoqueId) {
        estoqueService.desativarItemEstoque(itemEstoqueId);
        return ResponseEntity.noContent().build();
    }
}
