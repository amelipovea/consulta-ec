# Fase 5 — Interfaz React integrada con ConsultaEC

## Objetivo

Reemplazar la pantalla de estado de la fase 1 por un portal funcional de consultas, sin cambiar la arquitectura C4 existente.

### Arquitectura conservada

`React (5173) → Orquestador (8080) → SRI (8081) / ANT (8082) → Redis local en Docker (solo ANT)`

La fase modifica únicamente el frontend y scripts/documentación. Las rutas HTTP fueron tomadas de los contratos de las fases 2–4 y no se inventaron otras rutas.

## Funciones implementadas

1. Formulario de correo, RUC y consulta de contribuyente.
2. Consulta vehicular habilitada solo después de recibir `estado=OK` en la respuesta de contribuyente.
3. Consulta de puntos ANT independiente mediante cédula.
4. Visualización de campos normalizados, errores estructurados, fuente y fecha de consulta.
5. Marcado explícito de simulación y caché obsoleta para evitar presentar datos anteriores como actuales.
6. Estado de conectividad interna de los microservicios y diseño responsivo.

## Evidencias a obtener

- Captura del frontend con tres secciones y los estados internos.
- Captura de validación del RUC o correo (sin identificadores personales reales).
- Captura de un error controlado del proveedor.
- Captura de datos de puntos **simulados**, indicando visiblemente su carácter sintético.
- Captura de datos simulados recuperados desde Redis local, con verificación separada de lectura y TTL.
- Salida `npm.cmd run test` con todos los tests aprobados.
- Salida `npm.cmd run build` sin errores.
- Salida `mvnw.cmd -B clean test` sin errores para comprobar compatibilidad con fases anteriores.

## Precauciones

- La apariencia del portal no demuestra respuestas oficiales de ANT/SRI ni la conexión a Redis local.
- No publicar capturas con identificaciones personales reales.
- Los cambios en los microservicios y las pruebas de resiliencia se registran en las fases 4 y 6.
- Para probar el SRI con el proveedor real, utilizar únicamente datos propios o autorizados.
