# Servicio ANT — Fase 4

Microservicio independiente Java 17 / Spring Boot, puerto local `8082`.

## Qué hace esta fase

- Expone `POST /api/v1/interno/licencias/puntos/consultas` con `{"cedula":"9999999999"}`.
- Verifica formato (10 dígitos); no implica verificación de identidad real.
- **REAL (predeterminado):** contacta el recurso JSP indicado en la actividad. Como no existe un contrato de puntos confirmado, **no extrae ni inventa puntos del HTML**. Devuelve `503` y usa último dato válido en caché si existiera.
- **SIMULADO:** genera `{"puntos":20}` etiquetado `SIMULACION_ANT` para hacer pruebas de fallos, no para afirmar que sean puntos oficiales.
- Guarda solo resultados válidos en Redis cuando `REDIS_ENABLED=true`; controla TTL y recupera el último registro válido ante fallos técnicos.
- La clave de Redis usa HMAC-SHA256, no contiene la cédula legible. Real y simulación usan espacios de claves separados.

## Pruebas sin credenciales (no prueban Redis Cloud)

Desde la raíz del repositorio:

```powershell
.\mvnw.cmd -B clean test
```

## Demostrar simulación con Redis Cloud (evidencia separada)

Crear una instancia en Redis Cloud y obtener host, puerto y credenciales mediante su panel oficial. **Nunca publicar contraseñas ni capturas que las muestren**. En la terminal que iniciará `servicio-ant`, definir las variables del entorno, introduciendo los valores de su propia cuenta:

```powershell
$env:REDIS_ENABLED = 'true'
$env:REDIS_HOST = 'HOST_ASIGNADO'
$env:REDIS_PORT = 'PUERTO_ASIGNADO'
$env:REDIS_USERNAME = 'default'
$env:REDIS_PASSWORD = 'CONTRASENA_PRIVADA'
$env:REDIS_SSL_ENABLED = 'true'
$env:REDIS_CACHE_HMAC_SECRET = 'GENERAR_UN_SECRETO_ALEATORIO_LARGO'
$env:REDIS_TTL_SECONDS = '86400'
$env:ANT_MODO = 'SIMULADO'
$env:ANT_SIMULACION_FALLA = 'false'
.\mvnw.cmd -f servicio-ant\pom.xml spring-boot:run
```

Importante: sustituir el secreto de ejemplo por una clave aleatoria de al menos 24 caracteres; mantenerlo fijo entre las dos ejecuciones, pues si cambia, cambia la clave de caché. Evitar almacenar estas variables en `.env` versionado.

Con el orquestador encendido en otra terminal:

```powershell
.\probar-fase4.cmd -ModoSimulado
```

Debe devolver `fuente=SIMULACION_ANT`, `desdeCache=false`, y guardar el dato. Verificar la escritura en el panel de Redis Cloud (sin revelar credenciales ni datos personales); realizar además una consulta explícita de la clave/TTL mediante las herramientas de Redis si están disponibles.

Detener **solo** servicio-ant y reiniciarlo en la misma terminal con:

```powershell
$env:ANT_SIMULACION_FALLA = 'true'
.\mvnw.cmd -f servicio-ant\pom.xml spring-boot:run
```

En otra terminal:

```powershell
.\probar-fase4.cmd -ModoSimulado -EsperarCache
```

Debe devolver `fuente=SIMULACION_ANT_CACHE`, `desdeCache=true`, y conservar `consultadoEn` original. Si no recupera, revisar conexión TLS/puerto/credenciales, el TTL y el secreto HMAC (misma clave en ambas ejecuciones).

## Limitaciones que hay que declarar

La dirección del enunciado ANT corresponde a una página JSP de citaciones. No se ha comprobado ningún contrato oficial para obtener puntos. Por tanto, **la integración de puntos ANT reales permanece pendiente de verificar**; la simulación y el fallback demuestran la resiliencia, no una consulta oficial. No se debe activar `SIMULADO` y presentarlo como dato real.

El TTL de 86400 segundos (24h) es una elección experimental que deberá justificarse; después de expirar, no se devuelve el registro. Redis no es un historial permanente de puntos.
