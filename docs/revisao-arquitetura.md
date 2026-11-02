# Revisão de arquitetura e organização da DAD

Revisão baseada no ZIP `ds-dad-api-revisao.zip` enviado em 02/11/2026, incluindo as correções de filtros de datas nulas e listagem sem filtro de tipo. A refatoração preserva os contratos de entrada e saída usados pelo CRUD.

## Alterações

| Ponto | Alteração aplicada |
| --- | --- |
| Regras de negócio | Validações de quizzes, cursos, condomínios, cooperativas, usuários e coletas centralizadas em `dad.core.regra`. |
| Histórico e integridade | Bloqueios de estrutura de quiz, remoção de aulas/torres e alteração de programação com visitas passam a usar regras puras, com fatos consultados sob os bloqueios existentes. |
| Importação | A porta `ConsultaCadastroUsuariosGateway` retorna fatos tipados. O core decide quais campos estão inválidos e monta os erros indexados. |
| Entrada HTTP | A montagem das ocorrências do calendário passa do controller para o mapper. |
| Catálogos | Agrupamento de tipos administrativos extraído para uma regra do core, preservando IDs equivalentes. |
| Interfaces | O gateway de quiz expõe apenas as operações consumidas pelo core. Consultas auxiliares ficam privadas no adapter; um método sem uso foi removido. |
| Reutilização | Regra de paginação compartilhada; tradução dos dias da semana centralizada. |
| Formatação | Java padronizado com Google Java Format 1.24.0, indentação de dois espaços. Comentários e anotações Swagger preservados. |
| Imports | Imports com curinga expandidos com resolução de símbolos pelo compilador; imports sem uso e duplicados removidos e ordenados. |
| Build | Compilação Java explicitamente em UTF-8. |
| Proteção futura | Testes de arquitetura para impedir dependências do core em adapters, acesso direto à persistência pelo entrypoint e dependência da persistência em DTOs/controllers. |

As classes `ValidacaoImportacaoUsuariosGateway` e `ValidacaoImportacaoUsuariosGatewayImpl` foram substituídas pela porta e implementação de consulta. Todos os consumidores presentes no ZIP foram atualizados.

## Divisão de responsabilidades

- `entrypoint`: recebe HTTP, valida o contrato de entrada e converte DTOs.
- `core/service`: coordena operações por interfaces e delimita transações.
- `core/regra`: decide regras com objetos do domínio e fatos recebidos; não depende de Spring, JPA, banco ou HTTP.
- `core/domain`: representa dados e conceitos do negócio.
- `core/gateway`: define os contratos usados pela aplicação, sem expor entidades JPA ou DTOs HTTP.
- `dataprovider`: consulta o banco, obtém bloqueios, converte entities e persiste alterações.
- `config`: integra Spring, segurança, OpenAPI e infraestrutura.

As regras que precisam de dados atuais continuam sendo executadas depois da aquisição dos bloqueios na persistência. A extração não substitui verificações sob bloqueio por consultas antecipadas sem proteção.

É uma aplicação hexagonal com integração pragmática ao Spring: `@Service` e `@Transactional` permanecem na camada de aplicação. Domínio e regras são independentes do framework. Não foram introduzidos pacotes ou classes de use case.

## SOLID

A responsabilidade por decidir regras foi separada da responsabilidade por consultar e gravar. As portas usam tipos do core, e os serviços não dependem de adapters concretos. A interface de consulta da importação tem uma responsabilidade específica, e o gateway de quiz não expõe auxiliares internos. As implementações seguem os contratos verificados pela compilação e pelos testes existentes.

SOLID é um critério de projeto, não uma porcentagem comprovável. Não foram criadas interfaces para cada classe nem hierarquias de herança sem necessidade.

## Preservações verificadas

- Os 24 DTOs de entrada/saída mantêm as declarações e anotações da versão recebida, conferidas pela árvore sintática Java.
- Os valores literais de consultas/configuração em 23 arquivos mantêm o conteúdo original, incluindo os casts dos filtros de datas e as configurações de segurança.
- As rotas, métodos HTTP, paginação, códigos de resposta, formato de erros e requisitos JWT são preservados.
- Permanecem transações, bloqueios pessimistas/advisory, ordem das gravações e proteções de histórico.
- Não há alteração de schema, SQL de recursos, `.env`, credenciais, versão do aplicativo ou regras do frontend.

## Validação

Foram executados com Java 21: compilação de fontes de produção e de testes; 123 testes unitários, de contratos HTTP e de arquitetura, todos aprovados. A auditoria de símbolos resolveu 66 imports com curinga em 50 arquivos; a verificação final não identificou imports explícitos sem uso. A formatação foi verificada quanto à idempotência.

A suíte completa com PostgreSQL/Testcontainers precisa ser executada após a aplicação, com Docker aberto. O ZIP recebido contém Java e build, sem os recursos SQL de teste, e este ambiente não dispõe de Docker. Testes entre mobile, DAD e frontend em execução continuam sendo uma validação separada.

```powershell
.\gradlew.bat clean test --no-daemon --max-workers=1 --no-parallel
```

Depois dos testes, confira `git diff` e faça o commit na sua branch. O aplicador não executa commit ou push.
