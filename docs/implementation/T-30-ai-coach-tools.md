# T-30 - AI Coach Tools

## Objetivo

Convertir el chat en un coach capaz de usar contexto real del dia y registrar comidas estimadas solo tras confirmacion del usuario.

## Alcance

- Crear servicio de contexto IA.
- Estimar comidas desde texto natural.
- Proponer registro de comida.
- Registrar comida cuando el usuario confirme.
- Responder preguntas como `cuanto me falta hoy`.

## Fuera de alcance

- RAG vectorial.
- Fotos de comida.
- Health Connect.
- Planes de entrenamiento estructurados.

## Backend

### Servicios

- `AiContextService`: obtiene perfil, objetivos, comidas de hoy, actividad de hoy, peso reciente y resumen diario.
- `AiCoachService`: orquesta mensajes, contexto, estimacion y acciones.
- `FoodEstimationService`: pide al LLM estimacion nutricional estructurada.
- `PendingAiActionService`: guarda acciones pendientes de confirmacion.

### Datos

Crear tabla de acciones pendientes:

```sql
CREATE TABLE pending_ai_actions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users (id),
    conversation_id BIGINT       NOT NULL REFERENCES chat_conversations (id),
    action_type     VARCHAR(80)  NOT NULL,
    payload         JSONB        NOT NULL,
    status          VARCHAR(40)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    expires_at      TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_pending_ai_actions_user_conversation
    ON pending_ai_actions (user_id, conversation_id, status);
```

### Acciones iniciales

- `create_food_log`.

### Flujo

1. Usuario escribe comida en lenguaje natural.
2. IA estima macros/calorias.
3. Backend guarda `pending_ai_action`.
4. IA responde con estimacion y pregunta si registra.
5. Usuario confirma.
6. Backend crea `FoodLog` con `source=ai_estimate`.
7. IA responde con resumen actualizado.

### Reglas

- No guardar comida estimada sin confirmacion.
- Acciones pendientes expiran en 30 minutos.
- Si el usuario corrige valores, registrar la version corregida.
- Si no hay perfil/targets, pedir completar perfil.

### Endpoints

Se mantiene el endpoint de chat:

```text
POST /api/v1/chat/conversations/{id}/messages
```

Opcional si simplifica la UI:

```text
POST /api/v1/chat/conversations/{id}/actions/{actionId}/confirm
POST /api/v1/chat/conversations/{id}/actions/{actionId}/reject
```

## Web

- Mostrar respuestas de confirmacion de forma clara.
- Botones: `Registrar`, `Editar`, `Descartar` si el backend expone action IDs.
- Si se mantiene confirmacion por texto, aceptar `si, registralo`.

## Android

- Soportar el mismo flujo que Web.
- Minimo: confirmacion por texto.

## Pruebas

Backend:

- Construccion de contexto con datos de usuario.
- Deteccion de confirmacion.
- Creacion de `pending_ai_action`.
- Confirmacion crea `FoodLog`.
- Accion expirada no se ejecuta.
- Rate limit sigue funcionando.

## Criterios de aceptacion

- La IA puede responder `cuanto me falta hoy` con datos reales.
- La IA estima una comida y pide confirmacion.
- Al confirmar, se crea `FoodLog`.
- El resumen diario se actualiza.

