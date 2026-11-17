$ErrorActionPreference = 'Stop'
$fallos = 0

function Comprobar-Endpoint($nombre, $url, $tipo) {
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 8
        if ($r.StatusCode -ne 200) { throw "HTTP $($r.StatusCode)" }
        if ($tipo -eq 'json') {
            $contenido = $r.Content | ConvertFrom-Json
            if (-not $contenido.aplicacion -or -not $contenido.servicios) {
                throw 'La estructura JSON no coincide con el estado esperado'
            }
        } else {
            if ($r.Content -notmatch 'ConsultaEC') { throw 'No se reconoce la pagina ConsultaEC' }
        }
        Write-Host "[OK] ${nombre}"
    } catch {
        Write-Host "[ERROR] ${nombre}: $($_.Exception.Message)"
        $script:fallos++
    }
}

Write-Host 'ConsultaEC - Fase 5: conectividad local y portal web'
Comprobar-Endpoint 'Frontend React (5173)' 'http://127.0.0.1:5173/' 'html'
Comprobar-Endpoint 'Orquestador (8080)' 'http://127.0.0.1:8080/api/v1/sistema/estado' 'json'

foreach ($servicio in @(@('Servicio SRI', 'http://127.0.0.1:8081/actuator/health'), @('Servicio ANT', 'http://127.0.0.1:8082/actuator/health'))) {
    try {
        $respuesta = Invoke-RestMethod -Uri $servicio[1] -TimeoutSec 8
        if ($respuesta.status -ne 'UP') { throw 'El servicio no reporta UP' }
        Write-Host "[OK] $($servicio[0])"
    } catch {
        Write-Host "[ERROR] $($servicio[0]): $($_.Exception.Message)"
        $fallos++
    }
}

if ($fallos -eq 0) {
    Write-Host '[OK] FASE 5: frontend y servicios locales accesibles'
    exit 0
} else {
    Write-Host "[ERROR] FASE 5: $fallos error(es)"
    exit 1
}
