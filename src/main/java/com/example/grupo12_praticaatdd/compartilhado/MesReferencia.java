package com.example.grupo12_praticaatdd.compartilhado;

import java.util.regex.Pattern;

/**
 * Validacao do mes de referencia (formato AAAA-MM), usada pelos modulos
 * {@code engajamento} e {@code premiacao}.
 */
public final class MesReferencia {

    private static final Pattern FORMATO = Pattern.compile("\\d{4}-\\d{2}");

    private MesReferencia() {
    }

    public static String validar(String mesReferencia) {
        if (mesReferencia == null || !FORMATO.matcher(mesReferencia).matches()) {
            throw new IllegalArgumentException(
                    "Mes de referencia deve estar no formato AAAA-MM. Recebido: " + mesReferencia);
        }
        return mesReferencia;
    }
}
