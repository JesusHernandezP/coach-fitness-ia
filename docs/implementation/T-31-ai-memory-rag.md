# T-31 - AI Memory + RAG

## Objetivo

Agregar memoria semantica y recuperacion de conocimiento para que el coach recuerde preferencias, restricciones e informacion relevante.

## Alcance

- Activar `pgvector` en Supabase/Postgres.
- Crear tabla de memoria/documentos.
- Generar embeddings.
- Recuperar contexto relevante en cada respuesta.
- Guardar memorias importantes del usuario.

## Fuera de alcance

- Fotos.
- Fine-tuning.
- Reemplazar el contexto estructurado.

## Backend

### Datos

Crear migracion:

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai_memories (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users (id),
    type        VARCHAR(80) NOT NULL,
    content     TEXT        NOT NULL,
    embedding   vector(1536),
    importance  INTEGER     NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ai_memories_user_type ON ai_memories (user_id, type);
```

La dimension exacta del vector debe ajustarse al modelo de embeddings elegido.

### Tipos

- `preference`
- `restriction`
- `training_note`
- `nutrition_note`
- `system_knowledge`

### Servicios

- `EmbeddingClient`.
- `AiMemoryService`.
- `RagService`.

### Reglas

- Datos sensibles se guardan solo si aportan valor claro.
- No usar `user_metadata` ni claims JWT para autorizacion.
- Las memorias de usuario siempre se filtran por `user_id`.
- El RAG complementa, no reemplaza, datos estructurados del dia.

## Proveedor de embeddings

Primera opcion a evaluar:

- Usar un proveedor compatible con coste gratuito o bajo.
- Mantener `EmbeddingClient` desacoplado para poder cambiar proveedor.

## Pruebas

Backend:

- Insertar memoria.
- Buscar memoria relevante.
- No recuperar memorias de otro usuario.
- Chat incluye memoria relevante cuando aplica.

## Criterios de aceptacion

- El coach recuerda preferencias confirmadas.
- Una busqueda semantica recupera memorias relevantes.
- El contexto RAG se inyecta en respuestas.
- No rompe respuestas cuando no hay memorias.

