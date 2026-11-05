// =============================================================================
// Pipeline do Grupo 12 - AC1 + B1 (Desafio 6: CI/CD e observabilidade)
//
// Um Jenkinsfile, CICLOS independentes por componente (ADR-006):
//   - CI em paralelo por componente (Core Java, IA Python, notificacao Python, simulador IoT)
//   - deploy de TODOS ou de UM componente so (parametro COMPONENTE, docker compose --no-deps)
//   - o que BLOQUEIA deploy: testes + BDD + ArchUnit (regras de arquitetura) + JaCoCo 100%
//     + pytest dentro do build de cada imagem Python + health + smoke + verificacao arquitetural
// =============================================================================
pipeline {
    agent any

    options {
        skipDefaultCheckout(true)          // o checkout acontece uma unica vez, no stage 1
        disableConcurrentBuilds()          // HOMOL usa portas fixas: um build por vez
        timeout(time: 50, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    parameters {
        choice(name: 'COMPONENTE', choices: ['todos', 'core', 'ia-recomendacao', 'notificacao', 'frontend'],
            description: 'B1/D6: deploy independente. "todos" recria HOMOL; os demais publicam so aquele componente.')
        booleanParam(name: 'VERIFICAR_ARQUITETURA', defaultValue: true,
            description: 'B1/D6: roda em HOMOL as verificacoes dos ADRs (degradacao da IA, outbox, idempotencia, coleta).')
        booleanParam(name: 'PUSH_DOCKER_HUB', defaultValue: false,
            description: 'Opcional: publicar a imagem da API no Docker Hub apos o build local.')
    }

    environment {
        DOCKER_IMAGE = 'andprof/grupo12-pratica-atdd-api'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        IMAGE_TAG = "${BUILD_NUMBER}"
        COMPOSE_FILE = 'docker-compose.homol.yml'
        // Portas de HOMOL no host (as internas dos containers nao mudam).
        API_PORT = '8081'
        FRONT_PORT = '5174'
        PGADMIN_PORT = '5050'
        IA_PORT = '8001'
        RABBIT_UI_PORT = '15673'
        PROMETHEUS_PORT = '9091'
        GRAFANA_PORT = '3001'
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
                    echo [OK] Maven vem do mvnw.cmd; Python e Node rodam dentro dos builds das imagens.
                '''
            }
        }

        stage('1 - Checkout') {
            steps { checkout scm }
        }

        // ---------------------------------------------------------------------
        // CI por componente, em paralelo. Cada ramo e um portao de qualidade
        // independente: falhar a IA nao esconde o resultado do Core e vice-versa.
        // ---------------------------------------------------------------------
        stage('2 - CI por componente') {
            parallel {
                stage('Core: JUnit + BDD + ArchUnit + JaCoCo') {
                    when { expression { params.COMPONENTE in ['todos', 'core'] } }
                    // verify = testes + Cucumber + regras de arquitetura (ArchUnit) + gate de cobertura 100%
                    steps { bat 'call mvnw.cmd -B clean verify' }
                    post {
                        always {
                            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                            archiveArtifacts artifacts: 'target/site/jacoco/**,target/cucumber-report.html', allowEmptyArchive: true
                        }
                    }
                }
                stage('IA recomendacao: pytest no build') {
                    when { expression { params.COMPONENTE in ['todos', 'ia-recomendacao'] } }
                    steps { bat 'docker compose -f %COMPOSE_FILE% build ia-recomendacao' }
                }
                stage('Notificacao: pytest no build') {
                    when { expression { params.COMPONENTE in ['todos', 'notificacao'] } }
                    steps { bat 'docker compose -f %COMPOSE_FILE% build notificacao' }
                }
                stage('Simulador IoT: pytest no build') {
                    when { expression { params.COMPONENTE == 'todos' } }
                    steps { bat 'docker build -t grupo12-iot-simulador:%IMAGE_TAG% iot-simulador' }
                }
            }
        }

        stage('3 - CI - Qualidade PMD') {
            when { expression { params.COMPONENTE in ['todos', 'core'] } }
            steps { bat 'call mvnw.cmd -B pmd:pmd -DskipTests' }
            post {
                always { archiveArtifacts artifacts: 'target/site/pmd.html,target/pmd.xml', allowEmptyArchive: true }
            }
        }

        stage('4 - CI - Package') {
            when { expression { params.COMPONENTE in ['todos', 'core'] } }
            steps { bat 'call mvnw.cmd -B package -DskipTests' }
        }

        stage('5 - CD - Build dos Containers') {
            when { expression { params.COMPONENTE in ['todos', 'core', 'frontend'] } }
            steps {
                script {
                    def servicos = params.COMPONENTE == 'todos' ? 'backend frontend' : (params.COMPONENTE == 'core' ? 'backend' : 'frontend')
                    bat "docker compose -f %COMPOSE_FILE% build ${servicos}"
                }
            }
        }

        stage('6 - Registry - Docker Hub (opcional)') {
            when { expression { return params.PUSH_DOCKER_HUB && params.COMPONENTE in ['todos', 'core'] } }
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

        // ---------------------------------------------------------------------
        // Deploy: "todos" recria o ambiente; um componente so = deploy independente
        // (--no-deps: nao reinicia nada alem dele - ex.: modelo novo da IA sem tocar no Core).
        // ---------------------------------------------------------------------
        stage('7 - CD - Deploy HOMOL') {
            steps {
                script {
                    if (params.COMPONENTE == 'todos') {
                        bat 'docker compose -f %COMPOSE_FILE% down --remove-orphans'
                        powershell '''
                            $ports = @($env:API_PORT, $env:FRONT_PORT, $env:PGADMIN_PORT, $env:IA_PORT,
                                       $env:RABBIT_UI_PORT, $env:PROMETHEUS_PORT, $env:GRAFANA_PORT)
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
                        bat 'docker compose -f %COMPOSE_FILE% up -d'
                    } else {
                        def servico = params.COMPONENTE == 'core' ? 'backend' : params.COMPONENTE
                        echo "Deploy independente: somente '${servico}' (os demais containers seguem rodando)."
                        bat "docker compose -f %COMPOSE_FILE% up -d --no-deps ${servico}"
                    }
                }
            }
        }

        // Readiness do Actuator (B1/D6). O RabbitMQ fica fora do health de proposito:
        // broker fora nao torna o Core indisponivel (ADR-004).
        stage('8 - CD - Health Check HOMOL') {
            steps {
                powershell '''
                    function Esperar($nome, $url) {
                        for ($i = 1; $i -le 24; $i++) {
                            try {
                                $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
                                if ($r.StatusCode -eq 200) { Write-Host "[OK] $nome no ar: $url"; return }
                            } catch { Write-Host "$nome ainda nao pronto. Tentativa $i/24..." }
                            Start-Sleep -Seconds 5
                        }
                        docker compose -f $env:COMPOSE_FILE ps
                        throw "Health check falhou: $nome ($url)"
                    }
                    Esperar "Core (readiness)" "http://localhost:$($env:API_PORT)/actuator/health/readiness"
                    Esperar "IA recomendacao"  "http://localhost:$($env:IA_PORT)/saude"
                    Esperar "Prometheus"       "http://localhost:$($env:PROMETHEUS_PORT)/-/ready"
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
                    Invoke-RestMethod -Method Post -Uri "$front/api/alunos" -ContentType "application/json" -Body $body -TimeoutSec 10 | Out-Null

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

        // ---------------------------------------------------------------------
        // B1 / D6: verificacao ARQUITETURAL em HOMOL - cada passo testa um ADR no
        // ambiente implantado (nao basta compilar: a decisao tem que se sustentar rodando).
        // ---------------------------------------------------------------------
        stage('10 - Verificacao arquitetural em HOMOL') {
            when { expression { params.VERIFICAR_ARQUITETURA } }
            steps {
                powershell '''
                    $api  = "http://localhost:$($env:API_PORT)"
                    $prom = "http://localhost:$($env:PROMETHEUS_PORT)"
                    $nome = "arq-$($env:BUILD_NUMBER)"
                    $mes  = "{0:D4}-01" -f (3000 + ([int]$env:BUILD_NUMBER % 6000))   # mes exclusivo deste build
                    function Prom($q) { (Invoke-RestMethod -Uri "$prom/api/v1/query?query=$([uri]::EscapeDataString($q))").data.result }

                    Invoke-RestMethod -Method Post -Uri "$api/api/alunos" -ContentType "application/json" -Body (@{ nome = $nome } | ConvertTo-Json) | Out-Null
                    $part = @{ nomeAluno = $nome; topicosEscritos = 5; comentariosAjuda = 5; mesReferencia = $mes } | ConvertTo-Json
                    Invoke-RestMethod -Method Post -Uri "$api/api/participacoes" -ContentType "application/json" -Body $part | Out-Null

                    # ADR-002: encerrar o mesmo mes duas vezes credita um unico curso
                    $p1 = Invoke-RestMethod -Method Post -Uri "$api/api/forum/encerrar-mes?mes=$mes"
                    $p2 = Invoke-RestMethod -Method Post -Uri "$api/api/forum/encerrar-mes?mes=$mes"
                    if ($p1.cursosGanhos -ne $p2.cursosGanhos) { throw "ADR-002 violado: mes premiado duas vezes" }
                    Write-Host "[OK] ADR-002: premiacao idempotente (cursosGanhos=$($p2.cursosGanhos))"

                    # ADR-004: o evento da premiacao sai do outbox (relay + RabbitMQ)
                    $ok = $false
                    for ($i = 1; $i -le 12; $i++) {
                        $r = Prom "gamificacao_outbox_pendentes"
                        if ($r -and [double]$r[0].value[1] -eq 0) { $ok = $true; break }
                        Start-Sleep -Seconds 5
                    }
                    if (-not $ok) { throw "ADR-004: eventos presos no outbox (broker inacessivel?)" }
                    Write-Host "[OK] ADR-004: outbox esvaziado - evento entregue ao broker"

                    # ADR-003: IA fora -> recomendacao degradada com HTTP 200 (fluxo nao quebra)
                    docker compose -f $env:COMPOSE_FILE stop ia-recomendacao | Out-Null
                    try {
                        $rec = Invoke-RestMethod -Uri "$api/api/recomendacoes?aluno=$nome" -TimeoutSec 10
                        if (-not $rec.degradado) { throw "ADR-003 violado: com a IA fora a resposta deveria ser degradada" }
                        Invoke-RestMethod -Uri "$api/api/forum/engajamento?mes=$mes" -TimeoutSec 10 | Out-Null
                        Write-Host "[OK] ADR-003: IA fora -> catalogo padrao (degradado=true), ranking respondendo"
                    } finally {
                        docker compose -f $env:COMPOSE_FILE start ia-recomendacao | Out-Null
                    }

                    # D6: o Prometheus esta coletando o Core (sem isso nenhuma das metricas acima vale)
                    $up = Prom 'up{job="core"}'
                    if (-not $up -or $up[0].value[1] -ne "1") { throw "Prometheus nao esta coletando o Core" }
                    Write-Host "[OK] D6: Prometheus coletando o Core"
                '''
            }
        }

        stage('11 - Ambiente HOMOL') {
            steps {
                echo "API / Swagger: http://localhost:${API_PORT}/swagger-ui.html"
                echo "Front-end:     http://localhost:${FRONT_PORT}"
                echo "pgAdmin:       http://localhost:${PGADMIN_PORT}"
                echo "RabbitMQ:      http://localhost:${RABBIT_UI_PORT}"
                echo "Prometheus:    http://localhost:${PROMETHEUS_PORT}/alerts"
                echo "Grafana:       http://localhost:${GRAFANA_PORT}  (dashboard: B1 - ASRs da Arquitetura)"
            }
        }
    }

    post {
        success { echo "Pipeline concluido (${params.COMPONENTE}): CI por componente, HOMOL, smoke e verificacao arquitetural aprovados." }
        failure {
            echo 'Pipeline interrompido. Veja o stage que falhou (componente, porta, ADR ou servico responsavel).'
            bat 'docker compose -f %COMPOSE_FILE% ps -a & docker compose -f %COMPOSE_FILE% logs --tail=80 backend ia-recomendacao notificacao'
        }
    }
}
