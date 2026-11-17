#requires -Version 5.1
param(
    [switch]$ModoSimulado,
    [switch]$EsperarCache,
    [string]$Orquestador = 'http://127.0.0.1:8080',
    [string]$Ant = 'http://127.0.0.1:8082'
)
$ErrorActionPreference = 'Stop'
$fallos = 0
function ProbarSalud([string]$nombre, [string]$base) {
    try {
        $salud = Invoke-RestMethod -Method Get -Uri "$base/actuator/health" -TimeoutSec 5
        if ($salud.status -eq 'UP') {
            Write-Host ("[OK] {0}: UP" -f $nombre) -ForegroundColor Green
        } else {
            Write-Host ("[ERROR] {0}: estado {1}" -f $nombre, $salud.status) -ForegroundColor Red
            $script:fallos++
        }
    } catch {
        Write-Host ("[ERROR] {0}: no responde salud - {1}" -f $nombre, $_.Exception.Message) -ForegroundColor Red
        $script:fallos++
    }
}
function ProbarError400([string]$nombre, [string]$ruta, [string]$cuerpo) {
    try {
        Invoke-RestMethod -Method Post -Uri $ruta -ContentType 'application/json' -Body $cuerpo -TimeoutSec 7 | Out-Null
        Write-Host ("[ERROR] {0}: no rechazó datos inválidos" -f $nombre) -ForegroundColor Red
        $script:fallos++
    } catch {
        $response = $_.Exception.Response
        if ($null -ne $response -and [int]$response.StatusCode -eq 400) {
            Write-Host ("[OK] {0}: HTTP {1}" -f $nombre, [int]$response.StatusCode) -ForegroundColor Green
        } else {
            Write-Host ("[ERROR] {0}: respuesta distinta de 400" -f $nombre) -ForegroundColor Red
            $script:fallos++
        }
    }
}
Write-Host 'ConsultaEC fase 4: prueba sin datos personales reales' -ForegroundColor Cyan
ProbarSalud 'Orquestador' $Orquestador
ProbarSalud 'Servicio ANT' $Ant
ProbarError400 'API pública - cédula inválida' "$Orquestador/api/v1/licencias/puntos/consultas" '{"correo":"prueba@example.com","cedula":"1"}'
ProbarError400 'Servicio ANT - cédula inválida' "$Ant/api/v1/interno/licencias/puntos/consultas" '{"cedula":"1"}'

if ($ModoSimulado) {
    # Solo se debe activar cuando ANT_MODO=SIMULADO; jamás usar RUC/cédulas reales.
    $peticion = '{"correo":"prueba@example.com","cedula":"9999999999"}'
    try {
        $http = Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$Orquestador/api/v1/licencias/puntos/consultas" -ContentType 'application/json' -Body $peticion -TimeoutSec 8
        $resultado = $http.Content | ConvertFrom-Json
        $desdeCache = [bool]$resultado.metadatos.desdeCache
        $esperado = [bool]$EsperarCache
        $fuenteEsperada = if ($EsperarCache) { 'SIMULACION_ANT_CACHE' } else { 'SIMULACION_ANT' }
        if ($http.StatusCode -eq 200 -and $desdeCache -eq $esperado -and $resultado.fuente -ceq $fuenteEsperada -and $resultado.datos.puntos -ge 0) {
            Write-Host ("[OK] HTTP {0}: fuente={1}, desdeCache={2}, puntos={3}, consultadoEn={4}" -f $http.StatusCode, $resultado.fuente, $desdeCache, $resultado.datos.puntos, $resultado.metadatos.consultadoEn) -ForegroundColor Green
        } else {
            Write-Host ("[ERROR] Resultado inesperado: fuente={0}, desdeCache={1}" -f $resultado.fuente, $desdeCache) -ForegroundColor Red
            $fallos++
        }
    } catch {
        Write-Host ("[ERROR] Consulta sintética falló: {0}" -f $_.Exception.Message) -ForegroundColor Red
        $fallos++
    }
} else {
    Write-Host '[INFO] Sin -ModoSimulado: no se envía una consulta válida al portal real ANT.'
}
if ($fallos -gt 0) {
    Write-Host ("FASE 4: {0} ERROR(ES)" -f $fallos) -ForegroundColor Red
    exit 1
}
Write-Host '[OK] FASE 4: comprobaciones seleccionadas aprobadas' -ForegroundColor Green
if (-not $ModoSimulado) { Write-Host '[NOTA] La caché Redis local se comprueba con scripts/probar-recuperacion-redis.ps1.' }
exit 0
