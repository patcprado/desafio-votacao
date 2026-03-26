# 🗳️ Desafio Técnico: Sistema de Votação (DBServer)

Este repositório contém uma solução robusta e escalável para o gerenciamento de sessões de votação em assembleias cooperativas, desenvolvida como parte do desafio técnico da DBServer.

---

## 🏗️ Arquitetura e Decisões Técnicas

A aplicação foi construída utilizando **Arquitetura Hexagonal (Ports and Adapters)**. Esta escolha estratégica visa:

* **Independência de Framework:** O "Core" da aplicação (Regras de Negócio) é isolado, facilitando manutenções e trocas de tecnologias de infraestrutura.
* **Domínio Rico:** As regras de validação de votos e sessões estão centralizadas no domínio, seguindo os princípios **SOLID**.
* **Testabilidade:** A separação clara permite testes unitários de alta performance e testes de integração focados.

---

## 🎨 Protótipo e Design de API (Figma)

Antes da implementação, a solução foi desenhada no Figma focando na **Experiência do Desenvolvedor (DX)** e na clareza dos fluxos de dados.

| 1. Cadastro de Pauta | 2. Votação | 3. Resultado Final |
|---|---|---|
| ![Cadastro](src/main/resources/static/assets/Figma-cadastro-pauta.JPG) | ![Voto](src/main/resources/static/assets/Figma-voto.JPG) | ![Resultado](src/main/resources/static/assets/Figma-resultado.JPG) |

> **Nota sobre DX:** O design foi prototipado para garantir que os contratos REST fossem intuitivos e seguissem as melhores práticas, facilitando o consumo por qualquer front-end ou dispositivo mobile.

---

## 📖 Documentação da API (Swagger)

A API utiliza o **SpringDoc OpenAPI** para gerar documentação interativa.

🔗 **Acesse aqui:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---
## 🛠️ Tecnologias Utilizadas
O projeto foi desenvolvido com tecnologias modernas para garantir performance, facilidade de deploy e documentação automatizada:

* **Linguagem & Framework:** Java 21 + Spring Boot 3 (Ecossistema atualizado).
* **API & Documentação:** RESTful API com **Swagger/OpenAPI (SpringDoc)** para contratos claros e testáveis.
* **Persistência de Dados:**
    * **Desenvolvimento/Testes:** H2 (In-memory) com profiles dedicados para agilidade.
    * **Produção/Homologação:** PostgreSQL para resiliência e integridade.
    * **Versionamento:** Flyway (Migrations) para controle histórico do schema.
* **Infraestrutura & Deploy:** **Docker & Docker Compose** para orquestração de containers (API + Banco de Dados).
* **Gestão de Dependências:** Maven.
* **Spring Data JPA** (Persistência com H2 em memória)
* **Bean Validation** (Validação de contratos)

---

## 🐳 Gerenciamento do Ambiente (Docker)

A aplicação e o banco de dados estão conteinerizados. Utilize os comandos abaixo para gerenciar o ciclo de vida dos containers:

### 1. Subir o ambiente
Para subir a API (`votacao-api`) e o banco de dados (`votacao-db`) em segundo plano:

docker-compose up -d

### 2. Verificar o status
Para garantir que os containers estão rodando corretamente e verificar as portas:

docker ps

### 3. Encerrar o ambiente
Para parar os serviços e remover as redes criadas pelo Compose:

docker-compose down

### 4. Consultar Logs
Caso precise validar a inicialização do Spring ou do Postgres:

docker logs -f votacao-api

---

## 📊 Observabilidade (Spring Boot Actuator)

A aplicação utiliza o **Spring Boot Actuator** para expor endpoints de monitoramento, permitindo validar a saúde do sistema e métricas de performance.

### 1. Saúde do Sistema (Health Check)
Verifica se a aplicação e seus componentes (como o Banco de Dados PostgreSQL) estão operacionais.
* **Endpoint:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
* **O que observar:** O status deve retornar `"status": "UP"`.

### 2. Métricas da Aplicação (Metrics)
Exibe uma lista de métricas disponíveis (memória JVM, conexões com banco, threads, etc).
* **Endpoint:** [http://localhost:8080/actuator/metrics](http://localhost:8080/actuator/metrics)
* **Dica:** Para visualizar uma métrica específica, use o caminho completo, ex: `/actuator/metrics/jvm.memory.used`.

---

## ⚙️ Configuração do Ambiente do Banco (Variáveis)

A aplicação está configurada para conectar-se ao PostgreSQL via Docker. Caso deseje rodar localmente fora do container, estas são as configurações de conectividade:

| Variável | Valor Padrão (Docker) | Descrição |
| :--- | :--- | :--- |
| **DB_URL** | `jdbc:postgresql://db:5432/db_votacao` | String de conexão com o Postgres |
| **DB_USERNAME** | `admin` | Usuário do banco de dados |
| **DB_PASSWORD** | `admin` | Senha do banco de dados |

> **Nota de Sênior:** Em um cenário de produção real, estas credenciais seriam injetadas via **AWS Secrets Manager**, garantindo que dados sensíveis não fiquem expostos no código.

---

## 🚀 Como Executar o Projeto

1.  **Pré-requisitos:** Certifique-se de ter o **JDK 21** instalado.
2.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/patcprado/desafio-votacao.git](https://github.com/patcprado/desafio-votacao.git)
    ```
3.  **Execute via Terminal:**
    ```bash
    ./mvnw spring-boot:run
    ```
---

## 📖 Endpoints Principais (Base URL)

Caso prefira utilizar ferramentas como **Postman**, **Insomnia** ou **cURL**, a base URL da aplicação é:
`http://localhost:8080/v1`

### Resumo de Chamadas:
* **Pautas:** `POST /pautas` (Criar) | `GET /pautas` (Listar)
* **Sessões:** `POST /pautas/{id}/abrir` (Abrir votação)
* **Votos:** `POST /pautas/{id}/votos` (Registrar voto)
* **Resultado:** `GET /pautas/{id}/resultado` (Contabilização)

---

## 🧪 Estratégia de Testes e Stress

O projeto foca em três pilares de validação para garantir a resiliência:

1. **Carga Dinâmica:** Através do gatilho automático de 100 votos por sessão.
2. **Idempotência:** Bloqueio rigoroso de votos duplicados (`400 Bad Request`).
3. **Concorrência:** Testado via script Shell para garantir suporte a múltiplas requisições simultâneas.

### Executando o Script de Stress
O repositório inclui o `teste_stress.sh`, que automatiza o fluxo de criação, abertura e injeta mais **50 votos simultâneos**.

chmod +x teste_stress.sh

./teste_stress.sh

---

## ⚡ Gatilho de Carga Automática (Seed Dinâmico)

Diferente de um *seed* estático, a aplicação possui um mecanismo de **indução de carga sob demanda**:

* **O Gatilho:** Ao criar uma nova Pauta e abrir sua respectiva Sessão, o sistema automaticamente popula essa sessão com **100 votos iniciais**.
* **O Motivo:** Permitir que o avaliador valide imediatamente os algoritmos de contabilização e performance dos endpoints de `/resultado`, sem a necessidade de realizar 100 chamadas manuais.

---

## ☁️ Estratégia de Cloud (Próximos Passos)

Embora o foco deste desafio seja a lógica de negócio e a API, a solução foi desenhada para ser **Cloud Native**. Em um cenário real de produção, a infraestrutura recomendada seria:

1.  **Containerização:** O `Dockerfile` já presente permite o deploy em clusters **AWS ECS** ou **EKS (Kubernetes)**.
2.  **Escalabilidade:** Utilização de um **Application Load Balancer (ALB)** para distribuir a carga entre múltiplas instâncias da API.
3.  **Persistência Gerenciada:** Migração do banco PostgreSQL local para um **Amazon RDS**, garantindo backups e alta disponibilidade.
4.  **Infraestrutura como Código:** Utilização de **Terraform** para provisionar VPC, Subnets e Security Groups, garantindo um ambiente isolado e seguro.
5.  **Monitoramento:** Integração dos logs via **CloudWatch** e métricas via **Prometheus/Grafana** (utilizando os dados já expostos pelo Spring Actuator).
## 📝 Autor
**Patricia Campos** - Senior Back-end Developer