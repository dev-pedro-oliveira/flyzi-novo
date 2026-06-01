package com.flyzi.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "voos")
public class Voo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origem;
    private String destino;
    private String descricaoRota;
    private String data;
    private String dataISO;
    private String horario;
    private String duracaoTexto;
    private Integer duracaoMinutos;
    private String tipo;
    private Double preco;
    private String companhia;
    private String classeCor;
    private Integer milhasNum;
    private String milhasFormatado;
    private Boolean teveQueda;
    private String continente;
    private String categoria;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public String getDescricaoRota() { return descricaoRota; }
    public void setDescricaoRota(String descricaoRota) { this.descricaoRota = descricaoRota; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getDataISO() { return dataISO; }
    public void setDataISO(String dataISO) { this.dataISO = dataISO; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public String getDuracaoTexto() { return duracaoTexto; }
    public void setDuracaoTexto(String duracaoTexto) { this.duracaoTexto = duracaoTexto; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }
    public String getCompanhia() { return companhia; }
    public void setCompanhia(String companhia) { this.companhia = companhia; }
    public String getClasseCor() { return classeCor; }
    public void setClasseCor(String classeCor) { this.classeCor = classeCor; }
    public Integer getMilhasNum() { return milhasNum; }
    public void setMilhasNum(Integer milhasNum) { this.milhasNum = milhasNum; }
    public String getMilhasFormatado() { return milhasFormatado; }
    public void setMilhasFormatado(String milhasFormatado) { this.milhasFormatado = milhasFormatado; }
    public Boolean getTeveQueda() { return teveQueda; }
    public void setTeveQueda(Boolean teveQueda) { this.teveQueda = teveQueda; }
    public String getContinente() { return continente; }
    public void setContinente(String continente) { this.continente = continente; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
