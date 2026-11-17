# Fase 3 — Integración SRI

## Alcance

Se implementan tres GET HTTP reales hacia el SRI **desde el microservicio** Java, sin cambiar los contratos públicos de la fase 2:

1. Validación de existencia del RUC.
2. Recuperación de información y verificación de persona natural.
3. Consulta por placa **después** de comprobar persona natural.

Se añadieron adaptador HTTP (timeouts), normalizador de datos, controladores internos, errores JSON y pruebas locales con servidor HTTP simulado.

## Respuestas internas

- `200 OK`: `estado`, `fuente`, `datos`, `metadatos`.
- `404 RUC_NO_ENCONTRADO` y `404 VEHICULO_NO_ENCONTRADO`.
- `422 NO_ES_PERSONA_NATURAL`.
- `502 RESPUESTA_SRI_INVALIDA` cuando no se puede comprobar con seguridad el resultado.
- `503 SRI_NO_DISPONIBLE` ante problemas de acceso o de red.

## Verificación

1. Ejecutar `./mvnw.cmd -B clean test` desde la raíz.
2. Reiniciar **orquestador y SRI** si estaban activos; ANT y React permanecen sin cambios.
3. Ejecutar `./probar-fase3.cmd`: verifica salud y validación sin datos reales.
4. Opcional: con datos **propios o autorizados**, usar los ejemplos de `docs/contratos/FASE_3_SRI.md` para una consulta real sin publicar la respuesta.

## Limitaciones

Los dos servicios externos podrían no responder o cambiar su contrato. No se suplanta el resultado del SRI con datos sintéticos. El servicio ANT sigue pendiente de fase 4 y deberá devolver `501` cuando reciba una solicitud válida.
