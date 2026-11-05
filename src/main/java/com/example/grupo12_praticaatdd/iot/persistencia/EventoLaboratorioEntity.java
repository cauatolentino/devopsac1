package com.example.grupo12_praticaatdd.iot.persistencia;

import com.example.grupo12_praticaatdd.iot.dominio.EventoDeLaboratorio;
import com.example.grupo12_praticaatdd.iot.dominio.TipoEventoLaboratorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Evento de laboratorio recebido. UNIQUE em evento_id = deduplicacao garantida pelo banco.
 */
@Entity
@Table(name = "evento_laboratorio")
public class EventoLaboratorioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false, unique = true, length = 64)
    private String eventoId;

    @Column(nullable = false, length = 60)
    private String laboratorio;

    @Column(nullable = false, length = 60)
    private String dispositivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEventoLaboratorio tipo;

    @Column(nullable = false, length = 120)
    private String aluno;

    @Column(length = 120)
    private String atividade;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    @Column(name = "recebido_em", nullable = false)
    private Instant recebidoEm;

    protected EventoLaboratorioEntity() {
        // exigido pelo JPA
    }

    public EventoLaboratorioEntity(EventoDeLaboratorio evento, Instant recebidoEm) {
        this.eventoId = evento.eventoId();
        this.laboratorio = evento.laboratorio();
        this.dispositivo = evento.dispositivo();
        this.tipo = evento.tipo();
        this.aluno = evento.aluno();
        this.atividade = evento.atividade();
        this.ocorridoEm = evento.ocorridoEm();
        this.recebidoEm = recebidoEm;
    }

    public EventoDeLaboratorio paraDominio() {
        return new EventoDeLaboratorio(eventoId, laboratorio, dispositivo, tipo, aluno, atividade, ocorridoEm);
    }
}
