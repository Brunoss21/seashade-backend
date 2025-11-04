package com.seashade.api_seashade.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seashade.api_seashade.controller.dto.CreateUpdateProdutoDto;
import com.seashade.api_seashade.controller.dto.ProdutoResponseDto;
import com.seashade.api_seashade.model.Produto;
import com.seashade.api_seashade.service.ProdutoService;

@RestController
@RequestMapping("/api/quiosques/{quiosqueId}/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // Endpoint para LISTAR todos os produtos de um quiosque (o cardápio)
    // GET /api/quiosques/{id-do-quiosque}/produtos
    @GetMapping
    public ResponseEntity<List<ProdutoResponseDto>> getProdutosDoQuiosque(@PathVariable Long quiosqueId) {
        // Agora ele retorna o DTO "limpo", sem loop
        List<ProdutoResponseDto> produtos = produtoService.listarProdutosPorQuiosque(quiosqueId);
        return ResponseEntity.ok(produtos);
    }
    
    // Endpoint para CRIAR um novo produto para um quiosque
    // POST /api/quiosques/{id-do-quiosque}/produtos
    @PostMapping
    public ResponseEntity<Produto> criarProduto(@PathVariable Long quiosqueId, @RequestBody CreateUpdateProdutoDto dto) {
        Produto novoProduto = produtoService.criarProduto(
            quiosqueId,
            dto.nome(),
            dto.descricao(),
            dto.preco(),
            dto.estoque(),
            dto.categoria()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    // Endpoint para BUSCAR um produto específico pelo seu ID
    // GET /api/quiosques/{id-do-quiosque}/produtos/{id-do-produto}
    @GetMapping("/{produtoId}")
    public ResponseEntity<Optional<Produto>> buscarProdutoPorId(@PathVariable UUID quiosqueId, @PathVariable Long produtoId) {
        Optional<Produto> produto = produtoService.buscarProdutoPorId(produtoId);
        return ResponseEntity.ok(produto);
    }

    // Endpoint para ATUALIZAR um produto específico
    // PUT /api/quiosques/{id-do-quiosque}/produtos/{id-do-produto}
    @PutMapping("/{produtoId}")
    public ResponseEntity<Produto> atualizarProduto(@PathVariable Long quiosqueId, 
                                                    @PathVariable Long produtoId, 
                                                    @RequestBody CreateUpdateProdutoDto dto) {
        Produto produtoAtualizado = produtoService.atualizarProduto(
            produtoId,
            dto.nome(),
            dto.descricao(),
            dto.preco(),
            dto.estoque()

        );
        return ResponseEntity.ok(produtoAtualizado);
    }

    @PatchMapping("/{produtoId}/desativar")
    public ResponseEntity<Void> desativarProduto(@PathVariable Long produtoId) {
        produtoService.desativarProduto(produtoId);
        return ResponseEntity.noContent().build(); 
    }
}
