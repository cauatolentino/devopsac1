package com.example.grupo12_praticaatdd.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Roda os cenarios BDD escritos em Gherkin (src/test/resources/features).
 *
 * Executado junto com o restante da suite por `./mvnw test`.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.grupo12_praticaatdd.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-report.html")
public class ExecutarCenariosBddTest {
}
