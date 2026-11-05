package com.example.grupo12_praticaatdd.eventos.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Evento de integracao aguardando entrega ao broker (padrao Transactional Outbox).
 *
 * E gravado na MESMA transacao da operacao de negocio. Assim, ou a premiacao e o
 * evento existem juntos, ou nenhum dos dois existe - mesmo que o broker esteja fora.
 */
@Entity
@Table(name = "evento_outbox")
public class EventoOutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false, unique = true, length = 36)
    private String eventoId;

    @Column(nullable = false, length = 80)
    private String tipo;

    @Column(name = "routing_key", nullable = false, length = 120)
    private String routingKey;

    @Column(nullable = false, length = 2000)
    private String payload;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "publicado_em")
    private Instant publicadoEm;

    @Column(nullable = false)
    private int tentativas;

    @Column(name = "ultimo_erro", length = 500)
    private String ultimoErro;

    protected EventoOutboxEntity() {
        // exigido pelo JPA
    }

    public EventoOutboxEntity(String eventoId, String tipo, String routingKey, String payload, Instant criadoEm) {
        this.eventoId = eventoId;
        this.tipo = tipo;
        this.routingKey = routingKey;
        this.payload = payload;
        this.criadoEm = criadoEm;
    }

    public void marcarPublicado(Instant quando) {
        this.publicadoEm = quando;
        this.tentativas++;
        this.ultimoErro = null;
    }

    public void registrarFalha(String erro) {
        this.tentativas++;
        String texto = String.valueOf(erro);
        this.ultimoErro = texto.substring(0, Math.min(texto.length(), 500));
    }

    public String getEventoId() {
        return eventoId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getPublicadoEm() {
        return publicadoEm;
    }

    public int getTentativas() {
        return tentativas;
    }

    public String getUltimoErro() {
        return ultimoErro;
    }
}
