# Fase 2 – API de Acceso y Orquestación

## Objetivos

1. Añadir tres endpoints públicos JSON para contribuyentes, vehículos y puntos.
2. Validar correo, RUC, placa y cédula.
3. Devolver errores HTTP estructurados y sin datos sensibles.
4. Comunicar únicamente con los microservicios internos (nunca directamente con SRI/ANT).
5. Conservar los checks de conectividad de fase 1.
6. Preparar un contrato explícito para implementar las fases 3 y 4.

## Qué se puede demostrar ya

- Campos incorrectos → `400 VALIDACION_ERROR`.
- JSON incorrecto → `400 CUERPO_JSON_INVALIDO`.
- Content-Type no JSON → `415 TIPO_CONTENIDO_NO_SOPORTADO`.
- Petición válida con microservicio de fase 1 encendido → `501 CONTRATO_INTERNO_PENDIENTE`.
- Microservicio desconectado → `503 DEPENDENCIA_NO_DISPONIBLE`.
- Health y estado local de los tres procesos siguen funcionando.

**Una solicitud válida NO devuelve datos del SRI/ANT en esta fase.** Esta decisión evita
ocultar con simulaciones un proveedor todavía no integrado.

## Cómo probar

Desde `C:\AME\JAVA\consulta-ec`:

```powershell
.\mvnw.cmd -B clean test
```

Con SRI, ANT y Orquestador encendidos (reiniciar Orquestador después de aplicar los archivos):

```powershell
.\probar-fase2.cmd
```

Para guardar evidencias, captura la terminal con el código de respuesta y JSON; usa
solo identificaciones sintéticas. Prueba también la interfaz React de fase 1: debe
seguir mostrando el estado de los dos servicios.

## Precauciones

- Las rutas `/api/v1/interno/...` serán implementadas por los microservicios en siguientes fases.
- Redis Cloud no está configurado en esta fase.
- No registrar credenciales ni datos reales en commits.
- Revisar el historial propuesto en `docs/commits/FASE_2.md`.
