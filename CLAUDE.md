# CLAUDE.md

Guidance for working in this repository.

## Overview

Monorepo for a standalone authentication/authorization service:

- `backend/` — Spring Boot 3.4 (Java 21) API issuing self-signed RSA JWTs. Spring Security OAuth2 Resource Server, Spring Data JPA + PostgreSQL, Liquibase, springdoc (Swagger UI).
- `frontend/` — React + TypeScript (Vite), Tailwind CSS v4 and [neobrutalism.dev](https://www.neobrutalism.dev/) components (shadcn-style, in `src/components/ui`).
- `docker-compose.yml` — `db` (Postgres 17), `api` (port 8080) and `web` (nginx, port 4200, proxies `/api` to `api`).

## Commands

Backend (from `backend/`):

```bash
./mvnw spring-boot:run   # needs Postgres + env vars from .env exported
./mvnw test
```

Frontend (from `frontend/`):

```bash
npm install
npm start          # Vite dev server on :4200, /api proxied to localhost:8080
npm run build      # tsc -b + vite build -> dist/
npm run lint       # oxlint
```

Everything: `docker compose up -d --build` (copy `.env.example` to `.env` first; `JWT_PUBLIC_KEY`/`JWT_PRIVATE_KEY` are RSA PEMs, see README).

## Architecture notes

- Backend package root: `com.lucas.auth` with `configs/`, `controllers/`, `dtos/`, `entities/`, `exceptions/`, `repositories/`, `services/`.
- Access tokens (5 min) carry roles in a `scope` claim and are validated statelessly; refresh tokens (24h) carry only identity. Roles: `USER`, `SUPERUSER`. Owner-or-`SUPERUSER` checks use `@PreAuthorize`.
- Entities are never exposed through the API; always go through DTO records.
- Schema changes go through Liquibase changelogs, never `ddl-auto`.
- Public endpoints: `POST /api/user`, `POST /api/login`, `POST /api/refresh` (and `/error`). Frontend `src/core/api.ts` keeps the same list in `PUBLIC_URLS` — update both together.
- Frontend: `src/core/api.ts` (fetch wrapper, token storage, 401 handler), `src/core/auth.tsx` (context), `src/core/RequireAuth.tsx` (route guard), `src/pages/*`. Use `@/` for imports from `src`.
- Add UI components with `npx shadcn@latest add https://neobrutalism.dev/r/<component>.json`. Style with Tailwind utilities only; no SCSS/CSS modules.
- The seeded `superuser` account (`SuperuserConfig`) is for local dev only.

## Conventions

- Conventional Commits (`feat`, `fix`, `refactor`, `docs`, `test`, `ci`, ...), scoped when useful, e.g. `feat(frontend): ...`.
- No `Co-Authored-By` trailers in commits.
- Frontend formatting: Prettier (no semicolons, single quotes, width 100).
- Keep the README in sync when behavior, endpoints or layout change.

## CI

`.github/workflows/ci.yml` runs on PRs and pushes to `main`: backend `./mvnw verify`, frontend `npm ci` + lint + build, then a Docker build of both images. Backend tests use H2 (profile `test`), so no Docker/Postgres is needed to run them.

## Roadmap (what is still missing)

1. **Tests**
   - Frontend: set up Vitest + React Testing Library; cover `api.ts`, the auth context, the guard and the login/register forms.
2. **User management UI** (frontend)
   - `SUPERUSER`-only page listing users (`GET /api/user`).
   - Edit (`PUT /api/user/{username}`) and delete (`DELETE /api/user/{username}`) of the own account, and of any account for `SUPERUSER`.
3. **Token refresh** — the frontend stores only the access token and never calls `/api/refresh`. Store the refresh token, and on a 401 from a protected call try one refresh, retry the request, and only log out if that fails. Logout should also clear the refresh token.
