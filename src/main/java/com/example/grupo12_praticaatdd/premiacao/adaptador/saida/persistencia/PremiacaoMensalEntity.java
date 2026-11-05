package com.example.grupo12_praticaatdd.premiacao.adaptador.saida.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Premiacao concedida em um mes. A coluna {@code mes_referencia} e unica: mesmo
 * com duas requisicoes simultaneas, o banco impede premiar o mesmo mes duas vezes.
 */
@Entity
@Table(name = "premiacao_mensal")
public class PremiacaoMensalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mes_referencia", nullable = false, unique = true, length = 7)
    private String mesReferencia;

    @Column(name = "aluno_premiado", nullable = false, length = 120)
    private String alunoPremiado;

    @Column(nullable = false)
    private int engajamento;

    @Column(name = "cursos_ganhos", nullable = false)
    private int cursosGanhos;

    protected PremiacaoMensalEntity() {
        // exigido pelo JPA
    }

    public PremiacaoMensalEntity(String mesReferencia, String alunoPremiado, int engajamento, int cursosGanhos) {
        this.mesReferencia = mesReferencia;
        this.alunoPremiado = alunoPremiado;
        this.engajamento = engajamento;
        this.cursosGanhos = cursosGanhos;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public String getAlunoPremiado() {
        return alunoPremiado;
    }

    public int getEngajamento() {
        return engajamento;
    }

    public int getCursosGanhos() {
        return cursosGanhos;
    }
}
