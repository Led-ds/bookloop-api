# bookloop-api

Backend do **BookLoop** — plataforma de compartilhamento responsável de conhecimento.
Java 21 + Spring Boot 3, arquitetura limpa por domínio (DDD), autenticação JWT, PostgreSQL/Flyway
e documentação OpenAPI. Consumido pelo `bookloop-web` e provisionado pelo `bookloop-platform`.

## Configuração e operação (v1.1)

### Variáveis de ambiente

| Variável | Descrição | Default (dev) |
|----------|-----------|---------------|
| `DB_URL` | JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/bookloop` |
| `DB_USER` | Usuário do banco | `bookloop` |
| `DB_PASSWORD` | Senha do banco | `bookloop` |
| `JWT_SECRET` | Segredo HMAC do JWT (**≥ 32 bytes**) | valor de dev (trocar em produção) |
| `JWT_ACCESS_TTL_MS` | TTL do access token | `900000` (15 min) |
| `JWT_REFRESH_TTL_MS` | TTL do refresh token | `1209600000` (14 dias) |
| `APP_CORS_ALLOWED_ORIGINS` | Origens CORS permitidas, separadas por vírgula | `http://localhost:5173,https://kehmmmut47.us-east-1.awsapprunner.com` |
| `SERVER_PORT` | Porta HTTP | `8080` |

Segredos vêm do AWS Secrets Manager via variáveis de ambiente — nunca versionados nem embutidos na imagem.

### Health check

Endpoint público de saúde: **`GET /actuator/health`** → `200 {"status":"UP"}`.
Configure o health check do **App Runner** para este caminho (não usar endpoint de negócio).

### CORS

As origens permitidas são externalizadas em `APP_CORS_ALLOWED_ORIGINS` (mantém `localhost:5173`
para desenvolvimento). Preflight `OPTIONS` habilitado. Não usar `*` em produção.

### Executar localmente

```bash
# 1) Postgres local (exemplo)
docker run --name bookloop-db -e POSTGRES_DB=bookloop -e POSTGRES_USER=bookloop \
  -e POSTGRES_PASSWORD=bookloop -p 5432:5432 -d postgres:16

# 2) API (Flyway aplica V1/V2 no start)
mvn spring-boot:run
# Swagger: http://localhost:8080/swagger-ui.html
```

Usuários de seed (senha `senha12345`): `ana@bookloop.dev`, `bruno@bookloop.dev`, `admin@bookloop.dev`.

### Build e execução com Docker

```bash
docker build -t bookloop-api:latest .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host:5432/bookloop" \
  -e DB_USER=bookloop -e DB_PASSWORD=... \
  -e JWT_SECRET="<segredo-com-32+-bytes>" \
  -e APP_CORS_ALLOWED_ORIGINS="https://SEU-FRONT/,https://outra-origem" \
  bookloop-api:latest
```

### Testes

```bash
mvn clean compile
mvn test
```

---

## Domínio v1.2 — status, perfil, Home pública e edição de livros

### Status de livro (`BookStatus`)

| Status | Significado |
|--------|-------------|
| `AVAILABLE` | Disponível para solicitação. |
| `RESERVED` | Solicitação aprovada, aguardando retirada. |
| `RENTED` | Em posse do leitor. |

Visibilidade é controlada **à parte** por `isPublic` (não é mais um status). Ciclo: cadastro →
`AVAILABLE`; aprovação → `RESERVED`; retirada → `RENTED`; devolução → `AVAILABLE`.

### Status de aluguel (`RentalStatus`)

| Status | Significado |
|--------|-------------|
| `PENDING` | Aguardando resposta do dono. |
| `APPROVED` | Aprovado, aguardando retirada. |
| `ACTIVE` | Empréstimo em andamento. |
| `RETURNED` | Devolvido (final). |
| `OVERDUE` | Atrasado (era `LATE`; renomeado). |
| `REJECTED` | Recusado (final). |
| `CANCELLED` | Cancelado pelo leitor antes da aprovação. |

Transições válidas: `PENDING→APPROVED/REJECTED/CANCELLED`, `APPROVED→ACTIVE`, `ACTIVE→RETURNED/OVERDUE`,
`OVERDUE→RETURNED` (e `OVERDUE→ACTIVE` ao renovar). Transições inválidas são bloqueadas no domínio.

### Perfil do usuário

- `GET /api/v1/users/me` — dados do usuário autenticado (nunca retorna hash de senha).
- `PUT /api/v1/users/me` — atualiza o próprio perfil: `name`, `bio`, `city`, `state`, `addressLine`,
  `neighborhood`, `postalCode`, `avatarUrl` (URL http(s) validada). `email` não é alterado aqui.
  `profileCompleted` é derivado (bio + cidade + estado preenchidos).

### Edição de livros

- `PUT /api/v1/books/{id}` — **apenas o dono** (403 caso contrário; 404 se não existir).
- Bloqueada quando o livro está `RENTED`. `coverUrl` é validado como URL http(s).

### Home pública

- `GET /api/v1/public/home` — **sem autenticação**. Retorna `stats` (totais + `availableBooks`;
  `averageRating` nulo enquanto não há avaliações), `featuredBooks` (públicos, disponíveis, com capa),
  `communityBooks` (públicos recentes), `recentActivities` (derivadas de livros recentes),
  `bookOfTheWeek`, e `reviews`/`topReaders` vazios (sem domínio de avaliação/ranking ainda).
