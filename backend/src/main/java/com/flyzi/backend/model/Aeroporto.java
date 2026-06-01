package com.flyzi.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "aeroportos")
public class Aeroporto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String iata;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cidade;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String pais;

    // ============================================================
    // CONSTRUTORES
    // ============================================================

    public Aeroporto() {
    }

    public Aeroporto(String iata, String nome, String cidade, String estado) {
        this.iata = iata;
        this.nome = nome;
        this.cidade = cidade;
        this.estado = estado;
        this.pais = "Brasil";
    }

    public Aeroporto(String iata, String nome, String cidade, String estado, String pais) {
        this.iata = iata;
        this.nome = nome;
        this.cidade = cidade;
        this.estado = estado;
        this.pais = pais;
    }

    // ============================================================
    // GETTERS E SETTERS
    // ============================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    // ============================================================
    // TO STRING
    // ============================================================

    @Override
    public String toString() {
        return iata + " - " + nome + " (" + cidade + ", " + estado + ")";
    }
}