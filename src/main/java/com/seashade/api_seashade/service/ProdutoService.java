package com.seashade.api_seashade.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.seashade.api_seashade.controller.dto.ProdutoResponseDto;
import com.seashade.api_seashade.model.Produto;
import com.seashade.api_seashade.model.Quiosque;
import com.seashade.api_seashade.repository.ProdutoRepository;
import com.seashade.api_seashade.repository.QuiosqueRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final QuiosqueRepository quiosqueRepository;

     public ProdutoService(ProdutoRepository produtoRepository, QuiosqueRepository quiosqueRepository) {
        this.produtoRepository = produtoRepository;
        this.quiosqueRepository = quiosqueRepository;
    }

    /*public List<Produto> listarProdutosPorQuiosque(Long quiosqueId){
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));
        return produtoRepository.findByQuiosque(quiosque);
    }*/

    public List<ProdutoResponseDto> listarProdutosPorQuiosque(Long quiosqueId) {
        List<Produto> entidades = produtoRepository.findByQuiosqueIdAndAtivoTrue(quiosqueId);
            return entidades.stream()
            .map(ProdutoResponseDto::new)
            .collect(Collectors.toList());
    }
    
    public Optional<Produto> buscarProdutoPorId(Long produtoId) {
        return produtoRepository.findById(produtoId);
    }

    // CRIAR um novo produto
    @Transactional
    public Produto criarProduto(Long quiosqueId, String nome, String descricao, BigDecimal preco, Integer estoque, String categoria) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));

        Produto novoProduto = new Produto();
        novoProduto.setNome(nome);
        novoProduto.setDescricao(descricao);
        novoProduto.setPreco(preco);
        novoProduto.setEstoque(estoque);
        novoProduto.setQuiosque(quiosque);
        novoProduto.setCategoria(Produto.CategoriaProduto.valueOf(categoria));

        return produtoRepository.save(novoProduto);
    }

    @Transactional
    public Produto atualizarProduto(Long produtoId, String nome, String descricao, BigDecimal preco, Integer estoque) {
    Produto produtoExistente = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    produtoExistente.setNome(nome);
    produtoExistente.setDescricao(descricao);
    produtoExistente.setPreco(preco);
    produtoExistente.setEstoque(estoque);

    return produtoRepository.save(produtoExistente);
    }

    @Transactional
    public void deletarProduto(Long produtoId) {
    Produto produtoExistente = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + produtoId));
    
    produtoRepository.delete(produtoExistente);
    }

    @Transactional
    public void desativarProduto(Long produtoId) {
        // Busca o produto
        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + produtoId));

        // Define o produto como 'inativo'
        produto.setAtivo(false);

        produtoRepository.save(produto);
    }

}
