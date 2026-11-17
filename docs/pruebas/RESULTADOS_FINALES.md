# Resultados finales observados — 17 de noviembre de 2026

Entorno: Windows, Java 17, Node.js/npm, Docker Desktop y Redis local `consultaec-redis` en `127.0.0.1:6379`. Horas de esta página: Ecuador (UTC−05:00). Se usaron exclusivamente identidades de prueba sintéticas. El repositorio estaba en `main`, sin remoto configurado y con cambios de frontend previos sin commit; se revisaron antes de integrarlos.

## Comandos y resultados

| Comando ejecutado | Resultado observado |
|---|---|
| `docker start consultaec-redis` y `docker exec consultaec-redis redis-cli ping` | Contenedor iniciado; `PONG`. Estaba detenido al inicio. |
| `.\mvnw.cmd -B clean test` | `BUILD SUCCESS` a las 13:35:12. Orquestador: 22 pruebas aprobadas; SRI: 13 aprobadas; ANT: 10 aprobadas y 1 omitida por condición explícita de integración. Cero fallos y errores. |
| `$env:CONSULTAEC_REDIS_INTEGRATION='true'; .\mvnw.cmd -B -pl servicio-ant test` | `BUILD SUCCESS` a las 13:20:26; 11 pruebas ANT aprobadas, ninguna omitida. La prueba aislada escribió, leyó, comprobó TTL positivo y verificó que el dato ya no se recupera tras 2 segundos. |
| En `frontend/`: `npm.cmd run test` | 14 pruebas aprobadas, 0 fallos. |
| En `frontend/`: `npm.cmd run build` | TypeScript y Vite completaron la compilación. |
| `.\probar-fase1.cmd` | Código de salida 0: salud local de tres backends, comunicación interna y React accesibles. |
| `.\probar-fase2.cmd` | Código de salida 0: cuatro solicitudes inválidas rechazadas con HTTP 400 y `VALIDACION_ERROR`. Se retiraron expectativas históricas de HTTP 501 para rutas ya implementadas. |
| `.\probar-fase3.cmd` | Código de salida 0: salud y validaciones del SRI interno y API pública. |
| `.\probar-fase4.cmd -ModoSimulado` | Código de salida 0: HTTP 400 para cédula inválida y HTTP 200 con `SIMULACION_ANT`, `desdeCache=false`, 20 puntos sintéticos. |
| `.\probar-fase4.cmd -ModoSimulado -EsperarCache` | Código de salida 0: HTTP 200 con `SIMULACION_ANT_CACHE`, `desdeCache=true` y timestamp original. |
| `.\probar-fase5.cmd` | Código de salida 0: React HTTP 200, orquestador y servicios locales accesibles. |

Las pruebas del SRI son contra un **servidor HTTP local simulado**, no contra una respuesta oficial en vivo. Verifican las tres rutas GET, parámetros, normalización, regla de persona natural, datos vacíos y HTTP 503 del proveedor. No se utilizó un RUC real ni se verificó una respuesta SRI en vivo. Los tres endpoints públicos conservaron su contrato; se observaron HTTP 400 de validación en las rutas de contribuyentes, vehículos y puntos, y el flujo completo de puntos por el orquestador. No se hizo una prueba de consulta SRI en vivo ni una sesión automatizada de interacción visual del navegador.

## Redis y resiliencia ANT

El secreto HMAC se cargó desde el archivo DPAPI existente sin mostrarlo ni versionarlo. Se conservó la misma instancia Redis y el mismo secreto al reiniciar **solo ANT**. El servicio ANT respondió salud `UP` con Redis habilitado.

| Escenario real ejecutado | HTTP / fuente | Redis y resultado |
|---|---|---|
| Proveedor simulado operativo, `9999999999`, `prueba@example.com` | 200 / `SIMULACION_ANT`, `desdeCache=false`, 20 puntos | Escritura y lectura directas verificadas; TTL inicial 86400 s; `consultadoEn=2026-11-17T18:28:00.489683600Z`. |
| Reinicio solo ANT con `ANT_SIMULACION_FALLA=true` | 200 / `SIMULACION_ANT_CACHE`, `desdeCache=true`, 20 puntos | Se conservaron los puntos y la fecha original anterior; TTL observado 86179 s. |
| Fallo con otra identidad sintética sin caché | 503 / `DEPENDENCIA_NO_DISPONIBLE` en el orquestador | Error JSON estructurado, sin campo de puntos inventados. |
| Reinicio solo ANT con `ANT_SIMULACION_PUNTOS=-1` | 200 / `SIMULACION_ANT_CACHE`, `desdeCache=true` | El dato inválido no sobrescribió los 20 puntos ni `consultadoEn`; TTL observado 86133 s. |
| Caducidad aislada | Prueba Java con TTL de 2 s | Después del vencimiento, `recuperar` devolvió vacío. No se alteró el TTL de 86400 s de la aplicación. |
| Restauración de ANT | 200 / `SIMULACION_ANT`, `desdeCache=false` | Nueva escritura y lectura correctas; TTL inicial 86400 s. |

Los escenarios HTTP se ejecutaron con `scripts/probar-recuperacion-redis.ps1 -Escenario Actual`, `Fallo` e `Invalido`, respectivamente. El script informa estado HTTP, fuente, indicador de caché, puntos, fecha, TTL y aprobado/fallido. El dato sintético original se reemplazó solo al restaurar el modo operativo.

## Comprobación actual del portal ANT

El docente autorizó la simulación cuando el portal de citaciones presentó HTTP 502. **La prueba actual no reprodujo ese estado:** a las **13:16:08 UTC−05:00 del 17 de noviembre de 2026** respondió **HTTP 401**. URL probada con identidad sintética:

`https://consultaweb.ant.gob.ec/PortalWEB/paginas/clientes/clp_grid_citaciones.jsp?ps_tipo_identificacion=CED&ps_identificacion=9999999999&ps_placa=`

Procedimiento reproducible en PowerShell:

```powershell
$url = 'https://consultaweb.ant.gob.ec/PortalWEB/paginas/clientes/clp_grid_citaciones.jsp?ps_tipo_identificacion=CED&ps_identificacion=9999999999&ps_placa='
$fecha = Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz'
try {
    $r = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 12
    "Fecha: $fecha; HTTP: $([int]$r.StatusCode)"
} catch {
    if ($_.Exception.Response) { "Fecha: $fecha; HTTP: $([int]$_.Exception.Response.StatusCode)" }
    else { "Fecha: $fecha; sin respuesta HTTP: $($_.Exception.Message)" }
}
```

El recurso es una página JSP de citaciones, sin contrato verificable de puntos. No se fabricó una captura del 502 anterior ni se interpretó el HTTP 401 como puntos oficiales.

## Incidencias y límites

- La primera ejecución de Maven se interrumpió por un problema del canal del JVM de pruebas en el entorno restringido. La repetición completa fuera de ese aislamiento terminó con `BUILD SUCCESS`.
- La primera versión del script de Redis interpretó incorrectamente una respuesta de salud como arreglo de bytes y exigía `Int32` cuando PowerShell entregó `Int64`. Un intento posterior no pudo leer el cuerpo de error HTTP 503 en PowerShell 7. Se corrigieron ambos defectos y se repitieron los tres escenarios hasta obtener código de salida 0.
- Los cuatro puertos ya estaban ocupados por aplicaciones de ConsultaEC iniciadas antes de la prueba; no se detuvieron SRI, orquestador ni React. Solo ANT se reinició para los escenarios y quedó restaurado al modo operativo.
- Se revisó el código para comprobar que React solo llama a `/api/v1` del orquestador, que no usa `localStorage`, `sessionStorage` ni cookies propias, y que contiene reglas CSS para 1060, 760 y 430 px. La apariencia y el flujo en tamaños reales de pantalla requieren las capturas manuales de entrega.
- Se recibió un comentario cualitativo real de una persona que probó la interfaz: el correo obligatorio no era suficientemente obvio. El retest posterior, las duraciones y las tasas de finalización siguen pendientes; no se atribuyeron otros hallazgos a esa persona.
- El visor IcePanel en línea no fue editado; las modificaciones requeridas de C1/C2/C3 están en `docs/arquitectura/ACTUALIZACION_ICEPANEL.md`.
- No hay remoto Git configurado; los commits quedaron locales y no se hizo `push`.

## Evidencias para entrega

La lista de capturas que deben obtener los estudiantes está en [docs/evidencias/README.md](../evidencias/README.md). No se añadieron capturas de participantes ni del portal histórico. Las salidas de prueba citadas arriba fueron observadas en terminal; quien entregue el proyecto debe capturarlas de forma auténtica y sin secretos visibles.

## Verificación posterior a la mejora de experiencia — 17 de noviembre de 2026

La sección de correo compartido se destacó antes de los formularios. Se incorporaron validación prioritaria, foco al campo erróneo, anuncio ARIA, autocompletado de correo, defensa en el cliente HTTP y bloqueo inmediato de envíos repetidos. La interfaz también identifica la simulación ANT antes de enviar. Estos cambios no alteraron las rutas públicas ni los servicios Java.

| Comando o revisión | Resultado observado |
|---|---|
| En `frontend/`: `npm.cmd run test` | **22 pruebas aprobadas**, 0 fallos. Incluyen correo ausente, inválido y válido, reutilización en tres rutas, prevención de peticiones inválidas, recuperación, bloqueo durante carga, marcado accesible y avisos ANT. |
| En `frontend/`: `npm.cmd run build` | TypeScript y Vite terminaron sin errores. |
| `.\mvnw.cmd -B clean test` | `BUILD SUCCESS` a las 14:31:35; 22 pruebas del orquestador, 13 del SRI y 10 de ANT aprobadas, con la prueba aislada Redis omitida por condición explícita. No se repitió la integración Redis de TTL porque no cambió ese código; su ejecución anterior aprobada se conserva arriba. |
| `.\probar-fase2.cmd` y `.\probar-fase5.cmd` | Ambos terminaron con código 0. La fase 2 observó cuatro HTTP 400 estructurados; la fase 5 observó React, orquestador, SRI y ANT accesibles. |
| Renderizado local con Edge sin interfaz | Se revisó una captura temporal de escritorio y otra con ancho CSS cercano a móvil; la sección del correo se veía completa y legible. Las capturas quedaron en `frontend/dist/`, ignorado por Git, y no sustituyen las evidencias que deben obtener los estudiantes. No se ejecutó una interacción automatizada de formulario en navegador. |

El comentario de usabilidad recibido es cualitativo. **No se ha medido todavía** si la nueva interfaz reduce errores o tiempos; el protocolo de retest está en [USABILIDAD.md](USABILIDAD.md).

## Commits locales de implementación y pruebas

| Hash | Mensaje |
|---|---|
| `ef55975` | `corrección: adaptar Redis para ejecución local` |
| `4e7d988` | `pruebas: verificar recuperación ANT y caducidad en Redis local` |
| `1533452` | `funcionalidad: integrar formularios y resultados en React` |
| `36964cf` | `pruebas: validar contratos SRI, orquestador y frontend` |
| `b5b3489` | `mejora: destacar el correo obligatorio y guiar las consultas` |
| `f191a50` | `pruebas: verificar correo, envíos y avisos de la interfaz` |

El commit de documentación que incorpora este archivo se identifica con `git log -1 --oneline` después de terminar el cierre.
