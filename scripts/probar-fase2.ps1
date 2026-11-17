$ErrorActionPreference = "Stop"
$base = "http://127.0.0.1:8080"
$fallos = 0

function Comprobar-Post {
    param([string]$Nombre, [string]$Ruta, [string]$Cuerpo, [int[]]$Esperados, [string]$Codigo)
    $http = 0
    $texto = ""
    try {
        $respuesta = Invoke-WebRequest -UseBasicParsing -Uri "$base$Ruta" -Method Post `
             -ContentType "application/json" -Body ([Text.Encoding]::UTF8.GetBytes($Cuerpo)) -TimeoutSec 10
        $http = [int]$respuesta.StatusCode
        $texto = $respuesta.Content
    } catch {
        if ($null -ne $_.Exception.Response) {
            $http = [int]$_.Exception.Response.StatusCode
            $flujo = $_.Exception.Response.GetResponseStream()
            if ($null -ne $flujo) {
                $lector = New-Object System.IO.StreamReader($flujo)
                $texto = $lector.ReadToEnd()
                $lector.Close()
            }
        } else {
            Write-Host "[ERROR] $Nombre : no se pudo conectar al orquestador" -ForegroundColor Red
            $script:fallos++
            return
        }
    }
    $codigoReal = ""
    try { $objeto = $texto | ConvertFrom-Json; $codigoReal = $objeto.error.codigo } catch { }
    $ok = (($Esperados -contains $http) -and (($Codigo -eq "") -or ($codigoReal -eq $Codigo)))
    if ($ok) {
        Write-Host "[OK] $Nombre : HTTP $http ($codigoReal)" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] $Nombre : HTTP $http ($codigoReal) | esperado: $($Esperados -join ',') $Codigo" -ForegroundColor Red
        $script:fallos++
    }
    Write-Host $texto
    Write-Host ""
}

Write-Host "ConsultaEC - pruebas de fase 2 (identificaciones sinteticas)" -ForegroundColor Cyan
Write-Host "Orquestador: $base"

try {
    $estado = Invoke-RestMethod -Uri "$base/actuator/health" -TimeoutSec 5
    if ($estado.status -ne "UP") { throw "Health no esta UP" }
    Write-Host "[OK] Health del orquestador" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Inicia el orquestador primero en el puerto 8080" -ForegroundColor Red
    exit 1
}

Comprobar-Post "RUC invalido" "/api/v1/contribuyentes/consultas" `
    '{"correo":"prueba@ejemplo.com","ruc":"123"}' @(400) "VALIDACION_ERROR"
Comprobar-Post "Correo invalido" "/api/v1/contribuyentes/consultas" `
    '{"correo":"incorrecto","ruc":"1712345678001"}' @(400) "VALIDACION_ERROR"
Comprobar-Post "Placa invalida" "/api/v1/vehiculos/consultas" `
    '{"correo":"prueba@ejemplo.com","ruc":"1712345678001","placa":"??"}' @(400) "VALIDACION_ERROR"
Comprobar-Post "Cedula invalida" "/api/v1/licencias/puntos/consultas" `
    '{"correo":"prueba@ejemplo.com","cedula":"123"}' @(400) "VALIDACION_ERROR"
Comprobar-Post "Consulta valida SRI sin fase 3" "/api/v1/contribuyentes/consultas" `
    '{"correo":"prueba@ejemplo.com","ruc":"1712345678001"}' @(501) "CONTRATO_INTERNO_PENDIENTE"
Comprobar-Post "Consulta valida ANT sin fase 4" "/api/v1/licencias/puntos/consultas" `
    '{"correo":"prueba@ejemplo.com","cedula":"1712345678"}' @(501) "CONTRATO_INTERNO_PENDIENTE"

if ($fallos -eq 0) {
    Write-Host "FASE 2 APROBADA - API preparada, integraciones externas pendientes" -ForegroundColor Green
    exit 0
}
Write-Host "FASE 2 CON $fallos ERROR(ES)" -ForegroundColor Red
exit 1
