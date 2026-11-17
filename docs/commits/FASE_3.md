# Commits en español — Fase 3

Trabajar desde `C:\AME\JAVA\consulta-ec`. No crear los commits hasta obtener `BUILD SUCCESS` y ejecutar el script. Los commits clasifican el código por responsabilidad, aunque el compilador solo puede validar el conjunto completo una vez instalado.

Verificar primero: `git status`, `git rev-parse --show-toplevel` (debe terminar en `consulta-ec`).

## Commit 1 — Acceso al SRI y configuración

```powershell
git add servicio-sri/pom.xml servicio-sri/src/main/resources/application.yml
git add servicio-sri/src/main/java/ec/edu/consultaec/sri/dominio/
git add servicio-sri/src/main/java/ec/edu/consultaec/sri/infraestructura/ClienteHttpSri.java
git diff --cached --check
git commit -m "funcionalidad: conectar adaptador HTTP con los endpoints del SRI"
```

## Commit 2 — Normalización segura

```powershell
git add servicio-sri/src/main/java/ec/edu/consultaec/sri/infraestructura/NormalizadorSri.java
git commit -m "funcionalidad: normalizar y validar respuestas externas del SRI"
```

## Commit 3 — Casos de uso y API interna

```powershell
git add servicio-sri/src/main/java/ec/edu/consultaec/sri/aplicacion/
git add servicio-sri/src/main/java/ec/edu/consultaec/sri/api/
git commit -m "funcionalidad: integrar contribuyentes y vehículos en el servicio SRI"
```

## Commit 4 — Pruebas

```powershell
git add servicio-sri/src/test/java/ec/edu/consultaec/sri/
git commit -m "pruebas: comprobar consultas SRI con respuestas sintéticas"
```

## Commit 5 — Documentación y script de verificación

```powershell
git add servicio-sri/README.md docs/avances/FASE_3.md docs/contratos/FASE_3_SRI.md docs/commits/FASE_3.md
git add scripts/probar-fase3.ps1 probar-fase3.cmd
git commit -m "documentación: registrar contratos y evidencias de integración SRI"
```

`git push origin main` solo después de revisar `git remote -v` y confirmar el repositorio correcto.
