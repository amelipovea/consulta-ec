# Contrato de integración SRI — Fase 3

La aplicación React **no llama al SRI directamente**. Flujo:

`React → POST Orquestador (8080) → POST SRI interno (8081) → HTTPS SRI`.

## Consultar contribuyente

`POST http://127.0.0.1:8080/api/v1/contribuyentes/consultas`

```json
{"correo":"usuario@example.com","ruc":"RUC_AUTORIZADO_13_DIGITOS"}
```

## Consultar vehículo

`POST http://127.0.0.1:8080/api/v1/vehiculos/consultas`

```json
{"correo":"usuario@example.com","ruc":"RUC_AUTORIZADO_13_DIGITOS","placa":"ABC-1234"}
```

Sustituye los marcadores por datos propios o autorizados en Postman/PowerShell. No subas datos reales ni capturas sin anonimizar.

**Respuesta exitosa (estructura, ejemplo de campos sintéticos; no es una consulta oficial):**

```json
{
  "tipoConsulta": "CONTRIBUYENTE", "estado": "OK", "fuente": "SRI",
  "datos": {"ruc": "1712345678001", "tipoContribuyente": "PERSONA NATURAL"},
  "metadatos": {"consultadoEn": "2026-11-17T00:00:00Z", "desdeCache": false}
}
```

El RUC indicado es sintético y se utiliza solamente para ilustrar la forma del JSON.

## Endpoints del proveedor

- `GET /sri-catastro-sujeto-servicio-internet/rest/ConsolidadoContribuyente/existePorNumeroRuc?numeroRuc=`
- `GET /sri-catastro-sujeto-servicio-internet/rest/ConsolidadoContribuyente/obtenerPorNumerosRuc?ruc=`
- `GET /sri-matriculacion-vehicular-recaudacion-servicio-internet/rest/BaseVehiculo/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn?numeroPlacaCampvCpn=`

El normalizador reconoce `tipoContribuyente=PERSONA NATURAL` (o `NATURAL`) y un subconjunto de campos; no asume datos ausentes. Si el proveedor cambia su contrato o no es accesible, mostramos error.

Las pruebas de integración usan un servidor HTTP local simulado para verificar rutas GET, parámetros, respuestas y errores técnicos. **No se registró una respuesta SRI en vivo** porque no se usaron identificadores personales ajenos o no autorizados. Los tiempos configurados son 1500 ms para conexión y 4500 ms para lectura; pueden ajustarse mediante `SRI_CONEXION_MS` y `SRI_LECTURA_MS`.

## Interpretación de resultados

- `400 VALIDACION_ERROR` = entrada con formato incorrecto.
- `404 RUC_NO_ENCONTRADO` / `VEHICULO_NO_ENCONTRADO` = proveedor sin resultado.
- `422 NO_ES_PERSONA_NATURAL` = SRI identificó otro tipo de contribuyente.
- `502 ...` = proveedor devolvió un contrato inesperado.
- `503 ...` = proveedor no disponible.

La consulta ANT sigue en fase 4. No hay caché SRI en esta implementación.
