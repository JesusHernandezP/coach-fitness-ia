# T-32 - Health Connect Sync

## Objetivo

Sincronizar pasos y calorias activas desde relojes Xiaomi via Health Connect en Android hacia el backend para que Web y la IA usen esos datos.

## Flujo decidido

```text
Xiaomi Watch -> Mi Fitness/Zepp Life -> Health Connect -> Android -> Backend -> Supabase -> Web/IA
```

## Alcance

- Integrar Health Connect en Android.
- Leer pasos diarios.
- Leer calorias activas diarias.
- Enviar datos al backend.
- Backend actualiza `ActivityLog` con upsert.
- Web consume datos existentes de actividad.

## Fuera de alcance

- Soporte iPhone automatico.
- Bluetooth directo desde Angular.
- Entrenamientos detallados.
- Frecuencia cardiaca/sueno.

## Backend

### Datos

Extender `activity_logs`:

```sql
ALTER TABLE activity_logs ADD COLUMN source VARCHAR(80);
ALTER TABLE activity_logs ADD COLUMN synced_at TIMESTAMPTZ;
```

### Endpoint

```text
POST /api/v1/health-sync/daily-activity
```

Payload:

```json
{
  "date": "2026-06-15",
  "steps": 8500,
  "caloriesBurned": 430,
  "source": "health_connect"
}
```

### Reglas

- Upsert por `(user_id, date)`.
- Si ya hay actividad manual, definir politica:
  - `health_connect` actualiza pasos/calorias, conserva notas.
  - Si el usuario edito manualmente despues del sync, no sobrescribir sin regla explicita.
- Registrar `synced_at`.

## Android

- Agregar dependencia Health Connect.
- Pantalla/ajuste para conectar permisos.
- Permisos:
  - `Steps`
  - `ActiveCaloriesBurned`
- Boton `Sincronizar actividad`.
- Manejar errores:
  - Health Connect no instalado.
  - Permisos denegados.
  - No hay datos para hoy.

## Web

- Mostrar fuente de actividad si backend la devuelve.
- Mantener entrada manual como fallback.

## Pruebas

Backend:

- Upsert crea actividad.
- Upsert actualiza actividad existente.
- Usuario no modifica datos de otro usuario.

Android:

- Unit tests de mapper/servicio.
- Build debug.
- Validacion manual en dispositivo real Android con Health Connect.

## Criterios de aceptacion

- Android sincroniza pasos/calorias del dia.
- Backend guarda o actualiza `ActivityLog`.
- Web muestra pasos/calorias sincronizados.
- Chat puede usar esos datos en contexto.

