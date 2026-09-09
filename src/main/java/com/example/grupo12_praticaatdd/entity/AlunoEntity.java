package com.example.grupo12_praticaatdd.entity;

import com.example.grupo12_praticaatdd.domain.Aluno;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representacao do Aluno no banco de dados.
 *
 * A entidade e separada da classe de dominio de proposito: o dominio (que foi
 * construido por TDD) nao depende de JPA nem de nenhum framework.
 */
@Entity
@Table(name = "aluno")
public class AlunoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @Column(name = "cursos_ganhos", nullable = false)
    private int cursosGanhos;

    protected AlunoEntity() {
        // exigido pelo JPA
    }

    public AlunoEntity(String nome) {
        this.nome = nome;
        this.cursosGanhos = 0;
    }

    /**
     * Converte a entidade para o objeto de dominio, preservando os cursos ja ganhos.
     */
    public Aluno paraDominio() {
        Aluno aluno = new Aluno(nome);
        for (int i = 0; i < cursosGanhos; i++) {
            aluno.ganharCurso();
        }
        return aluno;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getCursosGanhos() {
        return cursosGanhos;
    }

    public void setCursosGanhos(int cursosGanhos) {
        this.cursosGanhos = cursosGanhos;
    }
}
