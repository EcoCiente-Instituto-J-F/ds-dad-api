# Solicitante autenticado na criação de agendamentos

A criação usa o ID do `UsuarioAutenticado` fornecido pelo fluxo JWT. O controller exige autenticação confirmada e um principal com ID positivo. O service encaminha `Coleta.Criacao`, que reúne os dados do agendamento e o ID do solicitante, até o gateway.

Antes de gravar, o gateway consulta o usuário com bloqueio e exige que ainda exista e esteja ativo. IDs ausentes ou inválidos e usuários inexistentes/inativos interrompem o fluxo com HTTP 401. Não existe mais uma alternativa de criação por `salvar(null, dados)` sem solicitante.

A entidade mapeia `tb_agendamentos_coletas.solicitado_por_usuario_id` como obrigatória e não atualizável (`nullable = false`, `updatable = false`). `ColetaPersistenceMapper.toNovaEntity` preenche o campo na criação; `atualizar` preserva o solicitante original. Outro administrador pode editar o agendamento sem assumir sua autoria.

O DTO HTTP continua sem campo de solicitante e rejeita propriedades não declaradas, incluindo tentativas de enviar o solicitante no JSON. Nenhum ID de produção foi fixado. As respostas existentes não foram ampliadas.

## Testes

Foram acrescentados testes de controller, service, mapper, gateway e integração para origem do ID na autenticação, principal inválido/anônimo, usuário inexistente/inativo, rejeição de solicitante no JSON e preservação do solicitante após edição por outro administrador.

O schema de testes versionado no GitHub não tinha essa coluna, embora ela já seja obrigatória no banco usado pela aplicação. A preparação de `AbstractIntegrationTest` passa a reproduzir a coluna e seu NOT NULL somente no PostgreSQL efêmero do Testcontainers. Esse código não é executado na inicialização da aplicação e não altera o banco de produção. Nenhuma migration nem remoção de NOT NULL foi incluída.

Execute com Docker aberto:

```powershell
.\gradlew.bat clean test --no-daemon --max-workers=1 --no-parallel
```

Os testes de integração compilam, mas precisam rodar com Docker no ambiente de desenvolvimento. O aplicador cria backup e confere os arquivos antes de escrever; não faz commit ou push.
