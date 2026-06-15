# Roadmap - AI Coach y diario fitness

## Vision

Convertir Fitness AI Coach en un diario fitness inteligente:

```text
Usuario + reloj + chat natural -> backend -> Supabase -> coach IA + dashboard
```

La IA actua como nutricionista y entrenador personal, usando objetivos, historico, comidas, actividad y memoria para recomendar acciones.

## Orden de implementacion recomendado

1. `T-28` Personalizacion de usuario.
2. `T-29` Nutrition Journal.
3. `T-30` AI Coach Tools.
4. `T-33` Dashboard KPIs y evolucion.
5. `T-32` Health Connect Sync.
6. `T-31` AI Memory + RAG.
7. `T-34` AI Weekly Review.

## Estrategia de ramas

Cada ticket debe implementarse en una rama independiente:

```text
feature/t-28-user-personalization
feature/t-29-nutrition-journal
feature/t-30-ai-coach-tools
feature/t-33-dashboard-kpis
feature/t-32-health-connect-sync
feature/t-31-ai-memory-rag
feature/t-34-ai-weekly-review
```

Cada rama debe abrir PR hacia `main`, pasar CI y desplegar en Render/Vercel si aplica.

## Instrucciones obligatorias para agentes

Antes de implementar cualquier ticket, el agente debe leer este roadmap y el spec del ticket concreto. No debe implementar varios tickets a la vez.

Flujo obligatorio al comenzar:

```text
git checkout main
git pull --ff-only origin main
git checkout -b feature/t-XX-nombre-del-ticket
```

Reglas:

- Implementar solo el ticket solicitado.
- No adelantar dependencias de tickets futuros salvo que el spec lo pida explicitamente.
- No mezclar refactors no relacionados.
- No modificar secretos ni archivos `.env` reales.
- Toda tabla nueva debe ir en una migracion Flyway nueva.
- Todo endpoint nuevo debe tener DTOs claros, validacion, seguridad por usuario y Swagger/OpenAPI.
- Si un cambio toca Web o Android, mantener compatibilidad con el backend desplegado o documentar la dependencia del PR.
- Antes de finalizar, ejecutar las pruebas indicadas en el spec del ticket.
- Si alguna prueba no puede ejecutarse localmente, explicar el motivo exacto en el cierre del trabajo.

Flujo obligatorio al terminar:

```text
git status
git add <archivos-del-ticket>
git commit -m "<conventional-commit>"
git push -u origin feature/t-XX-nombre-del-ticket
```

Luego abrir PR hacia `main`. No hacer push directo a `main`, porque la rama esta protegida.

Plantilla recomendada para pedir implementacion a un agente:

```text
Implementa T-XX siguiendo docs/implementation/T-XX-archivo.md.

Trabaja en una rama feature/t-XX-nombre desde main.
No implementes otros tickets.
Ejecuta las pruebas indicadas en el spec.
Haz commit con Conventional Commit.
Sube la rama y deja listo el PR hacia main.
```

## Reglas generales

- No mezclar tickets funcionales en un mismo PR.
- Toda tabla nueva requiere migracion Flyway.
- Todo endpoint nuevo requiere Swagger/OpenAPI.
- Toda respuesta debe estar filtrada por usuario autenticado.
- El chat no debe guardar acciones ambiguas sin confirmacion.
- Health Connect es puente Android; Angular consume el backend.
- Fotos de comida quedan fuera hasta nuevo ticket.

## Dependencias entre tickets

```text
T-28 -> independiente
T-29 -> base para T-30, T-33 y T-34
T-30 -> depende de T-29
T-33 -> depende de T-29; mejora si T-32 existe
T-32 -> puede ir despues de T-29 o T-33
T-31 -> depende de T-30 para aportar valor real
T-34 -> depende de T-29; mejora con T-31 y T-32
```

## Verificacion minima por PR

Backend:

```text
mvn verify
```

Web:

```text
npm run build -- --configuration production
```

Android:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Si un ticket toca solo una capa, ejecutar al menos esa capa y documentar lo no ejecutado.
