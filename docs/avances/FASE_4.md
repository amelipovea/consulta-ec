# Fase 4 — ANT y Redis local

## Componentes incluidos

- `PuntosAntController`: endpoint interno y validación de cédula.
- `ConsultarPuntosAnt`: guardar última respuesta válida y fallback solo ante fallo técnico.
- `ProveedorAntPortal`: contacto con el recurso JSP del enunciado; **sin contrato de puntos verificable**.
- `ProveedorAntSimulado`: pruebas **explícitamente sintéticas** mediante `ANT_MODO=SIMULADO`.
- `RedisUltimoDato`: caché en Redis local en Docker, habilitada con `REDIS_ENABLED=true`; clave HMAC y TTL. TLS sigue siendo configurable para otro entorno.
- `CacheDeshabilitada`: permite arrancar sin Redis en pruebas unitarias; no demuestra recuperación real.

## Estado y evidencias

1. La configuración local usa `127.0.0.1:6379`, sin TLS, con tiempos de conexión y lectura de 3 segundos. El ejemplo de entorno no incluye secretos.
2. El secreto HMAC de al menos 24 caracteres se recupera del archivo DPAPI del usuario y permanece igual entre reinicios ANT.
3. Las pruebas unitarias cubren respuesta nueva, fallback, fallo sin caché y respuesta inválida. La prueba aislada con Redis local usa TTL de 2 segundos.
4. El script `scripts/probar-recuperacion-redis.ps1` comprueba estados HTTP, fuente, indicador de caché, timestamp, lectura y TTL directamente en Redis.
5. Los resultados realmente ejecutados y las tareas manuales se registran en `docs/pruebas/RESULTADOS_FINALES.md`.
6. El recurso real es una página JSP de citaciones sin contrato confirmado de puntos. La simulación autorizada por el docente no representa datos oficiales.

No publicar credenciales, cédulas reales ni claves Redis asociadas a personas.
