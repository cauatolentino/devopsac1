package com.example.grupo12_praticaatdd.premiacao.aplicacao;

import com.example.grupo12_praticaatdd.aluno.dominio.Aluno;
import com.example.grupo12_praticaatdd.compartilhado.MesReferencia;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.entrada.EncerrarMes;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.CreditoDeCursos;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.EventosDePremiacao;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.FonteDeEngajamento;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.HistoricoDePremiacoes;
import com.example.grupo12_praticaatdd.premiacao.aplicacao.porta.saida.ParticipanteDoMes;
import com.example.grupo12_praticaatdd.premiacao.dominio.Forum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Nucleo do modulo premiacao (RF02 - conquistas): implementa o caso de uso
 * {@link EncerrarMes} usando apenas portas.
 *
 * B1 / Desafio 2: esta classe nao conhece mais o EngajamentoService, o
 * AlunoService nem JPA. Tudo o que e externo chega por {@link FonteDeEngajamento},
 * {@link CreditoDeCursos}, {@link HistoricoDePremiacoes} e {@link EventosDePremiacao}. Por isso ela roda em
 * teste de unidade puro, com adapters falsos, sem subir Spring nem banco.
 *
 * As anotacoes {@code @Service} e {@code @Transactional} sao a unica concessao ao
 * framework (decisao pragmatica registrada no ADR-002): sem contexto Spring elas
 * nao tem efeito nenhum.
 */
@Service
public class PremiacaoService implements EncerrarMes {

    private final FonteDeEngajamento fonteDeEngajamento;
    private final CreditoDeCursos creditoDeCursos;
    private final HistoricoDePremiacoes historico;
    private final EventosDePremiacao eventos;

    public PremiacaoService(FonteDeEngajamento fonteDeEngajamento,
                            CreditoDeCursos creditoDeCursos,
                            HistoricoDePremiacoes historico,
                            EventosDePremiacao eventos) {
        this.fonteDeEngajamento = fonteDeEngajamento;
        this.creditoDeCursos = creditoDeCursos;
        this.historico = historico;
        this.eventos = eventos;
    }

    /**
     * Encerra o mes e aplica a regra da US1: o aluno de maior engajamento ganha 1 curso.
     * Um mes que ja teve premiacao devolve o resultado registrado e nao credita de novo.
     */
    @Override
    @Transactional
    public ResultadoPremiacao encerrarMes(String mesReferencia) {
        String mes = MesReferencia.validar(mesReferencia);

        Optional<ResultadoPremiacao> jaPremiado = historico.buscar(mes);
        if (jaPremiado.isPresent()) {
            return jaPremiado.get();
        }

        List<ParticipanteDoMes> participantes = fonteDeEngajamento.participantesDoMes(mes);
        Forum forum = new Forum();
        participantes.forEach(participante -> forum.registrar(participante.participacao()));

        Aluno premiado = forum.premiarAlunoDoMes();
        if (premiado == null) {
            return ResultadoPremiacao.semPremiacao(mes);
        }

        ParticipanteDoMes vencedor = participacaoVencedoraDe(premiado, participantes);
        creditoDeCursos.creditar(vencedor.alunoId(), premiado.getCursosGanhos());

        ResultadoPremiacao resultado = new ResultadoPremiacao(mes, premiado.getNome(),
                vencedor.participacao().getEngajamento(), premiado.getCursosGanhos());
        historico.registrar(resultado);
        // Desafio 4: so REGISTRA o fato (outbox, mesma transacao). Notificar / analytics e assincrono.
        eventos.premiacaoConcedida(vencedor.alunoId(), resultado);
        return resultado;
    }

    /**
     * O Forum devolve o aluno premiado; aqui recuperamos a participacao de maior
     * engajamento desse aluno (ele pode ter mais de uma no mes).
     */
    private ParticipanteDoMes participacaoVencedoraDe(Aluno premiado, List<ParticipanteDoMes> participantes) {
        ParticipanteDoMes vencedor = null;
        for (ParticipanteDoMes participante : participantes) {
            boolean doPremiado = participante.participacao().getAluno() == premiado;
            if (doPremiado && (vencedor == null
                    || participante.participacao().getEngajamento() > vencedor.participacao().getEngajamento())) {
                vencedor = participante;
            }
        }
        return vencedor;
    }
}
