package com.seashade.api_seashade.model;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "tb_quiosques")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Quiosque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; 

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonBackReference
    private User user;

    public Quiosque(String name, User user){
        this.name = name;
        this.user = user;

    }

    @OneToMany(mappedBy = "quiosque", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Atendente> atendentes = new ArrayList<>();

    @OneToMany(mappedBy = "quiosque", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference 
    private List<Produto> produtos = new ArrayList<>();

    @OneToMany(mappedBy = "quiosque", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemEstoque> itensEstoque = new ArrayList<>();

    public List<ItemEstoque> getItensEstoque() {
        return itensEstoque;
    }
    public void setItensEstoque(List<ItemEstoque> itensEstoque) {
        this.itensEstoque = itensEstoque;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public Quiosque() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Atendente> getAtendentes() {
        return atendentes;
    }
    
    public void setAtendentes(List<Atendente> atendentes) {
        this.atendentes = atendentes;
    }

    public Long getQuiosqueId() {
    return this.id;
    }
   
    
}
