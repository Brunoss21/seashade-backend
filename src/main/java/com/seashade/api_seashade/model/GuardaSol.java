package com.seashade.api_seashade.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_guarda_sois")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GuardaSol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false)
    private String identificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusGuardaSol status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiosque_id", nullable = false)
    private Quiosque quiosque;

    public enum StatusGuardaSol {
        LIVRE,
        OCUPADO
    }

    // --- Getters e Setters ---
    public Long getId() { return id; } 
    public void setId(Long id) { this.id = id; } 
    public String getIdentificacao() { return identificacao; }
    public void setIdentificacao(String identificacao) { this.identificacao = identificacao; }
    public StatusGuardaSol getStatus() { return status; }
    public void setStatus(StatusGuardaSol status) { this.status = status; }
    public Quiosque getQuiosque() { return quiosque; }
    public void setQuiosque(Quiosque quiosque) { this.quiosque = quiosque; }

}
