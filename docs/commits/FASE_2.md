# Commits de la fase 2 – en español

Aplica el ZIP **en la raíz** `C:\AME\JAVA\consulta-ec`, donde existe el `pom.xml` general.
No extraigas una carpeta extra dentro de `consulta-ec`. El ZIP contiene únicamente archivos
nuevos o modificados, **no reemplaza** `frontend`, `servicio-sri`, `servicio-ant` ni `.git`.

**Importante:** confirma primero `git rev-parse --show-toplevel` → `C:/AME/JAVA/consulta-ec`.
Si no coincide, detente: podrías estar modificando un repositorio de otros proyectos.

Como los archivos se superponen juntos, usa `git add` por grupos para generar commits
separados; no uses `git add .`.

### Commit 1 – dependencias y configuración

```powershell
git add orquestador/pom.xml orquestador/src/main/resources/application.yml
git diff --cached --check
git commit -m "configuración: habilitar validaciones y tiempos de espera HTTP"
```

### Commit 2 – solicitudes, respuestas y errores

```powershell
git add orquestador/src/main/java/ec/edu/consultaec/orquestador/api/dto/
git add orquestador/src/main/java/ec/edu/consultaec/orquestador/api/ErroresRestControllerAdvice.java
git diff --cached --check
git commit -m "funcionalidad: definir contratos REST y errores estructurados"
```

### Commit 3 – coordinación y cliente interno

```powershell
git add orquestador/src/main/java/ec/edu/consultaec/orquestador/aplicacion/ClienteServiciosInternos.java
git add orquestador/src/main/java/ec/edu/consultaec/orquestador/aplicacion/CoordinadorConsultas.java
git add orquestador/src/main/java/ec/edu/consultaec/orquestador/aplicacion/ResultadoInterno.java
git add orquestador/src/main/java/ec/edu/consultaec/orquestador/aplicacion/DependenciaException.java
git add orquestador/src/main/java/ec/edu/consultaec/orquestador/api/ConsultasController.java
git add orquestador/src/main/java/ec/edu/consultaec/orquestador/api/EstadoSistemaController.java
git diff --cached --check
git commit -m "funcionalidad: implementar orquestación de consultas SRI y ANT"
```

### Commit 4 – pruebas

```powershell
.\mvnw.cmd -B clean test
git add orquestador/src/test/java/ec/edu/consultaec/orquestador/ValidacionesConsultasTests.java
git add orquestador/src/test/java/ec/edu/consultaec/orquestador/ConsultasControllerTests.java
git add orquestador/src/test/java/ec/edu/consultaec/orquestador/CoordinadorConsultasTests.java
git add orquestador/src/test/java/ec/edu/consultaec/orquestador/ClienteServiciosInternosTests.java
git diff --cached --check
git commit -m "pruebas: verificar validaciones contratos y fallos del orquestador"
```

### Commit 5 – guías y comprobación manual

```powershell
git add orquestador/README.md
git add docs/contratos/FASE_2_API_REST.md docs/avances/FASE_2.md docs/commits/FASE_2.md
git add scripts/probar-fase2.ps1 probar-fase2.cmd
git diff --cached --check
git commit -m "documentación: registrar contrato HTTP y evidencias de fase dos"
```

### Comprobar el resultado

```powershell
git status
git log --oneline -5
```

No hagas `push` hasta comprobar `git remote -v` y confirmar que pertenece al
repositorio nuevo de ConsultaEC.
