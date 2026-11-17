#requires -Version 5.1
param(
    [ValidateSet('Actual', 'Fallo', 'Invalido')]
    [string]$Escenario = 'Actual',
    [string]$Orquestador = 'http://127.0.0.1:8080',
    [string]$Ant = 'http://127.0.0.1:8082'
)
$ErrorActionPreference = 'Stop'
$fallos = 0

function Informar([bool]$correcto, [string]$mensaje) {
    if ($correcto) { Write-Host "[OK] $mensaje" -ForegroundColor Green }
    else { Write-Host "[ERROR] $mensaje" -ForegroundColor Red; $script:fallos++ }
}

function Http([string]$url, [string]$cuerpo = '') {
    try {
        $opciones = @{ Uri = $url; UseBasicParsing = $true; TimeoutSec = 12 }
        if ($cuerpo) {
            $opciones.Method = 'Post'
            $opciones.ContentType = 'application/json; charset=utf-8'
            $opciones.Body = [Text.Encoding]::UTF8.GetBytes($cuerpo)
        }
        $respuesta = Invoke-WebRequest @opciones
        $texto = if ($respuesta.Content -is [byte[]]) {
            [Text.Encoding]::UTF8.GetString($respuesta.Content)
        } else { [string]$respuesta.Content }
        return @{ estado = [int]$respuesta.StatusCode; cuerpo = ($texto | ConvertFrom-Json) }
    } catch {
        if ($null -eq $_.Exception.Response) { throw }
        $respuesta = $_.Exception.Response
        $texto = ''
        if ($respuesta -is [System.Net.Http.HttpResponseMessage]) {
            $texto = $_.ErrorDetails.Message
        } else {
            $flujo = $respuesta.GetResponseStream()
            if ($null -ne $flujo) {
                $lector = New-Object System.IO.StreamReader($flujo)
                try { $texto = $lector.ReadToEnd() } finally { $lector.Close() }
            }
        }
        $json = if ($texto) { $texto | ConvertFrom-Json } else { $null }
        return @{ estado = [int]$respuesta.StatusCode; cuerpo = $json }
    }
}

function Redis([string]$operacion, [string]$clave) {
    $salida = & docker exec consultaec-redis redis-cli --raw $operacion $clave 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Redis no respondió a $operacion (código $LASTEXITCODE)" }
    return ($salida | Out-String).Trim()
}

function Clave([string]$cedula, [string]$secreto) {
    $mac = [System.Security.Cryptography.HMACSHA256]::new([Text.Encoding]::UTF8.GetBytes($secreto))
    try {
        $hash = [Convert]::ToBase64String($mac.ComputeHash([Text.Encoding]::UTF8.GetBytes($cedula)))
        return 'consultaec:ant:sim:' + $hash.TrimEnd('=').Replace('+', '-').Replace('/', '_')
    } finally { $mac.Dispose() }
}

try {
    $secreto = $env:REDIS_CACHE_HMAC_SECRET
    if ([string]::IsNullOrWhiteSpace($secreto)) {
        $ruta = Join-Path $env:APPDATA 'ConsultaEC\ant-hmac.dpapi'
        if (-not (Test-Path -LiteralPath $ruta)) { throw 'Falta el secreto DPAPI de ANT' }
        $seguro = (Get-Content -LiteralPath $ruta -Raw).Trim() | ConvertTo-SecureString
        $secreto = (New-Object System.Net.NetworkCredential('', $seguro)).Password
    }
    if ($secreto.Length -lt 24) { throw 'El secreto HMAC debe tener al menos 24 caracteres' }
    $pong = & docker exec consultaec-redis redis-cli ping 2>&1
    if ($LASTEXITCODE -ne 0 -or ($pong | Out-String).Trim() -ne 'PONG') { throw 'Redis local no devolvió PONG' }
    Informar $true 'Redis local: PONG'

    foreach ($servicio in @(@('Orquestador', $Orquestador), @('ANT', $Ant))) {
        $salud = Http "$($servicio[1])/actuator/health"
        Informar ($salud.estado -eq 200 -and $salud.cuerpo.status -eq 'UP') "$($servicio[0]) salud: HTTP $($salud.estado), $($salud.cuerpo.status)"
    }

    $cedula = '9999999999'
    $clave = Clave $cedula $secreto
    $anterior = $null
    if ($Escenario -ne 'Actual') {
        $textoAnterior = Redis 'GET' $clave
        if (-not $textoAnterior) { throw 'No hay dato sintético previo en Redis; ejecutar primero el escenario Actual' }
        $anterior = $textoAnterior | ConvertFrom-Json
    }
    $respuesta = Http "$Orquestador/api/v1/licencias/puntos/consultas" '{"correo":"prueba@example.com","cedula":"9999999999"}'
    $cuerpo = $respuesta.cuerpo
    $fuenteEsperada = if ($Escenario -eq 'Actual') { 'SIMULACION_ANT' } else { 'SIMULACION_ANT_CACHE' }
    $cacheEsperada = $Escenario -ne 'Actual'
    $fechaValida = $false
    try { $fechaValida = $null -ne [datetimeoffset]::Parse($cuerpo.metadatos.consultadoEn) } catch { }
    Write-Host ("Consulta: HTTP {0}; fuente={1}; desdeCache={2}; puntos={3}; consultadoEn={4}" -f `
        $respuesta.estado, $cuerpo.fuente, $cuerpo.metadatos.desdeCache, $cuerpo.datos.puntos, $cuerpo.metadatos.consultadoEn)
    Informar ($respuesta.estado -eq 200 -and $cuerpo.fuente -ceq $fuenteEsperada -and `
        $cuerpo.metadatos.desdeCache -ceq $cacheEsperada -and `
        ($cuerpo.datos.puntos -is [int] -or $cuerpo.datos.puntos -is [long]) -and `
        $cuerpo.datos.puntos -ge 0 -and $fechaValida) "Contrato del escenario $Escenario"

    $textoGuardado = Redis 'GET' $clave
    $ttl = [long](Redis 'TTL' $clave)
    $guardado = if ($textoGuardado) { $textoGuardado | ConvertFrom-Json } else { $null }
    Informar ($null -ne $guardado -and $ttl -gt 0 -and `
        $guardado.datos.puntos -eq $cuerpo.datos.puntos -and `
        $guardado.consultadoEn -eq $cuerpo.metadatos.consultadoEn) "Redis: lectura y TTL positivo ($ttl s)"
    if ($anterior) {
        Informar ($anterior.datos.puntos -eq $cuerpo.datos.puntos -and `
            $anterior.consultadoEn -eq $cuerpo.metadatos.consultadoEn -and `
            $anterior.consultadoEn -eq $guardado.consultadoEn) 'Se conservaron puntos y fecha de la consulta original'
    }

    if ($Escenario -eq 'Fallo') {
        $sinCache = $null
        for ($intento = 0; $intento -lt 20; $intento++) {
            $candidata = '99' + (Get-Random -Minimum 10000000 -Maximum 99999999)
            if ((Redis 'EXISTS' (Clave $candidata $secreto)) -eq '0') { $sinCache = $candidata; break }
        }
        if (-not $sinCache) { throw 'No se encontró una identidad sintética sin caché' }
        $fallo = Http "$Orquestador/api/v1/licencias/puntos/consultas" `
            ('{"correo":"prueba@example.com","cedula":"' + $sinCache + '"}')
        Write-Host ("Sin caché: HTTP {0}; código={1}" -f $fallo.estado, $fallo.cuerpo.error.codigo)
        Informar ($fallo.estado -eq 503 -and $fallo.cuerpo.error.codigo -eq 'DEPENDENCIA_NO_DISPONIBLE' -and `
            $null -eq $fallo.cuerpo.datos) 'Fallo sin caché: indisponibilidad estructurada, sin puntos inventados'
    }
} catch {
    Informar $false $_.Exception.Message
} finally {
    $secreto = $null
}
if ($fallos -gt 0) { Write-Host "[ERROR] $fallos comprobación(es) fallidas" -ForegroundColor Red; exit 1 }
Write-Host "[OK] Escenario $Escenario aprobado" -ForegroundColor Green
exit 0
