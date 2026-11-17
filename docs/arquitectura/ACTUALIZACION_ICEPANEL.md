# Actualización pendiente del modelo IcePanel

El [visor C4 de ConsultaEC](https://s.icepanel.io/n43YWpkfdsi2st/hsOQ/landscape/diagrams/viewer?diagram=vufBdibo13&model=cqKWptnNe3&overlay_tab=tags&x1=0&y1=0&x2=1699&y2=935) puede mostrar Redis Cloud. **No se modificó el diagrama en línea**; esta es la lista de cambios para quien tenga permiso de edición.

## C1: contexto

- Mantener al ciudadano y ConsultaEC como sistema principal.
- Mantener al SRI y al portal ANT como sistemas externos. Etiquetar la consulta de puntos ANT como simulada por autorización docente mientras no exista contrato oficial verificable.
- Quitar la referencia a Redis Cloud como servicio externo administrado. Si se muestra Redis en C1, describirlo como infraestructura local de ConsultaEC en Docker.

## C2: contenedores

- Conservar cuatro aplicaciones: React :5173, Orquestador :8080, Servicio SRI :8081 y Servicio ANT :8082.
- Representar `consultaec-redis` como contenedor Redis local en `127.0.0.1:6379`, asociado únicamente al servicio ANT.
- Conexiones: Ciudadano → React; React → Orquestador; Orquestador → SRI y ANT; SRI → SRI externo; ANT → Redis local. ANT → portal ANT aplica solo en modo `REAL` y no produce puntos oficiales actualmente.
- Eliminar React → servicios internos/Redis y cualquier flecha ANT → Redis Cloud.

## C3: componentes ANT

- `PuntosAntController` → `ConsultarPuntosAnt` → `ProveedorAntSimulado` o `ProveedorAntPortal`.
- `ConsultarPuntosAnt` → `AlmacenUltimoDato` → `RedisUltimoDato` → Redis local. `CacheDeshabilitada` solo corresponde a pruebas/arranque sin Redis.
- Anotar validación antes de guardar, fallback únicamente por fallo técnico, `fuente=SIMULACION_ANT_CACHE`, `desdeCache=true`, timestamp original, clave HMAC y TTL de 86400 segundos.
- Indicar TLS desactivado para Docker local y configurable para otro entorno. El secreto HMAC se carga desde DPAPI mediante una variable de entorno; no dibujar ni publicar su valor.

Tras actualizar, comprobar que nombres, puertos, conexiones y notas de simulación coincidan con el repositorio y guardar una captura sin datos personales.
