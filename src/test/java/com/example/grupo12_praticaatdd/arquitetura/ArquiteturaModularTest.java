package com.example.grupo12_praticaatdd.arquitetura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * B1 / Desafio 1 - as fronteiras do monolito modular viram regra do build.
 *
 * Se alguem reintroduzir o acoplamento que existia na estrutura em camadas da AC1
 * (ex.: um modulo lendo a tabela de outro, ou o dominio dependendo de JPA), este
 * teste falha e o pipeline para.
 *
 * Mapa dos modulos:
 * <pre>
 *   aluno        cadastro de alunos
 *   engajamento  participacoes no forum e ranking (RF01) -> depende de aluno
 *   premiacao    encerramento do mes / conquistas (RF02) -> depende de engajamento e aluno
 *   compartilhado erros, validacoes e configuracao comuns
 * </pre>
 * Cada modulo tem: web (entrada HTTP), aplicacao (contrato publico do modulo),
 * dominio (regras, sem framework) e persistencia (JPA, privada do modulo).
 *
 * Desafio 2: o modulo premiacao (regras que mais mudam) e hexagonal:
 * <pre>
 *   premiacao.dominio                    regra da US1 (Forum)
 *   premiacao.aplicacao                  caso de uso + porta.entrada / porta.saida
 *   premiacao.adaptador.entrada.web      REST
 *   premiacao.adaptador.saida.modulos    fala com aluno/engajamento
 *   premiacao.adaptador.saida.persistencia  JPA (tabela premiacao_mensal)
 * </pre>
 */
@AnalyzeClasses(packages = "com.example.grupo12_praticaatdd",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaModularTest {

    private static final String BASE = "com.example.grupo12_praticaatdd";

    // ---------------------------------------------------------------- ciclos

    @ArchTest
    static final ArchRule modulosSemDependenciaCiclica =
            slices().matching(BASE + ".(*)..")
                    .should().beFreeOfCycles()
                    .because("modulos com ciclo nao podem ser extraidos nem evoluidos separadamente");

    // ---------------------------------------------------- direcao entre modulos

    @ArchTest
    static final ArchRule alunoNaoConheceOutrosModulos =
            noClasses().that().resideInAPackage(BASE + ".aluno..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(BASE + ".engajamento..", BASE + ".premiacao..");

    @ArchTest
    static final ArchRule engajamentoNaoConhecePremiacao =
            noClasses().that().resideInAPackage(BASE + ".engajamento..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".premiacao..")
                    .because("o ranking (RF01) e o candidato a extracao e nao pode depender das conquistas");

    @ArchTest
    static final ArchRule compartilhadoNaoConheceModulos =
            noClasses().that().resideInAPackage(BASE + ".compartilhado..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".aluno..", BASE + ".engajamento..", BASE + ".premiacao..");

    // ---------------------------------------- persistencia e web sao privadas

    @ArchTest
    static final ArchRule persistenciaDoAlunoEPrivada = persistenciaPrivada("aluno");

    @ArchTest
    static final ArchRule persistenciaDoEngajamentoEPrivada = persistenciaPrivada("engajamento");

    @ArchTest
    static final ArchRule webDoAlunoEPrivada = webPrivada("aluno");

    @ArchTest
    static final ArchRule webDoEngajamentoEPrivada = webPrivada("engajamento");

    @ArchTest
    static final ArchRule webDaPremiacaoEPrivada = webPrivada("premiacao", "adaptador.entrada.web");

    @ArchTest
    static final ArchRule persistenciaDaPremiacaoEPrivada =
            classes().that().resideInAPackage(BASE + ".premiacao.adaptador.saida.persistencia..")
                    .should().onlyHaveDependentClassesThat()
                    .resideInAPackage(BASE + ".premiacao.adaptador.saida.persistencia..")
                    .because("so o adapter JPA conhece a tabela premiacao_mensal");

    // ------------------------- Desafio 2: Ports & Adapters no modulo premiacao

    @ArchTest
    static final ArchRule nucleoDaPremiacaoNaoConheceAdapters =
            noClasses().that().resideInAnyPackage(BASE + ".premiacao.dominio..", BASE + ".premiacao.aplicacao..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".premiacao.adaptador..")
                    .because("a dependencia aponta para dentro: adapters conhecem as portas, nunca o contrario");

    @ArchTest
    static final ArchRule nucleoDaPremiacaoSemPersistenciaNemOutrosModulos =
            noClasses().that().resideInAPackage(BASE + ".premiacao.aplicacao..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "jakarta.persistence..", "org.springframework.data..", "..persistencia..",
                            BASE + ".aluno.aplicacao..", BASE + ".engajamento.aplicacao..")
                    .because("o caso de uso fala com banco e com outros modulos somente por portas de saida");

    @ArchTest
    static final ArchRule soOAdapterDeModulosConheceOsOutrosModulos =
            noClasses().that().resideInAPackage(BASE + ".premiacao..")
                    .and().resideOutsideOfPackage(BASE + ".premiacao.adaptador.saida.modulos..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".aluno.aplicacao..", BASE + ".engajamento.aplicacao..")
                    .because("se o ranking virar servico, so o adapter de modulos muda");

    // ------------------------------- Desafio 3: fronteira da IA (servico Python)

    @ArchTest
    static final ArchRule fluxoEssencialNaoDependeDaIa =
            noClasses().that().resideInAnyPackage(
                            BASE + ".aluno..", BASE + ".engajamento..", BASE + ".premiacao..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".recomendacao..")
                    .because("a indisponibilidade da IA nao pode bloquear cadastro, participacao, ranking e premiacao");

    @ArchTest
    static final ArchRule soOAdapterDeIaFazChamadaHttp =
            noClasses().that().resideOutsideOfPackage(BASE + ".recomendacao.adaptador.saida.ia..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.web.client..", "org.springframework.http.client..")
                    .because("timeout e traducao de falha da IA ficam concentrados em um unico adapter");

    @ArchTest
    static final ArchRule nucleoDaRecomendacaoNaoConheceAdapters =
            noClasses().that().resideInAnyPackage(
                            BASE + ".recomendacao.dominio..", BASE + ".recomendacao.aplicacao..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".recomendacao.adaptador..");

    // ------------------------------------ Desafio 4: eventos (outbox + broker)

    @ArchTest
    static final ArchRule moduloEventosNaoConheceONegocio =
            noClasses().that().resideInAPackage(BASE + ".eventos..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".aluno..", BASE + ".engajamento..", BASE + ".premiacao..", BASE + ".recomendacao..")
                    .because("o modulo eventos e infraestrutura de integracao, reutilizavel por qualquer modulo");

    @ArchTest
    static final ArchRule soOsAdaptersDeBrokerConhecemAmqp =
            noClasses().that().resideOutsideOfPackages(BASE + ".eventos.rabbit..", BASE + ".iot.adaptador.entrada.mqtt..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework.amqp..")
                    .because("trocar o broker deve mexer so nos adapters de saida (eventos) e de entrada (IoT)");

    @ArchTest
    static final ArchRule nucleoDaPremiacaoNaoConheceOutboxNemBroker =
            noClasses().that().resideInAnyPackage(BASE + ".premiacao.dominio..", BASE + ".premiacao.aplicacao..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".eventos..")
                    .because("o caso de uso so conhece a porta EventosDePremiacao");

    @ArchTest
    static final ArchRule outboxEPrivadoDoModuloEventos =
            classes().that().resideInAPackage(BASE + ".eventos.outbox..")
                    .should().onlyHaveDependentClassesThat().resideInAnyPackage(
                            BASE + ".eventos.outbox..", BASE + ".eventos.aplicacao..", BASE + ".eventos.relay..");

    // ------------------------------------------- Desafio 5: IoT (MQTT -> fila)

    @ArchTest
    static final ArchRule persistenciaDoIotEPrivada = persistenciaPrivada("iot");

    @ArchTest
    static final ArchRule fluxoEssencialNaoDependeDoIot =
            noClasses().that().resideInAnyPackage(
                            BASE + ".aluno..", BASE + ".engajamento..", BASE + ".premiacao..")
                    .should().dependOnClassesThat().resideInAPackage(BASE + ".iot..")
                    .because("dispositivo instavel nao pode afetar cadastro, ranking e premiacao");

    @ArchTest
    static final ArchRule nucleoDoIotNaoConheceMqttNemJson =
            noClasses().that().resideInAnyPackage(BASE + ".iot.dominio..", BASE + ".iot.aplicacao..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            BASE + ".iot.adaptador..", "org.springframework.amqp..", "tools.jackson..")
                    .because("o formato da mensagem do dispositivo fica na camada anticorrupcao");

    // ------------------------------------------------- dominio sem framework

    @ArchTest
    static final ArchRule dominioIndependenteDeFramework =
            noClasses().that().resideInAPackage("..dominio..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..", "jakarta..", "org.hibernate..",
                            "..persistencia..", "..aplicacao..", "..web..")
                    .because("as regras construidas por TDD precisam continuar testaveis sem Spring e sem banco");

    @ArchTest
    static final ArchRule aplicacaoNaoDependeDaWeb =
            noClasses().that().resideInAPackage("..aplicacao..")
                    .should().dependOnClassesThat().resideInAPackage("..web..");

    private static ArchRule persistenciaPrivada(String modulo) {
        return classes().that().resideInAPackage(BASE + "." + modulo + ".persistencia..")
                .should().onlyHaveDependentClassesThat().resideInAnyPackage(
                        BASE + "." + modulo + ".persistencia..", BASE + "." + modulo + ".aplicacao..")
                .because("outros modulos so podem usar o contrato publico (aplicacao) do modulo " + modulo);
    }

    private static ArchRule webPrivada(String modulo) {
        return webPrivada(modulo, "web");
    }

    private static ArchRule webPrivada(String modulo, String pacoteWeb) {
        String web = BASE + "." + modulo + "." + pacoteWeb + "..";
        return classes().that().resideInAPackage(web)
                .should().onlyHaveDependentClassesThat().resideInAPackage(web)
                .because("a camada web e porta de entrada HTTP, nao contrato entre modulos");
    }
}
