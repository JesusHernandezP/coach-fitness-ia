# T-29 - Nutrition Journal

## Objetivo

Crear el diario nutricional base para registrar comidas y calcular consumo diario contra objetivos.

## Alcance

- Crear entidad `FoodLog`.
- Crear endpoints CRUD de comidas.
- Crear resumen nutricional diario.
- Agregar vista Web basica de comidas de hoy.
- Agregar soporte Android minimo para listar y crear comidas manuales.

## Fuera de alcance

- Estimacion por IA.
- Confirmaciones conversacionales.
- RAG.
- Fotos de comida.

## Backend

### Datos

Crear migracion Flyway:

```sql
CREATE TABLE food_logs (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id),
    date         DATE         NOT NULL,
    meal_type    VARCHAR(50)  NOT NULL,
    description  TEXT         NOT NULL,
    calories     DOUBLE PRECISION NOT NULL,
    protein_g    DOUBLE PRECISION,
    carbs_g      DOUBLE PRECISION,
    fat_g        DOUBLE PRECISION,
    source       VARCHAR(50)  NOT NULL,
    confidence   DOUBLE PRECISION,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ
);

CREATE INDEX idx_food_logs_user_date ON food_logs (user_id, date);
```

### Enumeraciones

- `MealType`: `breakfast`, `lunch`, `dinner`, `snack`, `other`.
- `FoodLogSource`: `manual`, `ai_estimate`.

### Endpoints

```text
POST   /api/v1/food-logs
GET    /api/v1/food-logs?from=&to=
GET    /api/v1/food-logs/today
PUT    /api/v1/food-logs/{id}
DELETE /api/v1/food-logs/{id}
GET    /api/v1/nutrition/today
GET    /api/v1/nutrition/weekly-summary
```

### DTO `DailyNutritionSummary`

```json
{
  "date": "2026-06-15",
  "targetCalories": 2360,
  "consumedCalories": 1280,
  "remainingCalories": 1080,
  "targetProteinG": 160,
  "consumedProteinG": 92,
  "remainingProteinG": 68,
  "targetCarbsG": 286,
  "consumedCarbsG": 140,
  "remainingCarbsG": 146,
  "targetFatG": 64,
  "consumedFatG": 41,
  "remainingFatG": 23,
  "activityCaloriesBurned": 430,
  "netCalories": 850
}
```

### Reglas

- Todas las consultas se filtran por usuario autenticado.
- `calories` requerido y mayor o igual a 0.
- Macros opcionales, pero si se envian deben ser mayores o iguales a 0.
- `remaining*` puede ser negativo si el usuario excede el objetivo.
- `activityCaloriesBurned` sale de `ActivityLog` del dia.

## Web

- Crear servicio `nutrition.service.ts`.
- En dashboard, mostrar tarjeta de resumen diario.
- Crear seccion/listado de comidas de hoy.
- Permitir crear comida manual simple.

## Android

- Agregar DTOs y endpoints en `ApiService`.
- Pantalla o modal simple para registrar comida manual.
- Mostrar resumen diario en dashboard si no rompe el layout actual.

## Pruebas

Backend:

- CRUD de `FoodLog`.
- Seguridad: usuario no accede comidas de otro usuario.
- Calculo de resumen diario.
- Weekly summary con varios dias.

Web:

- `npm run build -- --configuration production`.

Android:

- `./gradlew testDebugUnitTest`.
- `./gradlew assembleDebug`.

## Criterios de aceptacion

- Un usuario puede registrar, listar, editar y borrar comidas.
- El resumen diario calcula consumido/restante.
- Dashboard muestra calorias y proteina de hoy.
- Swagger documenta endpoints nuevos.

