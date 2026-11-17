#requires -Version 5.1
param(
    [string]$Orquestador = 'http://127.0.0.1:8080',
    [string]$Sri = 'http://127.0.0.1:8081'
)

$ErrorActionPreference = 'Stop'
$fallos = 0

# Los endpoints /actuator/health devuelven JSON. Se evalua el campo status,
# NO una coincidencia textual exacta que dependa de espacios en el JSON.
function ComprobarSalud([string]$nombre, [string]$url) {
    try {
        $respuesta = Invoke-RestMethod -Method Get -Uri "$url/actuator/health" -TimeoutSec 5
        $estado = [string]$respuesta.status
        if ($estado -eq 'UP') {
            Write-Host ("[OK] {0}: estado UP" -f $nombre) -ForegroundColor Green
            return
        }
        if ([string]::IsNullOrWhiteSpace($estado)) { $estado = 'sin campo status' }
        Write-Host ("[ERROR] {0}: el health devolvio estado {1}" -f $nombre, $estado) -ForegroundColor Red
    } catch {
        Write-Host ("[ERROR] {0}: no se pudo consultar /actuator/health - {1}" -f $nombre, $_.Exception.Message) -ForegroundColor Red
    }
    $script:fallos++
}

function ComprobarValidacion([string]$nombre, [string]$url, [string]$json) {
    try {
        Invoke-RestMethod -Method Post -Uri $url -Body $json -ContentType 'application/json; charset=utf-8' -TimeoutSec 8 | Out-Null
        Write-Host ("[ERROR] {0}: acepto una solicitud invalida" -f $nombre) -ForegroundColor Red
        $script:fallos++
    } catch {
        $respuestaError = $_.Exception.Response
        if ($null -ne $respuestaError -and [int]$respuestaError.StatusCode -eq 400) {
            Write-Host ("[OK] {0}: rechazada con HTTP 400" -f $nombre) -ForegroundColor Green
        } else {
            $codigo = if ($null -ne $respuestaError) { [int]$respuestaError.StatusCode } else { 'sin respuesta HTTP' }
            Write-Host ("[ERROR] {0}: se esperaba HTTP 400 y se obtuvo {1}" -f $nombre, $codigo) -ForegroundColor Red
            $script:fallos++
        }
    }
}

Write-Host 'ConsultaEC - fase 3: salud y validaciones (SIN consultar RUC reales)' -ForegroundColor Cyan
ComprobarSalud 'Orquestador' $Orquestador
ComprobarSalud 'Servicio SRI' $Sri
ComprobarValidacion 'RUC inválido - API pública' "$Orquestador/api/v1/contribuyentes/consultas" '{"correo":"prueba@example.com","ruc":"12"}'
ComprobarValidacion 'RUC inválido - servicio interno' "$Sri/api/v1/interno/contribuyentes/consultas" '{"ruc":"12"}'
ComprobarValidacion 'Placa inválida - servicio interno' "$Sri/api/v1/interno/vehiculos/consultas" '{"ruc":"1712345678001","placa":"1"}'

if ($fallos -eq 0) {
    Write-Host '[OK] FASE 3: salud y validaciones aprobadas' -ForegroundColor Green
    Write-Host 'IMPORTANTE: esto no confirma disponibilidad del SRI externo.'
    Write-Host 'Ejecuta tambien .\mvnw.cmd -B clean test para verificar las pruebas automatizadas.'
    exit 0
}
Write-Host ("[ERROR] FASE 3: {0} error(es)" -f $fallos) -ForegroundColor Red
exit 1
