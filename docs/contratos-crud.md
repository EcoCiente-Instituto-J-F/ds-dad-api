# Contratos administrativos para o CRUD

Base da revisão: `9d02e19dca26ead5705efdd3165bcfbf12ce469d`.
As alterações usam as tabelas e colunas existentes. Nenhuma migration ou mudança de schema é exigida.

## Calendário de ocorrências

`GET /api/admin/v1/coletas/calendario`

Exige JWT com perfil `ADMIN` e usuário ativo cujo tipo no banco seja administrativo.

| Parâmetro | Regra |
| --- | --- |
| inicio | Obrigatório, timestamp ISO 8601 com fuso, inclusivo |
| fim | Obrigatório, timestamp ISO 8601 com fuso, exclusivo e posterior ao início |
| condominioId, cooperativaId, statusId | Opcionais, inteiros positivos |
| pagina | Inteiro a partir de zero; padrão 0 |
| tamanho | De 1 a 100; padrão 50 |

Exemplo: `/api/admin/v1/coletas/calendario?inicio=2026-11-01T03:00:00Z&fim=2026-12-01T03:00:00Z&pagina=0&tamanho=50`

As recorrências usam `America/Sao_Paulo`, preservando o horário local do início. Os dias são identificados pelo nome do catálogo, e não pelo número do ID. O instante inicial da programação e seu instante final, quando informado, limitam as ocorrências. O final da programação é inclusivo; o final do intervalo consultado é exclusivo.

Agendamentos sem recorrência geram uma ocorrência em `dataInicio`. Os recorrentes podem gerar várias, mesmo quando começaram antes do período pesquisado. Filtros e limites temporais são aplicados antes de contar e paginar no PostgreSQL. A ordenação é crescente por `ocorrenciaEm` e ID do agendamento.

A resposta mantém `itens`, `pagina`, `tamanho`, `totalItens` e `totalPaginas`. Cada item contém todos os campos atuais de `ColetaResponse`, acrescidos de `ocorrenciaEm`. `visitas` continua representando o histórico do agendamento. O ID é do agendamento e pode se repetir entre ocorrências. A chave do componente no front deve combinar `id` e `ocorrenciaEm`.

O banco guarda o agendamento; a consulta calcula as ocorrências. Não são gravadas novas visitas para montar o calendário. O status exibido é o status atual do agendamento, sem inventar um status próprio por ocorrência.

## Importação de usuários

`POST /api/admin/v1/usuarios/importacao`

Exige administrador. Recebe `application/json`, nunca CSV ou multipart.

```json
{
  "usuarios": [
    {
      "nome": "Pessoa Exemplo",
      "email": "pessoa@example.com",
      "senha": "Exemplo@2026",
      "tipoUsuarioId": 1,
      "dataNascimento": "2000-01-10",
      "cpf": null,
      "avatar": null,
      "endereco": {
        "cep": "01001000",
        "uf": "SP",
        "cidade": "São Paulo",
        "logradouro": "Rua Exemplo",
        "numero": "10",
        "complemento": null
      },
      "condominioId": null
    }
  ]
}
```

O ID de tipo acima é ilustrativo; obtenha os IDs válidos em `/api/admin/v1/catalogos`.

Aceita de 1 a 100 itens. Reutiliza o DTO, mapper, validações, hash e persistência do cadastro individual. Verifica e-mails/CPFs repetidos no lote e no banco, tipos e vínculos com condomínio. Moradores devem informar um condomínio ativo; outros tipos seguem a regra individual existente.

Antes da gravação, os problemas são agregados em `campos`, com índices iniciando em zero. Uma transação envolve o lote inteiro: falhas durante a gravação também revertem usuários, endereços e vínculos já criados naquele lote.

Sucesso: HTTP 200 com `{"importados": 10}`. Dados de entrada ou preflight inválidos: HTTP 400. Restrições de integridade ou conflitos na gravação: HTTP 409, sempre com rollback do lote. Senhas e hashes não são retornados nem adicionados a logs.

```json
{
  "instante": "2026-11-02T12:00:00Z",
  "status": 400,
  "mensagem": "Corrija os dados da importação.",
  "campos": {
    "usuarios.0.email": "E-mail já cadastrado.",
    "usuarios.2.tipoUsuarioId": "Tipo de usuário inválido."
  }
}
```

## Quizzes com histórico

As respostas de criação, consulta, listagem e atualização passam a conter `possuiTentativas` e `totalTentativas`, contando inclusive as tentativas ainda não finalizadas. A quantidade é consultada nas tabelas existentes.

Com tentativas, o `PUT /api/admin/v1/quizzes/{id}` permite título e situação quando a estrutura enviada permanece igual. Curso, aula, perguntas, alternativas, nota mínima e pontos seguem protegidos pela regra existente. Não omita os campos estruturais do PUT: preserve o contrato atual do formulário. Exclusão e alterações estruturais impedidas retornam HTTP 409 com mensagem.

## Tipos administrativos equivalentes

O catálogo mostra uma opção `Administrador`, usando o menor ID existente entre `Admin`, `Administrador` e `Administradora`. A opção contém `idsEquivalentes`, com os IDs reais desse grupo. Os dados persistidos e seus IDs não são migrados ou excluídos.

Filtrar usuários por qualquer um desses IDs retorna todos os administradores equivalentes. O cadastro aceita qualquer ID administrativo válido existente; a resposta preserva o ID real do usuário e padroniza seu nome de tipo como `Administrador`. A autorização continua exigindo o perfil `ADMIN` no token e um tipo administrativo no banco; o token sozinho não concede o privilégio.

No front, ao selecionar o tipo de um usuário legado no catálogo, resolva a opção por:

```typescript
const opcao = tiposUsuarios.find(
  tipo => tipo.id === usuario.tipoUsuarioId ||
    tipo.idsEquivalentes?.includes(usuario.tipoUsuarioId)
);
```

## Coletas do dia

`GET /api/admin/v1/dashboard` acrescenta `coletasHoje`. A contagem cobre o dia local completo em `America/Sao_Paulo`, com as mesmas regras de recorrência do calendário e sem filtro de status. Inclui todos os status, como uma consulta ao calendário sem `statusId`. O front pode usar o campo diretamente; para contar somente um status, consulte o calendário com o filtro desejado.

## Cadastro público de morador: contrato da mobile

O fluxo pertence à `ds-mobile-api`, não à DAD. Rota existente: `POST /api/cadastro/morador`.

Campos confirmados no DTO: `nomeUsuario`, `email`, `senha`, `dataNascimento`, `cpf`, `codigoCondominio`. A data usa `yyyy-MM-dd`. O usuário não precisa conhecer `condominioId`.

A implementação consulta o código de acesso, criptografa a senha e grava o usuário com o vínculo em uma transação. Código inexistente gera `CONDOMINIUM_NOT_FOUND`. A busca é sem distinção de maiúsculas e minúsculas.

Ponto pendente na mobile: o serviço consultado não rejeita explicitamente condomínio inativo antes do vínculo. A confirmação de elegibilidade não deve ser considerada completa até essa regra e seus testes serem adicionados naquele repositório. Este pacote não altera arquivos da mobile nem duplica sua rota.

## Compatibilidade e validação

Os CRUDs existentes, cancelamento sem corpo, registro de visita sem alteração automática de status, edição de quiz com PUT e demais edições com PATCH são preservados. CORS continua configurável por `CORS_ORIGINS`. Não há mudança de segredo JWT nem de schema.

Swagger da DAD: `/swagger-ui.html`; OpenAPI: `/v3/api-docs`.

Testes adicionados: validação do calendário, importação, contratos HTTP e integração com PostgreSQL para recorrência, filtros, ordenação, paginação, fuso, rollback após uma gravação, histórico de quizzes, equivalência administrativa, autorização e dashboard. O teste de contexto passa a reutilizar o PostgreSQL do Testcontainers, sem depender do banco de produção.

Execute com Java 21 e Docker aberto:

```powershell
.\gradlew.bat clean test --no-daemon --max-workers=1 --no-parallel
```

A validação de login, perfil e alteração de senha entre aplicações exige as duas APIs em execução e o mesmo segredo JWT. Use uma conta de teste administrativa: faça login em `/auth/login`, reutilize o token em `/api/usuarios/me` e `/api/admin/v1/catalogos`, e teste `/senhas/alterar` com `senhaAtual`, `novaSenha` e `confirmacaoSenha`. Valide o novo login e restaure a senha da conta de teste ao final. Essa validação entre serviços não é substituída pelos mocks da suíte DAD.
