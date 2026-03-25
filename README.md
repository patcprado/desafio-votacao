Markdown

# 🗳️ Desafio Técnico: Sistema de Votação (DBServer)

Este repositório contém uma solução robusta e escalável para o gerenciamento de sessões de votação em assembleias cooperativas, desenvolvida como parte do desafio técnico da DBServer.

---

## 🏗️ Arquitetura e Decisões Técnicas

A aplicação foi construída utilizando **Arquitetura Hexagonal (Ports and Adapters)**. Esta escolha estratégica visa:

* **Independência de Framework:** O "Core" da aplicação (Regras de Negócio) é isolado, facilitando manutenções e trocas de tecnologia de infraestrutura.
* **Domínio Rico:** As regras de validação de votos e sessões estão centralizadas no domínio, seguindo os princípios **SOLID**.
* **Testabilidade:** A separação clara permite testes unitários de alta performance e testes de integração focados.

---

## 📖 Documentação da API (Swagger)

A API utiliza o **SpringDoc OpenAPI** para gerar documentação interativa. 

🔗 **Acesse aqui:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### 📸 Visualização da Interface
> *Dica: Insira aqui o print que você tirou da tela do Swagger.*

---

## 🛠️ Tecnologias Utilizadas

* **Java 21** & **Spring Boot 3**
* **Spring Data JPA** (Persistência com H2 em memória)
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
4.  A API estará disponível em `http://localhost:8080`.

---

## 📌 Detalhes de Implementação

### Versionamento da API
Utilizei a estratégia de **Versionamento pela URL** (ex: `/v1/pautas`).
* **Motivo:** É a abordagem mais clara para clientes mobile, permitindo que versões diferentes coexistam durante transições sem quebrar dispositivos que ainda não atualizaram o app.

### Regras de Negócio Implementadas
* **Sessão Expirada:** Bloqueio automático de votos após o tempo definido (default 1 min).
* **Voto Único:** O sistema impede que o mesmo `associadoId` vote mais de uma vez na mesma pauta.
* **Integridade:** Validação de existência de pautas antes da abertura de sessões.

### Banco de Dados (H2 Console)
Para visualizar as tabelas em tempo real:
* **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
* **JDBC URL:** `jdbc:h2:mem:testdb`
* **User:** `sa` | **Password:** *(em branco)*

---

## 🧪 Roteiro de Teste (Happy Path)

Para testar o fluxo completo, você pode usar o arquivo `src/testes.http` ou o Swagger:

1.  **Criar Pauta:** `POST /v1/pautas` (Gera ID 1).
2.  **Abrir Sessão:** `POST /v1/pautas/1/abrir?minutos=1`.
3.  **Votar (Sim):** `POST /v1/pautas/1/votos` (Associado A).
4.  **Votar (Não):** `POST /v1/pautas/1/votos` (Associado B).
5.  **Resultado:** `GET /v1/pautas/1/resultado` -> Deve retornar a contabilização e o vencedor.

---

## 📝 Autor
**Patricia Campos** - Senior Back-end Developer

---

# Votação

## Objetivo

No cooperativismo, cada associado possui um voto e as decisões são tomadas em assembleias, por votação. Imagine que você deve criar uma solução para dispositivos móveis para gerenciar e participar dessas sessões de votação.
Essa solução deve ser executada na nuvem e promover as seguintes funcionalidades através de uma API REST:

- Cadastrar uma nova pauta
- Abrir uma sessão de votação em uma pauta (a sessão de votação deve ficar aberta por
  um tempo determinado na chamada de abertura ou 1 minuto por default)
- Receber votos dos associados em pautas (os votos são apenas 'Sim'/'Não'. Cada associado
  é identificado por um id único e pode votar apenas uma vez por pauta)
- Contabilizar os votos e dar o resultado da votação na pauta

Para fins de exercício, a segurança das interfaces pode ser abstraída e qualquer chamada para as interfaces pode ser considerada como autorizada. A solução deve ser construída em java, usando Spring-boot, mas os frameworks e bibliotecas são de livre escolha (desde que não infrinja direitos de uso).

É importante que as pautas e os votos sejam persistidos e que não sejam perdidos com o restart da aplicação.

O foco dessa avaliação é a comunicação entre o backend e o aplicativo mobile. Essa comunicação é feita através de mensagens no formato JSON, onde essas mensagens serão interpretadas pelo cliente para montar as telas onde o usuário vai interagir com o sistema. A aplicação cliente não faz parte da avaliação, apenas os componentes do servidor. O formato padrão dessas mensagens será detalhado no anexo 1.

## Como proceder

Por favor, **CLONE** o repositório e implemente sua solução, ao final, notifique a conclusão e envie o link do seu repositório clonado no GitHub, para que possamos analisar o código implementado.

Lembre de deixar todas as orientações necessárias para executar o seu código.

### Tarefas bônus

- Tarefa Bônus 1 - Integração com sistemas externos
  - Criar uma Facade/Client Fake que retorna aleátoriamente se um CPF recebido é válido ou não.
  - Caso o CPF seja inválido, a API retornará o HTTP Status 404 (Not found). Você pode usar geradores de CPF para gerar CPFs válidos
  - Caso o CPF seja válido, a API retornará se o usuário pode (ABLE_TO_VOTE) ou não pode (UNABLE_TO_VOTE) executar a operação. Essa operação retorna resultados aleatórios, portanto um mesmo CPF pode funcionar em um teste e não funcionar no outro.

```
// CPF Ok para votar
{
    "status": "ABLE_TO_VOTE
}
// CPF Nao Ok para votar - retornar 404 no client tb
{
    "status": "UNABLE_TO_VOTE
}
```

Exemplos de retorno do serviço

### Tarefa Bônus 2 - Performance

- Imagine que sua aplicação possa ser usada em cenários que existam centenas de
  milhares de votos. Ela deve se comportar de maneira performática nesses
  cenários
- Testes de performance são uma boa maneira de garantir e observar como sua
  aplicação se comporta

### Tarefa Bônus 3 - Versionamento da API

○ Como você versionaria a API da sua aplicação? Que estratégia usar?

## O que será analisado

- Simplicidade no design da solução (evitar over engineering)
- Organização do código
- Arquitetura do projeto
- Boas práticas de programação (manutenibilidade, legibilidade etc)
- Possíveis bugs
- Tratamento de erros e exceções
- Explicação breve do porquê das escolhas tomadas durante o desenvolvimento da solução
- Uso de testes automatizados e ferramentas de qualidade
- Limpeza do código
- Documentação do código e da API
- Logs da aplicação
- Mensagens e organização dos commits

## Dicas

- Teste bem sua solução, evite bugs
- Deixe o domínio das URLs de callback passiveis de alteração via configuração, para facilitar
  o teste tanto no emulador, quanto em dispositivos fisicos.
  Observações importantes
- Não inicie o teste sem sanar todas as dúvidas
- Iremos executar a aplicação para testá-la, cuide com qualquer dependência externa e
  deixe claro caso haja instruções especiais para execução do mesmo
  Classificação da informação: Uso Interno

## Anexo 1

### Introdução

A seguir serão detalhados os tipos de tela que o cliente mobile suporta, assim como os tipos de campos disponíveis para a interação do usuário.

### Tipo de tela – FORMULARIO

A tela do tipo FORMULARIO exibe uma coleção de campos (itens) e possui um ou dois botões de ação na parte inferior.

O aplicativo envia uma requisição POST para a url informada e com o body definido pelo objeto dentro de cada botão quando o mesmo é acionado. Nos casos onde temos campos de entrada
de dados na tela, os valores informados pelo usuário são adicionados ao corpo da requisição. Abaixo o exemplo da requisição que o aplicativo vai fazer quando o botão “Ação 1” for acionado:

```
POST http://seudominio.com/ACAO1
{
    “campo1”: “valor1”,
    “campo2”: 123,
    “idCampoTexto”: “Texto”,
    “idCampoNumerico: 999
    “idCampoData”: “01/01/2000”
}
```

Obs: o formato da url acima é meramente ilustrativo e não define qualquer padrão de formato.

### Tipo de tela – SELECAO

A tela do tipo SELECAO exibe uma lista de opções para que o usuário.

O aplicativo envia uma requisição POST para a url informada e com o body definido pelo objeto dentro de cada item da lista de seleção, quando o mesmo é acionado, semelhando ao funcionamento dos botões da tela FORMULARIO.

# desafio-votacao

