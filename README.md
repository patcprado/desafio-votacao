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

## ⚡ Gatilho de Carga Automática (Seed Dinâmico)

Diferente de um *seed* estático, a aplicação possui um mecanismo de **indução de carga sob demanda**:

* **O Gatilho:** Ao criar uma nova Pauta e abrir sua respectiva Sessão, o sistema automaticamente popula essa sessão com **100 votos iniciais**.
* **O Motivo:** Permitir que o avaliador valide imediatamente os algoritmos de contabilização e performance dos endpoints de `/resultado`, sem a necessidade de realizar 100 chamadas manuais.

---

## 📖 Documentação da API (Swagger)

A API utiliza o **SpringDoc OpenAPI** para gerar documentação interativa.

🔗 **Acesse aqui:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🛠️ Tecnologias Utilizadas

* **Java 21** & **Spring Boot 3**
* **Spring Data JPA** (Persistência com H2 em memória)
* **Flyway** (Migração de banco de dados)
* **Bean Validation** (Validação de contratos)
* **SpringDoc OpenAPI** (Documentação v3)
* **Maven** (Gerenciador de dependências)

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

## 📌 Detalhes de Implementação

* **Versionamento da API:** Estratégia por URL (`/v1/pautas`) para melhor compatibilidade mobile.
* **Sessão Expirada:** Bloqueio automático de votos após o tempo definido (default 1 min).
* **Banco de Dados (H2 Console):** * **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    * **User:** `sa` | **Password:** *(em branco)*

---

## 📝 Autor
**Patricia Campos** - Senior Back-end Developer