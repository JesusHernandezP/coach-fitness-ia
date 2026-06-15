# T-34 - AI Weekly Review

## Objetivo

Generar una revision semanal personalizada con la IA usando diario nutricional, actividad, peso, objetivo y memoria.

## Alcance

- Crear endpoint de revision semanal.
- Preparar contexto estructurado.
- Generar resumen, hallazgos y recomendaciones.
- Mostrarlo en Web.

## Fuera de alcance

- Crear planes cerrados de entrenamiento de varias semanas.
- Diagnostico medico.
- Fotos.

## Backend

### Endpoint

```text
GET /api/v1/coach/weekly-review
```

### Respuesta

```json
{
  "periodStart": "2026-06-09",
  "periodEnd": "2026-06-15",
  "summary": "Esta semana cumpliste 5 de 7 dias...",
  "nutritionFindings": ["Proteina media baja frente al objetivo"],
  "activityFindings": ["Pasos consistentes durante 4 dias"],
  "weightFindings": ["Peso bajo 0.4 kg"],
  "recommendations": ["Sube proteina en desayuno", "Mantiene calorias objetivo"],
  "riskNotes": ["Las estimaciones de comidas pueden tener margen de error"]
}
```

### Reglas

- Si hay pocos datos, explicarlo claramente.
- No inventar pesos, comidas ni actividad.
- No dar diagnosticos medicos.
- Recomendaciones accionables y breves.

## Web

- Tarjeta `Revision semanal IA`.
- Boton `Generar revision`.
- Mostrar fecha y recomendaciones.

## Android

- Opcional en esta fase.
- Si se implementa, mostrar resumen simple.

## Pruebas

Backend:

- Contexto semanal con datos completos.
- Contexto con datos insuficientes.
- LLM mockeado en tests.
- Seguridad por usuario.

## Criterios de aceptacion

- La IA genera una revision semanal coherente con datos reales.
- No falla cuando faltan comidas o pesos.
- Las recomendaciones son consistentes con objetivo del usuario.

