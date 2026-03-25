Markdown
# Desafio Técnico: Sistema de Votação

Este repositório contém a solução para o desafio de gerenciamento de sessões de votação em assembleias cooperativas.

---

## 🏗️ Arquitetura e Decisões Técnicas

Para este projeto, optei pela **Arquitetura Hexagonal (Ports and Adapters)**. Esta escolha foi feita para garantir:

* **Independência de Framework:** O coração da aplicação (Regras de Negócio) é isolado de detalhes de infraestrutura como o Spring Boot ou o banco de dados H2.
* **Facilidade de Testes:** A separação permite testar o domínio sem a necessidade de subir o contexto completo do Spring.
* **Manutenibilidade:** Segue os princípios **SOLID**, facilitando a expansão do sistema (como a futura integração com sistemas externos de CPF).

## 🚀 Como Executar a Aplicação

* **Pré-requisitos:** Java 17+ e Maven instalados.
* **Clone o repositório:** `git clone [URL_DO_SEU_REPO]`
* **Execute via Terminal:**
```bash
mvn spring-boot:run
Acesse a API: A aplicação estará disponível em http://localhost:8080.

📌 Versionamento da API (Tarefa Bônus 3)
Utilizei a estratégia de Versionamento pela URL (ex: /v1/pautas).

Por que? É a abordagem mais clara para o cliente mobile (como citado no Anexo 1 do desafio), permitindo que versões diferentes da API coexistam durante períodos de transição sem quebrar dispositivos que ainda não atualizaram o aplicativo.

🗄️ Acesso ao Banco de Dados (H2 Console)
Para validar se os dados (Pautas, Sessões e Votos) estão sendo gravados corretamente:

URL: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:testdb

User: sa

Password: (em branco)

🗳️ Regras de Negócio do Voto
O sistema impede ações que quebrem a integridade da votação:

Sessão Encerrada: Não é possível votar se o tempo da sessão já expirou.

Voto Duplicado: O mesmo associadoId não pode votar duas vezes na mesma pauta.

ID Inexistente: O sistema valida se a pauta informada realmente existe antes de processar o voto.

🛠️ Solução de Problemas (Troubleshooting)
Erro: "Pauta não encontrada" (500 Internal Server Error)
Como o banco de dados é em memória (H2), os dados são apagados sempre que a aplicação é reiniciada.

Certifique-se de:

Executar o POST de criação de pauta antes de tentar abrir uma sessão.

Verificar no log do console qual foi o ID gerado para a pauta (ex: ID 1, 2...).

Usar esse ID correto na URL de abertura de sessão: /v1/pautas/{ID}/abrir.



🧪 Roteiro de Teste de Ponta a Ponta (End-to-End)
Abra o seu arquivo src/testes.http e execute nesta ordem exata:

1. O Fluxo de Sucesso (Happy Path)
Criar Pauta: POST /v1/pautas -> Deve retornar 201 Created e o ID 1.

Abrir Sessão: POST /v1/pautas/1/abrir?minutos=1 -> Deve retornar 200 OK.

Voto 1 (Sim): POST /v1/pautas/1/votos (Associado "A") -> Deve retornar 200 OK.

Voto 2 (Não): POST /v1/pautas/1/votos (Associado "B") -> Deve retornar 200 OK.

Resultado: GET /v1/pautas/1/resultado -> Deve mostrar:

totalVotos: 2

totalSim: 1

totalNao: 1

vencedor: "EMPATE" (ou conforme a lógica que o orquestrador implementou).

2. Teste de Resiliência (Edge Cases)
Duplicidade: Tente votar de novo com o Associado "A" na pauta 1. -> Esperado: Erro (400 ou RuntimeException).

Pauta Inexistente: Tente abrir sessão para a Pauta 99. -> Esperado: Erro "Pauta não encontrada".

Sessão Expirada: Aguarde 1 minuto e tente votar com o Associado "C". -> Esperado: Erro "Sessão encerrada".



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

