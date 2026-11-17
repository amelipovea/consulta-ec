# Fase 4 — ANT y Redis Cloud

## Componentes incluidos

- `PuntosAntController`: endpoint interno y validación de cédula.
- `ConsultarPuntosAnt`: guardar última respuesta válida y fallback solo ante fallo técnico.
- `ProveedorAntPortal`: contacto con el recurso JSP del enunciado; **sin contrato de puntos verificable**.
- `ProveedorAntSimulado`: pruebas **explícitamente sintéticas** mediante `ANT_MODO=SIMULADO`.
- `RedisUltimoDato`: almacén externo real habilitable con `REDIS_ENABLED=true`, TLS, HMAC y TTL.
- `CacheDeshabilitada`: sirve para iniciar y probar validaciones sin credenciales de Redis, **no equivale a Redis Cloud conectado**.

## Evidencias pendientes para completar la rúbrica

1. `mvnw.cmd -B clean test` con resultado `BUILD SUCCESS`.
2. `probar-fase4.cmd` con salud y HTTP 400.
3. **Conexión real** a Redis Cloud, con datos sintéticos y etiqueta `SIMULACION_ANT`.
4. Simulación de proveedor operativo: `desdeCache=false` y escritura con TTL.
5. Simulación de proveedor fallando: `desdeCache=true`, conservando `consultadoEn` anterior.
6. Caducidad del TTL (usar TTL corto para la prueba controlada y restablecer 86400 luego).
7. Registrar claramente que el recurso ANT real es una página JSP sin contrato de puntos confirmado.

No publicar credenciales, cédulas reales ni claves Redis asociadas a personas.
