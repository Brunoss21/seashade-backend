package com.seashade.api_seashade.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_comandas")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Comanda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 

    @Column(name = "numero_comanda", unique = true)
    private String numeroComanda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guarda_sol_id", nullable = false)
    private GuardaSol guardaSol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiosque_id", nullable = false)
    private Quiosque quiosque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendente_id", nullable = true) 
    private Atendente atendente;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    private LocalDateTime dataFechamento;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusComanda status;

    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemPedido> itens = new ArrayList<>();

    public enum StatusComanda {
        ABERTA,           // Garçom adicionando itens
        NA_COZINHA,       // Enviado para cozinha, aguardando início
        EM_PREPARO,       // Cozinha iniciou o preparo
        PRONTO_PARA_ENTREGA, // Cozinha finalizou, aguardando entrega do garçom
        FECHADA,          // Cliente pagou
        CANCELADA         // Pedido cancelado
    }

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroComanda() { return numeroComanda; }
    public void setNumeroComanda(String numeroComanda) { this.numeroComanda = numeroComanda; }
    public GuardaSol getGuardaSol() { return guardaSol; }
    public void setGuardaSol(GuardaSol guardaSol) { this.guardaSol = guardaSol; }
    public Quiosque getQuiosque() { return quiosque; }
    public void setQuiosque(Quiosque quiosque) { this.quiosque = quiosque; }
    public Atendente getAtendente() { return atendente; }
    public void setAtendente(Atendente atendente) { this.atendente = atendente; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }
    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }
    public StatusComanda getStatus() { return status; }
    public void setStatus(StatusComanda status) { this.status = status; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }

}
