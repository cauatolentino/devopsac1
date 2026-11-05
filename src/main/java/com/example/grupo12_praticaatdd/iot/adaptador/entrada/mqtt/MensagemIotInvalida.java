package com.example.grupo12_praticaatdd.iot.adaptador.entrada.mqtt;

/**
 * Mensagem de dispositivo que nao pode ser traduzida (topico ou payload fora do contrato).
 */
public class MensagemIotInvalida extends RuntimeException {

    public MensagemIotInvalida(String motivo) {
        super(motivo);
    }
}
