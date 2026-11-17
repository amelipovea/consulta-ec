# ConsultaEC – Contrato HTTP v1 (fase 2)

## Alcance y arquitectura

React → Orquestador (`:8080`) → Servicio SRI (`:8081`) / Servicio ANT (`:8082`).

**Fase 2** crea y prueba los contratos y validaciones del orquestador. Los servicios de fase 1
solo tienen rutas de estado, por lo que **una solicitud válida de negocio devuelve 501**
si el microservicio está activo, pero todavía no implementa su ruta interna.
Esto **no significa** que el SRI ni la ANT hayan sido consultados.

Los ejemplos usan RUC y cédulas **sintéticos para probar formatos**, no identidades verificadas.

## API pública – solicitudes

Todas las solicitudes son `POST` con `Content-Type: application/json`.

### 1. Contribuyentes

`POST http://127.0.0.1:8080/api/v1/contribuyentes/consultas`

```json
{"correo":"prueba@ejemplo.com","ruc":"1712345678001"}
```

Validaciones: correo no vacío y con formato de email, RUC de exactamente 13 números.
**La existencia, condición de persona natural y dígito verificador serán responsabilidad del SRI**, no de estas validaciones de formato.

Ruta interna futura: `POST http://127.0.0.1:8081/api/v1/interno/contribuyentes/consultas`

```json
{"ruc":"1712345678001"}
```

### 2. Vehículos

`POST http://127.0.0.1:8080/api/v1/vehiculos/consultas`

```json
{"correo":"prueba@ejemplo.com","ruc":"1712345678001","placa":"ABC-1234"}
```

Validaciones: correo, RUC y placa ecuatoriana básica (3 letras + 3 o 4 números; guion opcional).
La aplicación convierte la placa a mayúsculas antes de delegar.
**En fase 3**, el SRI verificará el RUC y que pertenezca a una persona natural antes de buscar la placa.

Ruta interna futura: `POST http://127.0.0.1:8081/api/v1/interno/vehiculos/consultas`

```json
{"ruc":"1712345678001","placa":"ABC-1234"}
```

### 3. Puntos de licencia

`POST http://127.0.0.1:8080/api/v1/licencias/puntos/consultas`

```json
{"correo":"prueba@ejemplo.com","cedula":"1712345678"}
```

Validaciones: correo, cédula de 10 dígitos. El formato no confirma identidad ni puntos.

Ruta interna futura: `POST http://127.0.0.1:8082/api/v1/interno/licencias/puntos/consultas`

```json
{"cedula":"1712345678"}
```

## Respuesta de éxito – contrato interno que implementarán las fases 3 y 4

Los microservicios responderán `200 OK` con esta estructura común:

```json
{
  "estado": "OK",
  "fuente": "SRI",
  "datos": {"ejemplo": "respuesta ficticia, no oficial"},
  "metadatos": {"consultadoEn": "2026-11-17T12:00:00Z", "desdeCache": false}
}
```

El orquestador añadirá `tipoConsulta` y devolverá al frontend:

```json
{
  "tipoConsulta": "CONTRIBUYENTE",
  "estado": "OK",
  "fuente": "SRI",
  "datos": {"ejemplo": "respuesta ficticia, no oficial"},
  "metadatos": {"consultadoEn": "2026-11-17T12:00:00Z", "desdeCache": false}
}
```

`tipoConsulta`: `CONTRIBUYENTE`, `VEHICULO` o `PUNTOS_LICENCIA`.
`fuente`: el microservicio declara la procedencia real: `SRI`, `ANT`, `REDIS_CLOUD` o
`SIMULADO` (este último **no representa datos oficiales**). El orquestador no inventa ni
reemplaza la fuente. En un resultado de Redis, `desdeCache=true` y la fecha identifica
cuándo se obtuvo el dato original. El proveedor debe entregar una fecha ISO-8601 UTC válida.

**El código de éxito aquí es un contrato previsto**, no una prueba exitosa contra SRI o ANT.

## Contrato uniforme de errores

```json
{
  "error": {
    "codigo": "VALIDACION_ERROR",
    "mensaje": "Corrige los campos indicados",
    "solicitudId": "uuid-para-identificar-la-solicitud",
    "instante": "2026-11-17T12:00:00Z",
    "detalles": [{"campo":"ruc","mensaje":"El RUC debe tener exactamente 13 dígitos"}]
  }
}
```

| HTTP | Código de error | Situación |
|---|---|---|
| 400 | `VALIDACION_ERROR` | Campos faltantes o inválidos |
| 400 | `CUERPO_JSON_INVALIDO` | JSON mal formado o ausente |
| 415 | `TIPO_CONTENIDO_NO_SOPORTADO` | No se envió JSON |
| 501 | `CONTRATO_INTERNO_PENDIENTE` | Microservicio responde 404 sin código documentado (rutas futuras faltantes) |
| 503 | `DEPENDENCIA_NO_DISPONIBLE` | Microservicio inaccesible o devuelve 503 |
| 502 | `CONTRATO_INTERNO_INVALIDO` | JSON de éxito del microservicio no cumple contrato |
| 502 | `ERROR_SERVICIO_INTERNO` | Otros errores HTTP técnicos de microservicio |
| 404 | `RUC_NO_ENCONTRADO`, `VEHICULO_NO_ENCONTRADO`, `PUNTOS_NO_ENCONTRADOS` | Futuro error 404 explícito de microservicio |
| 422 | `NO_ES_PERSONA_NATURAL` | Futuro error 422 explícito del SRI |
| 500 | `ERROR_INTERNO` | Fallo no previsto del orquestador |

En fase 3, el servicio SRI deberá emitir 404 o 422 **con el JSON de error** de arriba.
Un 404 de ruta sin JSON estructurado corresponde a `501 CONTRATO_INTERNO_PENDIENTE`.
Nunca registrar RUC, cédula o correo en mensajes públicos de error.

## Alcance NO implementado en fase 2

- Integración real SRI y validación de persona natural.
- Consulta automática ANT y su interpretación.
- Redis Cloud, TTL y recuperación de último dato válido.
- Formulario de negocio React (sigue con el panel de fase 1).

## Compatibilidad y privacidad

- No se rompe `/api/v1/sistema/estado` ni los health checks de fase 1.
- El correo es obligatorio en la API pública pero **no se transmite** a las otras aplicaciones.
- No usar información personal real en capturas ni en el repositorio público.
- Los timeouts son configurables con `CONSULTAEC_HTTP_CONEXION_MS` y `CONSULTAEC_HTTP_LECTURA_MS`.
