# ConsultaEC

ConsultaEC es un proyecto académico para consultar datos de contribuyentes y vehículos mediante el SRI y demostrar la recuperación de puntos de licencia ante fallos de la ANT. La consulta de puntos usa un **proveedor simulado autorizado por el docente** tras la indisponibilidad del portal de citaciones. Sus puntos son sintéticos, no oficiales.

## Arquitectura C4 y componentes

| Aplicación | Directorio | Puerto | Responsabilidad |
|---|---|---:|---|
| Interfaz React, TypeScript y Vite | `frontend/` | 5173 | Formularios, validación de formato, resultados y avisos |
| Orquestador Spring Boot, Java 17 | `orquestador/` | 8080 | API pública y coordinación |
| Servicio SRI Spring Boot, Java 17 | `servicio-sri/` | 8081 | Integraciones HTTP con el SRI |
| Servicio ANT Spring Boot, Java 17 | `servicio-ant/` | 8082 | Proveedor ANT y último dato válido |

```text
Ciudadano → React :5173 → Orquestador :8080 ─┬→ Servicio SRI :8081 → SRI externo
                                               └→ Servicio ANT :8082 → Redis local en Docker :6379
                                                                      ↘ portal ANT, solo en modo REAL
```

React consume únicamente la API del orquestador. Redis pertenece al servicio ANT. El `pom.xml` raíz compila los tres backends, que se ejecutan por separado. Véase [arquitectura](docs/arquitectura/README.md) y el [visor C4 de IcePanel](https://s.icepanel.io/n43YWpkfdsi2st/hsOQ/landscape/diagrams/viewer?diagram=vufBdibo13&model=cqKWptnNe3&overlay_tab=tags&x1=0&y1=0&x2=1699&y2=935). El visor puede conservar referencias antiguas a Redis Cloud; [esta actualización pendiente](docs/arquitectura/ACTUALIZACION_ICEPANEL.md) indica los cambios exactos.

## Integraciones y contratos

El servicio SRI realiza tres GET configurados contra `https://srienlinea.sri.gob.ec`: existencia de RUC, detalles del contribuyente y vehículo por placa. La consulta vehicular exige antes confirmar que el RUC corresponde a persona natural. Las pruebas automatizadas usan un servidor HTTP local y datos sintéticos; no verifican respuestas del SRI en vivo.

| Operación pública en `:8080` | JSON de solicitud |
|---|---|
| `POST /api/v1/contribuyentes/consultas` | `{"correo":"prueba@example.com","ruc":"9999999999001"}` |
| `POST /api/v1/vehiculos/consultas` | `{"correo":"prueba@example.com","ruc":"9999999999001","placa":"ABC-1234"}` |
| `POST /api/v1/licencias/puntos/consultas` | `{"correo":"prueba@example.com","cedula":"9999999999"}` |

Los ejemplos son sintéticos. La respuesta correcta contiene `tipoConsulta`, `estado=OK`, `fuente`, `datos` y `metadatos` con `consultadoEn` y `desdeCache`. Los errores contienen `error.codigo`, `mensaje`, `solicitudId`, `instante` y `detalles`. Los códigos y reglas están en [contratos HTTP](docs/contratos/FASE_2_API_REST.md), [SRI](docs/contratos/FASE_3_SRI.md) y [ANT](docs/contratos/FASE_4_ANT.md).

En `ANT_MODO=SIMULADO`, una respuesta nueva tiene `fuente=SIMULACION_ANT`. Si el proveedor falla y existe un último dato válido, el servicio devuelve `SIMULACION_ANT_CACHE`, `desdeCache=true` y la **fecha original**. React avisa que puede estar desactualizado. Sin caché responde con error HTTP estructurado; nunca inventa cero puntos. Redis local guarda la respuesta con TTL de 86400 segundos (24 horas), clave HMAC y sin cédula legible. El modo `REAL` no infiere puntos de la página JSP de citaciones sin contrato confirmado.

## Requisitos y preparación

Windows con PowerShell, Java 17, Node.js/npm, Docker Desktop y el contenedor local `consultaec-redis`. Maven Wrapper y npm pueden necesitar red al instalar dependencias por primera vez. El secreto HMAC estable debe existir, cifrado con DPAPI para el mismo usuario de Windows, en `%APPDATA%\ConsultaEC\ant-hmac.dpapi`. Debe tener al menos 24 caracteres. No se versiona ni se muestra.

Desde la raíz del repositorio, compruebe Redis:

```powershell
docker start consultaec-redis
docker exec consultaec-redis redis-cli ping
```

La respuesta esperada del segundo comando es `PONG`. `servicio-ant/.env.example` enumera la configuración local; Java no carga ese archivo automáticamente. Sin variables, ANT arranca en modo `REAL` y con Redis deshabilitado para permitir las pruebas unitarias. Para esta demostración deben definirse todas las variables de la terminal 2. TLS sigue disponible con `REDIS_SSL_ENABLED=true` para otros entornos, pero el Docker local usa `false`.

## Arranque manual: cuatro terminales

**Terminal 1, SRI:**

```powershell
cd C:\AME\JAVA\consulta-ec
.\mvnw.cmd -f servicio-sri\pom.xml spring-boot:run
```

**Terminal 2, ANT:** conservar esta misma terminal y el mismo secreto al reiniciar ANT.

```powershell
cd C:\AME\JAVA\consulta-ec
$rutaHmac = Join-Path $env:APPDATA 'ConsultaEC\ant-hmac.dpapi'
$seguro = (Get-Content -LiteralPath $rutaHmac -Raw).Trim() | ConvertTo-SecureString
$env:REDIS_CACHE_HMAC_SECRET = (New-Object System.Net.NetworkCredential('', $seguro)).Password
if ($env:REDIS_CACHE_HMAC_SECRET.Length -lt 24) { throw 'Secreto HMAC insuficiente' }
$env:REDIS_ENABLED = 'true'
$env:REDIS_HOST = '127.0.0.1'
$env:REDIS_PORT = '6379'
$env:REDIS_SSL_ENABLED = 'false'
$env:REDIS_USERNAME = 'default'
$env:REDIS_PASSWORD = ''
$env:REDIS_TTL_SECONDS = '86400'
$env:ANT_MODO = 'SIMULADO'
$env:ANT_SIMULACION_FALLA = 'false'
.\mvnw.cmd -f servicio-ant\pom.xml spring-boot:run
```

**Terminal 3, orquestador:**

```powershell
cd C:\AME\JAVA\consulta-ec
.\mvnw.cmd -f orquestador\pom.xml spring-boot:run
```

**Terminal 4, React:**

```powershell
cd C:\AME\JAVA\consulta-ec\frontend
npm.cmd ci
npm.cmd run dev
```

Abra `http://127.0.0.1:5173`. Los indicadores de salud reflejan procesos locales, no la disponibilidad de los portales externos. Para probar el fallback, detenga **solo ANT** con Ctrl+C, cambie `$env:ANT_SIMULACION_FALLA = 'true'` en su terminal y repita el comando ANT. Mantenga Redis y las otras aplicaciones activas.

## Pruebas y entrega

Desde la raíz:

```powershell
.\mvnw.cmd -B clean test
.\probar-fase1.cmd
.\probar-fase2.cmd
.\probar-fase3.cmd
.\probar-fase4.cmd -ModoSimulado
.\probar-fase5.cmd
.\scripts\probar-recuperacion-redis.ps1 -Escenario Actual
```

Después del reinicio de ANT con fallo:

```powershell
.\probar-fase4.cmd -ModoSimulado -EsperarCache
.\scripts\probar-recuperacion-redis.ps1 -Escenario Fallo
```

Para probar respuesta inválida, reinicie solo ANT con `ANT_SIMULACION_FALLA=false` y `ANT_SIMULACION_PUNTOS=-1`, y ejecute `-Escenario Invalido`. Restablezca después `ANT_SIMULACION_PUNTOS=20`. La prueba Java de vencimiento usa TTL de 2 segundos exclusivamente en un registro sintético aislado: `$env:CONSULTAEC_REDIS_INTEGRATION='true'; .\mvnw.cmd -B -pl servicio-ant test`. Quite la variable al terminar.

En `frontend/`: `npm.cmd run test` y `npm.cmd run build`. Los [resultados observados](docs/pruebas/RESULTADOS_FINALES.md) distinguen pruebas locales de integraciones en vivo.

Para entregar: ejecutar las pruebas, guardar capturas solicitadas en [evidencias](docs/evidencias/README.md) sin datos personales reales, realizar el [estudio de usabilidad](docs/pruebas/USABILIDAD.md), actualizar IcePanel según el documento pendiente, revisar `git status` y compartir los commits del repositorio correcto. No se publica automáticamente.

## Limitaciones conocidas

- La simulación ANT no proporciona puntos oficiales. La verificación actual del portal se documenta con su estado HTTP real, sin afirmar que todavía devuelva 502.
- El SRI en vivo puede cambiar respuesta o estar indisponible; las pruebas con servidor local no son evidencia de una respuesta oficial.
- Redis es una caché temporal, no historial permanente. La disponibilidad del proceso ANT no garantiza la conexión al proveedor.
- El estudio de usabilidad y las capturas de participantes requieren ejecución humana y siguen pendientes.
