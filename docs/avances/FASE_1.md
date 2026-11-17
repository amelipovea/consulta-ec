# Fase 1: entorno y arranque

## Evidencia que se debe guardar

1. Captura del terminal con `java -version`, `node -v`, `npm.cmd -v` y `git --version`.
2. Captura de las tres aplicaciones Spring Boot respondiendo `/actuator/health`.
3. Captura de React mostrando ambos servicios locales como disponibles.
4. Salida de `probar-fase1.cmd` y de `git log --oneline -3` tras el commit.

No publicar ninguna captura con identificaciones personales, credenciales ni datos reales.

## Comprobaciones

- Los tres backends arrancan con Java 17.
- El frontend Vite arranca en 127.0.0.1:5173.
- El frontend consulta al orquestador (no a SRI/ANT reales).
- El orquestador consulta los endpoints de estado de los dos servicios internos.
- Si se detiene uno de los servicios, el orquestador informa `NO_DISPONIBLE` sin bloquear el frontend.

## Commits planificados (en español)

- `inicio: preparar estructura y arranque de ConsultaEC`
- `funcionalidad: definir contrato REST de consultas`
- `funcionalidad: integrar contribuyentes del SRI`
- `funcionalidad: integrar consulta vehicular`
- `funcionalidad: implementar resiliencia de ANT`
- `funcionalidad: conectar Redis Cloud`
- `interfaz: desarrollar consultas y resultados en React`
- `pruebas: verificar funcionamiento y fallos`
- `documentación: completar arquitectura C4 y evidencias`
