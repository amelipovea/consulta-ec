# Servicio ANT y Redis local

Microservicio Java 17 / Spring Boot en `127.0.0.1:8082`. Expone `POST /api/v1/interno/licencias/puntos/consultas`; recibe `{"cedula":"9999999999"}` en las pruebas sintéticas. Solo valida diez dígitos, no una identidad real.

## Modos y recuperación

`ANT_MODO=SIMULADO` genera `{"puntos":20}` por defecto, con `fuente=SIMULACION_ANT`. Esta simulación fue autorizada por el docente cuando el portal de citaciones de la ANT devolvió HTTP 502. La comprobación actual se registra en [resultados finales](../docs/pruebas/RESULTADOS_FINALES.md): no se atribuye al portal un 502 que no se haya observado ahora. `ANT_SIMULACION_FALLA=true` provoca un error técnico controlado.

El servicio valida una respuesta antes de guardarla. Si el proveedor falla, lee el último dato válido de Redis y devuelve `fuente=SIMULACION_ANT_CACHE`, `desdeCache=true`, los mismos puntos y `consultadoEn` original. Si no hay caché, devuelve 503 estructurado. Una respuesta inválida no sobreescribe el dato anterior. El TTL predeterminado es 86400 segundos; después de vencer, el dato deja de recuperarse.

`ANT_MODO=REAL` solicita la página JSP de citaciones indicada por el proyecto. Aunque responda, no hay un contrato verificado de puntos; por ello no extrae puntos del HTML ni presenta un número oficial.

## Redis local en Docker

Usar el contenedor existente `consultaec-redis` y comprobar `docker exec consultaec-redis redis-cli ping`. El archivo [`.env.example`](.env.example) muestra los valores locales: `REDIS_ENABLED=true`, host `127.0.0.1`, puerto `6379`, usuario `default`, contraseña vacía, `REDIS_SSL_ENABLED=false`, TTL `86400`, modo `SIMULADO` y fallo `false`. Java lee variables de entorno; no carga `.env.example` automáticamente. Los tiempos de conexión y lectura de Redis son configurables y tienen un valor local de 3 segundos. TLS puede activarse para otro entorno con `REDIS_SSL_ENABLED=true`.

Cuando Redis está habilitado, `REDIS_CACHE_HMAC_SECRET` es obligatorio y debe tener al menos 24 caracteres. Se guarda cifrado con DPAPI en `%APPDATA%\ConsultaEC\ant-hmac.dpapi` y se carga en la terminal ANT según los [cuatro comandos de arranque manual](../README.md#arranque-manual-cuatro-terminales). Hay que reutilizar exactamente el mismo secreto entre reinicios. Las claves Redis contienen HMAC-SHA256, no la cédula en texto claro. Los espacios de claves de simulación y modo real son distintos.

## Verificación

Con las cuatro aplicaciones iniciadas:

```powershell
.\probar-fase4.cmd -ModoSimulado
.\scripts\probar-recuperacion-redis.ps1 -Escenario Actual
```

Reinicie **solo ANT** con `ANT_SIMULACION_FALLA=true` y ejecute `-Escenario Fallo`. El script informa estados HTTP, fuente, caché, fecha, puntos y TTL real, y comprueba además un fallo sin caché. Para verificar una respuesta inválida sin perder el dato válido, reinicie ANT con `ANT_SIMULACION_FALLA=false` y `ANT_SIMULACION_PUNTOS=-1`, luego use `-Escenario Invalido`.

La prueba de caducidad usa exclusivamente un registro sintético separado y TTL de 2 segundos:

```powershell
$env:CONSULTAEC_REDIS_INTEGRATION = 'true'
.\mvnw.cmd -B -pl servicio-ant test
Remove-Item Env:CONSULTAEC_REDIS_INTEGRATION
```

Los resultados reales de ejecución se anotan en [RESULTADOS_FINALES.md](../docs/pruebas/RESULTADOS_FINALES.md). No publicar el secreto, respuestas de personas reales ni capturas con identificadores personales.
