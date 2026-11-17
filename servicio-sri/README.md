# Servicio SRI — Fase 3

Microservicio independiente Spring Boot/Java 17 que consume endpoints del SRI desde el servidor.

## Rutas internas usadas por el orquestador

- `POST /api/v1/interno/contribuyentes/consultas` — JSON `{"ruc":"..."}`.
- `POST /api/v1/interno/vehiculos/consultas` — JSON `{"ruc":"...", "placa":"ABC-1234"}`.

Una solicitud válida responde con `estado=OK`, `fuente=SRI`, `datos` y `metadatos` (incluyendo `desdeCache=false`).

## Proveedor externo

Tres GET del enunciado: `existePorNumeroRuc`, `obtenerPorNumerosRuc`, `obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn`.

- Validación del RUC antes de consultar matrícula.
- Solo se permite persona natural explícitamente identificada por el SRI.
- Se comprueba que el RUC retornado coincida exactamente con el solicitado.
- Las respuestas externas ambiguas/HTML producen `502` y no se inventan valores.
- Timeout, bloqueo externo y errores técnicos pueden resultar en `503`.
- Pruebas unitarias usan servidor HTTP local y datos sintéticos.

Configurar con `SRI_URL_BASE`, `SRI_CONEXION_MS`, `SRI_LECTURA_MS`. El valor predeterminado de `SRI_URL_BASE` es el dominio del SRI.

**Limitación:** no se garantiza que los endpoints externos sigan accesibles, ni que todos los formatos de respuesta estén documentados. Las pruebas locales no equivalen a una consulta exitosa en producción.
