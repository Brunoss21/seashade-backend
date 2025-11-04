package com.seashade.api_seashade.model;

import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_itens_estoque")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ItemEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private String unidadeMedida; // Ex: "kg", "L", "unidade"

    @Column(name = "quantidade_atual", nullable = false)
    private BigDecimal quantidadeAtual;

    @Column(name = "estoque_maximo")
    private Integer estoqueMaximo;

    @Column(name = "custo_unitario")
    private BigDecimal custoUnitario; // Custo por kg/L/unidade

    @Column(nullable = false)
    private boolean ativo = true; // Todo item começa como ativo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiosque_id", nullable = false)
    @JsonBackReference
    private Quiosque quiosque;

    @JsonIgnore
    @OneToMany(mappedBy = "itemEstoque")
    private Set<ComponenteProduto> usosDoComponente;

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
    public BigDecimal getQuantidadeAtual() { return quantidadeAtual; }
    public void setQuantidadeAtual(BigDecimal quantidadeAtual) { this.quantidadeAtual = quantidadeAtual; }
    public BigDecimal getCustoUnitario() { return custoUnitario; }
    public void setCustoUnitario(BigDecimal custoUnitario) { this.custoUnitario = custoUnitario; }
    public Quiosque getQuiosque() { return quiosque; }
    public void setQuiosque(Quiosque quiosque) { this.quiosque = quiosque; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Integer getEstoqueMaximo() { return estoqueMaximo; }
    public void setEstoqueMaximo(Integer estoqueMaximo) { this.estoqueMaximo = estoqueMaximo; }
    public Set<ComponenteProduto> getUsosDoComponente() { return usosDoComponente; }
    public void setUsosDoComponente(Set<ComponenteProduto> usosDoComponente) { this.usosDoComponente = usosDoComponente; }
}
