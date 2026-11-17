#requires -Version 5.1
$ErrorActionPreference = 'Stop'

$endpoints = @(
    @{ Nombre = 'Orquestador Spring Boot'; Url = 'http://127.0.0.1:8080/actuator/health' },
    @{ Nombre = 'Servicio SRI Spring Boot'; Url = 'http://127.0.0.1:8081/actuator/health' },
    @{ Nombre = 'Servicio ANT Spring Boot'; Url = 'http://127.0.0.1:8082/actuator/health' }
)

$fallos = 0
Write-Host 'ConsultaEC: comprobacion de fase 1 (solo localhost)' -ForegroundColor Cyan
foreach ($elemento in $endpoints) {
    try {
        $r = Invoke-RestMethod -Uri $elemento.Url -TimeoutSec 5
        if ($r.status -ne 'UP') { throw 'El health check no devolvio UP' }
        Write-Host ('[OK] ' + $elemento.Nombre) -ForegroundColor Green
    } catch {
        Write-Host ('[ERROR] ' + $elemento.Nombre + ' - Verifica que este iniciado') -ForegroundColor Red
        $fallos++
    }
}

try {
    $estado = Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/v1/sistema/estado' -TimeoutSec 10
    if ($estado.orquestador -ne 'DISPONIBLE') { throw 'Orquestador no disponible' }
    foreach ($s in $estado.servicios) {
        if ($s.estado -ne 'DISPONIBLE') { throw ('No hay comunicacion interna con: ' + $s.nombre) }
        Write-Host ('[OK] Orquestador -> ' + $s.nombre) -ForegroundColor Green
    }
} catch {
    Write-Host '[ERROR] Fallo la comunicacion de orquestador con SRI o ANT local' -ForegroundColor Red
    $fallos++
}

try {
    $respuesta = Invoke-WebRequest -Uri 'http://127.0.0.1:5173' -TimeoutSec 5 -UseBasicParsing
    if ($respuesta.StatusCode -ne 200) { throw 'React/Vite no devolvio HTTP 200' }
    Write-Host '[OK] React/Vite accesible' -ForegroundColor Green
} catch {
    Write-Host '[ERROR] React/Vite no esta accesible; inicia npm.cmd run dev' -ForegroundColor Red
    $fallos++
}

if ($fallos -gt 0) {
    Write-Host ('Hay ' + $fallos + ' comprobacion(es) pendiente(s).') -ForegroundColor Yellow
    exit 1
}
Write-Host 'FASE 1 APROBADA: conectividad local completa, sin consultas a proveedores.' -ForegroundColor Green
exit 0
