# Evidencias de entrega pendientes

Recolectar capturas **reales**, con fecha y contexto visible, sin secretos ni identificadores personales reales. Usar los datos sintéticos de prueba. No crear imágenes que aparenten una respuesta institucional oficial.

1. Cuatro aplicaciones en sus terminales, puertos 5173, 8080, 8081 y 8082, sin variables secretas visibles.
2. `docker exec consultaec-redis redis-cli ping` con `PONG`, y prueba de lectura/TTL de una clave sintética sin mostrar el secreto HMAC.
3. Interfaz con las tres consultas y el estado de procesos locales.
4. Validación visible de correo, RUC, placa o cédula con formato erróneo.
5. Resultado ANT nuevo: `SIMULACION_ANT`, `desdeCache=false`, fecha y etiqueta de dato simulado.
6. Resultado ANT recuperado: `SIMULACION_ANT_CACHE`, `desdeCache=true`, mismos puntos y fecha original, con aviso de información anterior.
7. Fallo ANT sin caché: HTTP 503 y JSON de error, sin puntos falsos.
8. Salidas de Maven, pruebas de React y compilación del frontend, junto al resultado del test aislado de TTL.
9. Comprobación actual del portal ANT: comando, fecha, URL sintética y estado HTTP realmente recibido. Una captura histórica del 502 solo si existe evidencia auténtica previa; no recrearla.
10. Diagrama IcePanel actualizado por quien tenga acceso, si se realiza; contrastar con [las instrucciones](../arquitectura/ACTUALIZACION_ICEPANEL.md).
11. Fichas anónimas y resumen del [estudio de usabilidad](../pruebas/USABILIDAD.md) después de sesiones reales.

Antes de entregar, revisar cada imagen para ocultar rutas privadas, nombres de cuentas, claves, correos reales, RUC, cédulas y placas reales. Anotar en [RESULTADOS_FINALES.md](../pruebas/RESULTADOS_FINALES.md) qué evidencia se obtuvo y cuál sigue pendiente.
