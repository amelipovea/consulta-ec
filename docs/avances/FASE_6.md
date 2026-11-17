# Fase 6 — Integración, pruebas y entrega

Se conserva la arquitectura de cuatro aplicaciones y tres rutas públicas. ANT usa Redis **local en Docker**; el proveedor simulado autorizado por el docente sigue marcado explícitamente como sintético. SRI mantiene sus tres solicitudes externas, verificadas en pruebas mediante un servidor HTTP local con datos sintéticos. No se afirma haber obtenido respuestas oficiales del SRI.

La fase incorpora una prueba aislada de escritura, lectura y caducidad de Redis, un script para contrastar respuestas HTTP con el valor y TTL de Redis, validación más estricta del contrato en React y reparación del script de fase 2 que aún esperaba rutas internas sin implementar.

Los comandos, resultados reales y límites de ejecución están en [RESULTADOS_FINALES.md](../pruebas/RESULTADOS_FINALES.md). Para la entrega faltan las capturas y el estudio con participantes reales. Las modificaciones al visor IcePanel se describen en [ACTUALIZACION_ICEPANEL.md](../arquitectura/ACTUALIZACION_ICEPANEL.md) y requieren acceso al modelo en línea.
