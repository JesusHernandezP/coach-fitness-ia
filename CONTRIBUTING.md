# Guia de contribucion

## Flujo de ramas

```
main      ← produccion (protegida, solo merge via PR)
develop   ← integracion continua
feature/<ticket>  ← trabajo por ticket (p.ej. feature/t-02-auth-jwt)
fix/<descripcion>
```

Nunca commitear directamente a `main` ni `develop`.

## Conventional Commits

Formato: `<tipo>(<ambito>): <descripcion breve>`

| Tipo | Cuando usarlo |
|------|--------------|
| `feat` | Nueva funcionalidad |
| `fix` | Correccion de bug |
| `docs` | Solo documentacion |
| `refactor` | Cambio de codigo sin fix ni feature |
| `test` | Añadir o modificar tests |
| `chore` | Build, dependencias, CI |
| `style` | Formato (no afecta logica) |

Ejemplos:
```
feat(backend): add JWT authentication filter
fix(web): correct macro calculation display rounding
test(backend): add unit tests for NutritionCalculatorService
chore(ci): add Android CI workflow
```

## Pull Requests

1. Crear rama `feature/<ticket>` desde `develop`.
2. Commits con Conventional Commits.
3. Abrir PR a `develop` cuando este listo.
4. CI debe estar en verde (lint + tests).
5. Descripcion del PR: que cambia, como probarlo.
6. Merge con squash o merge commit (no rebase forzado).

## Versionado

SemVer: `vMAJOR.MINOR.PATCH`
- MAJOR: cambio incompatible de API.
- MINOR: nueva funcionalidad compatible.
- PATCH: fix compatible.

Actualizar `CHANGELOG.md` antes de cada release.

## Tests

- Backend: cobertura minima 70% en capa de servicios (`./mvnw verify`).
- Web: smoke test por componente (`npm test`).
- Android: unit tests para ViewModels (`./gradlew testDebugUnitTest`).

## Lint y formato

```bash
# Backend
./mvnw spotless:apply

# Web
npm run lint -- --fix
npx prettier --write src/

# Android
./gradlew ktlintFormat
```
