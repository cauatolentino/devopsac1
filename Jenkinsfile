pipeline {
    agent any

    options {
        skipDefaultCheckout(true)          // o checkout acontece uma unica vez, no stage 1
        disableConcurrentBuilds()          // HOMOL usa portas fixas: um build por vez
        timeout(time: 40, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    parameters {
        booleanParam(name: 'PUSH_DOCKER_HUB', defaultValue: false,
            description: 'Opcional: publicar a imagem da API no Docker Hub apos o build local.')
    }

    environment {
        DOCKER_IMAGE = 'andprof/grupo12-pratica-atdd-api'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        IMAGE_TAG = "${BUILD_NUMBER}"
        COMPOSE_FILE = 'docker-compose.homol.yml'
        // A API usa 8081 no host para nao disputar a porta 8080, comum no Jenkins.
        // As portas internas dos containers permanecem inalteradas.
        API_PORT = '8081'
        FRONT_PORT = '5174'
        PGADMIN_PORT = '5050'
    }

    stages {
        stage('0 - Preflight do Ambiente') {
            steps {
                bat '''
                    @echo off
                    echo === Java usado pelo Jenkins ===
                    java -version
                    if errorlevel 1 exit /b 1
                    echo.
                    echo === Docker ===
                    docker version
                    if errorlevel 1 exit /b 1
                    echo.
                    echo === Docker Compose ===
                    docker compose version
                    if errorlevel 1 exit /b 1
                    echo.
                    echo [OK] Maven vem do mvnw.cmd e Node/npm rodam dentro do build da imagem do frontend.
                '''
            }
        }

        stage('1 - Checkout') {
            steps { checkout scm }
        }

        // verify = testes JUnit + BDD (Cucumber) + relatorio JaCoCo + gate de cobertura 100%
        // (o relatorio e o gate do JaCoCo so rodam na fase verify, nao em "test").
        stage('2 - CI - Testes JUnit/BDD + JaCoCo') {
            steps { bat 'call mvnw.cmd -B clean verify' }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/site/jacoco/**,target/cucumber-report.html', allowEmptyArchive: true
                }
            }
        }

        stage('3 - CI - Qualidade PMD') {
            steps { bat 'call mvnw.cmd -B pmd:pmd -DskipTests' }
            post {
                always { archiveArtifacts artifacts: 'target/site/pmd.html,target/pmd.xml', allowEmptyArchive: true }
            }
        }

        stage('4 - CI - Package') {
            steps { bat 'call mvnw.cmd -B package -DskipTests' }
        }

        stage('5 - CD - Build dos Containers') {
            steps {
                bat 'docker compose -f %COMPOSE_FILE% build backend frontend'
            }
        }

        stage('6 - Registry - Docker Hub (opcional)') {
            when { expression { return params.PUSH_DOCKER_HUB } }
            steps {
                withCredentials([usernamePassword(credentialsId: env.DOCKER_CREDENTIALS_ID,
                    usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                    // PowerShell evita o espaco extra que "echo senha | docker login" injeta no cmd
                    powershell '''
                        $env:DOCKER_PASSWORD | docker login -u $env:DOCKER_USER --password-stdin
                        if ($LASTEXITCODE -ne 0) { throw "docker login falhou" }
                        try {
                            docker push "$($env:DOCKER_IMAGE):$($env:IMAGE_TAG)"
                            if ($LASTEXITCODE -ne 0) { throw "docker push falhou" }
                        } finally {
                            docker logout
                        }
                    '''
                }
            }
        }

        stage('7 - CD - Deploy HOMOL') {
            steps {
                bat 'docker compose -f %COMPOSE_FILE% down --remove-orphans'
                powershell '''
                    $ports = @($env:API_PORT, $env:FRONT_PORT, $env:PGADMIN_PORT)
                    $ocupadas = @()
                    foreach ($p in $ports) {
                        $conn = Get-NetTCPConnection -LocalPort ([int]$p) -State Listen -ErrorAction SilentlyContinue
                        if ($conn) { $ocupadas += $p }
                    }
                    if ($ocupadas.Count -gt 0) {
                        throw "Porta(s) ocupada(s): $($ocupadas -join ', '). Altere apenas as variaveis de porta no bloco environment do Jenkinsfile."
                    }
                    Write-Host "[OK] Portas de HOMOL disponiveis."
                '''
                bat 'docker compose -f %COMPOSE_FILE% up -d postgres pgadmin backend frontend'
            }
        }

        // O projeto nao usa Spring Actuator: a API e considerada no ar quando o
        // contrato OpenAPI (/v3/api-docs) responde 200.
        stage('8 - CD - Health Check HOMOL') {
            steps {
                powershell '''
                    $url = "http://localhost:$($env:API_PORT)/v3/api-docs"
                    $ok = $false
                    for ($i = 1; $i -le 18; $i++) {
                        try {
                            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
                            if ($r.StatusCode -eq 200) { $ok = $true; break }
                        } catch {
                            Write-Host "API ainda nao pronta. Tentativa $i/18..."
                        }
                        Start-Sleep -Seconds 5
                    }
                    if (-not $ok) {
                        docker compose -f $env:COMPOSE_FILE ps
                        docker compose -f $env:COMPOSE_FILE logs --tail=100 backend
                        throw "Health check da API falhou em $url"
                    }
                    Write-Host "[OK] API HOMOL no ar: $url"
                '''
            }
        }

        // Smoke test ponta a ponta: front (nginx) -> proxy /api -> API -> PostgreSQL.
        stage('9 - CD - Smoke Test E2E') {
            steps {
                powershell '''
                    $front = "http://localhost:$($env:FRONT_PORT)"
                    $nome  = "smoke-$($env:BUILD_NUMBER)"

                    $pagina = Invoke-WebRequest -Uri $front -UseBasicParsing -TimeoutSec 10
                    if ($pagina.StatusCode -ne 200) { throw "Front-end nao respondeu 200" }

                    $body = @{ nome = $nome } | ConvertTo-Json
                    $novo = Invoke-RestMethod -Method Post -Uri "$front/api/alunos" `
                        -ContentType "application/json" -Body $body -TimeoutSec 10

                    $alunos = Invoke-RestMethod -Uri "$front/api/alunos" -TimeoutSec 10
                    if (-not ($alunos | Where-Object { $_.nome -eq $nome })) {
                        throw "Aluno '$nome' cadastrado via front/proxy nao foi encontrado na listagem"
                    }

                    $mes = (Get-Date).ToString("yyyy-MM")
                    Invoke-RestMethod -Uri "$front/api/forum/engajamento?mes=$mes" -TimeoutSec 10 | Out-Null
                    Write-Host "[OK] Smoke test E2E aprovado (front -> API -> PostgreSQL)."
                '''
            }
        }

        stage('10 - Ambiente HOMOL') {
            steps {
                echo "API / Swagger: http://localhost:${API_PORT}/swagger-ui.html"
                echo "Front-end:     http://localhost:${FRONT_PORT}"
                echo "pgAdmin:       http://localhost:${PGADMIN_PORT}"
            }
        }
    }

    post {
        success { echo 'Pipeline concluido: CI (JUnit/BDD/JaCoCo/PMD), containers, HOMOL e smoke test aprovados.' }
        failure {
            echo 'Pipeline interrompido. Veja o stage que falhou (dependencia, porta ou servico responsavel).'
            bat 'docker compose -f %COMPOSE_FILE% ps -a & docker compose -f %COMPOSE_FILE% logs --tail=80 backend frontend'
        }
    }
}
