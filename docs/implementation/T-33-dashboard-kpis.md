# T-33 - Dashboard KPIs y evolucion

## Objetivo

Transformar el dashboard en un panel de seguimiento semanal y de evolucion con KPIs claros.

## Alcance

- KPIs diarios y semanales.
- Graficos de evolucion.
- Integrar nutricion, actividad y peso.
- Mostrar senales de progreso o involucion.

## Fuera de alcance

- RAG.
- Health Connect si T-32 no esta listo.
- Recomendacion semanal generada por IA.

## Backend

### Endpoints

```text
GET /api/v1/dashboard/today
GET /api/v1/dashboard/weekly-kpis
GET /api/v1/dashboard/nutrition-trend?days=30
GET /api/v1/dashboard/activity-trend?days=30
GET /api/v1/dashboard/adherence?days=30
```

### KPIs diarios

- Calorias objetivo.
- Calorias consumidas.
- Calorias restantes.
- Proteina objetivo/consumida/restante.
- Pasos.
- Calorias quemadas.
- Peso actual.

### KPIs semanales

- Dias con comidas registradas.
- Promedio calorias consumidas.
- Promedio proteina.
- Promedio pasos.
- Calorias activas totales.
- Cambio de peso semanal.
- Adherencia calorica.
- Racha de registro.

### Graficos

- Peso en el tiempo.
- Calorias consumidas vs objetivo.
- Proteina consumida vs objetivo.
- Pasos diarios.
- Calorias quemadas diarias.
- Adherencia diaria.

## Web

Reorganizar dashboard en secciones:

1. Hoy.
2. Semana.
3. Evolucion.
4. Diario reciente.

Principios UI:

- Denso pero legible.
- Tarjetas KPI compactas.
- Graficos con etiquetas claras.
- No ocultar informacion critica detras del chat.

## Android

- Mantener dashboard simple.
- Mostrar al menos KPIs diarios principales.

## Pruebas

Backend:

- Calculo de KPIs sin datos.
- Calculo con datos parciales.
- Calculo con multiples dias.
- Seguridad por usuario.

Web:

- Build production.
- Verificacion visual manual en desktop y mobile.

## Criterios de aceptacion

- El usuario ve estado de hoy en menos de 5 segundos.
- El usuario puede comparar semana actual con objetivo.
- Los graficos muestran evolucion de peso, comida y actividad.
- El dashboard funciona aunque falten datos.

