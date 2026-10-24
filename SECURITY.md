# Política de Segurança

O ds-dad-api está em fase inicial: hoje é apenas o esqueleto de uma API Spring Boot, sem banco de dados, autenticação ou tratamento de dados pessoais implementados.

## Versões suportadas

| Versão | Suporte |
| --- | --- |
| `main` (mais recente) | Sim |
| Demais branches | Não |

## Reportando uma vulnerabilidade

**Não abra issue, Pull Request ou discussão pública** para relatar vulnerabilidades.

Use o relato privado do GitHub: <https://github.com/EcoCiente-Instituto-J-F/ds-dad-api/security/advisories/new> (Security → Advisories → Report a vulnerability).

> Mantenedores: ative *Private vulnerability reporting* em Settings → Code security.

Inclua no relato:

- o impacto da falha;
- os passos para reproduzir, usando **dados fictícios**;
- o commit testado e a configuração;
- se possível, uma ideia de correção.

Depois do relato: confirmaremos o recebimento, corrigiremos de forma privada e publicaremos um advisory com crédito a quem reportou, se desejado. Pedimos um prazo razoável para a correção antes de qualquer divulgação pública.

## Escopo

**Dentro do escopo**

- Segredos commitados no repositório
- Vulnerabilidades em dependências com impacto real no projeto
- Falhas no workflow do bot de PR (`pr-bot.yml`) ou do CI (`ci.yml`) que exponham segredos (`GEMINI_API_KEY`, `DOCKERHUB_TOKEN`, `CI_DISPATCHER_PRIVATE_KEY`)
- Falhas de segurança nos endpoints que vierem a ser implementados

**Fora do escopo**

- Bugs sem impacto de segurança
- Falhas em serviços de terceiros (GitHub, Docker Hub)
- Negação de serviço no próprio deploy

## Como o sistema protege os dados

| Camada | Onde | O que faz |
| --- | --- | --- |
| Contêiner | `Dockerfile` | Executa a aplicação com usuário não-root (`spring`) |
| Segredos | `.gitignore` | Mantém arquivos `.env` fora do repositório |
| Bot de PR | `.github/workflows/pr-bot.yml` | Só roda para branches do próprio repositório, com checkout do código da branch base |

## Checklist de deploy

- Segredos fora de imagem, repositório e arquivos de configuração versionados.
- Ao adicionar endpoints, adicionar também autenticação e autorização antes do deploy.
- Não expor `/swagger-ui.html` publicamente em produção sem necessidade.
- Não expor endpoints internos ao público.
