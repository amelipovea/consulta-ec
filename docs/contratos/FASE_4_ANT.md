# Contrato ANT (fase 4)

Ruta interna `POST http://127.0.0.1:8082/api/v1/interno/licencias/puntos/consultas`

Cuerpo de prueba, **identificación sintética**:

```json
{"cedula":"9999999999"}
```

Éxito **solo en simulación explícita**:

```json
{
  "estado": "OK",
  "fuente": "SIMULACION_ANT",
  "datos": {"puntos": 20},
  "metadatos": {"consultadoEn": "2026-11-17T01:00:00Z", "desdeCache": false}
}
```

Si falla el simulador y hay caché válida:

```json
{
  "estado": "OK",
  "fuente": "SIMULACION_ANT_CACHE",
  "datos": {"puntos": 20},
  "metadatos": {"consultadoEn": "2026-11-17T01:00:00Z", "desdeCache": true}
}
```

Las fechas anteriores son solo ejemplos. El servicio utiliza la hora real de la consulta de origen. La caché está en Redis local en Docker, no en Redis Cloud. El TTL local predeterminado es 86400 segundos y el secreto HMAC debe conservarse entre reinicios.

Errores: `400 VALIDACION_ERROR`, `503 ANT_NO_DISPONIBLE` o `503 ANT_SIN_CONTRATO_PUNTOS`, `502 RESPUESTA_ANT_INVALIDA`. Todos bajo `error.codigo` y con `solicitudId`. Sin información válida en caché, devuelve indisponibilidad, nunca cero puntos inventados.

La API pública permanece `POST /api/v1/licencias/puntos/consultas` en puerto 8080, con `correo` y `cedula`, y transforma el contrato interno existente. El orquestador representa un 503 interno como `503 DEPENDENCIA_NO_DISPONIBLE`, conservando el formato público de error. Si el dato es inválido y no hay caché, representa el 502 interno como `502 ERROR_SERVICIO_INTERNO`. Ninguno de esos errores contiene puntos inventados.
