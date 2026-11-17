# Guía de commits separados en español — Fase 4

Ejecutar desde la raíz de `consulta-ec` DESPUÉS de obtener `BUILD SUCCESS` y comprobar la fase 4. Verificar `git rev-parse --show-toplevel` y `git status`. No crear commits de funcionalidades no verificadas.

**1. Contratos y validaciones ANT**

```powershell
git add servicio-ant/pom.xml servicio-ant/src/main/resources/application.yml
git add servicio-ant/src/main/java/ec/edu/consultaec/ant/api/
git add servicio-ant/src/main/java/ec/edu/consultaec/ant/dominio/
git commit -m "funcionalidad: definir contratos y validaciones del servicio ANT"
```

**2. Política de resiliencia**

```powershell
git add servicio-ant/src/main/java/ec/edu/consultaec/ant/aplicacion/
git commit -m "funcionalidad: recuperar ultimo dato ANT ante fallos tecnicos"
```

**3. Adaptadores del proveedor ANT y caché**

```powershell
git add servicio-ant/src/main/java/ec/edu/consultaec/ant/infraestructura/
git add servicio-ant/.env.example
git commit -m "funcionalidad: integrar adaptadores ANT y Redis con TTL"
```

**4. Pruebas automatizadas**

```powershell
git add servicio-ant/src/test/java/ec/edu/consultaec/ant/
git commit -m "pruebas: verificar cache y recuperacion frente a fallos ANT"
```

**5. Scripts y documentación**

```powershell
git add probar-fase4.cmd scripts/probar-fase4.ps1 scripts/probar-fase3.ps1
git add servicio-ant/README.md docs/avances/FASE_4.md docs/contratos/FASE_4_ANT.md docs/commits/FASE_4.md
git diff --cached --check
git commit -m "documentacion: registrar pruebas ANT y uso de Redis Cloud"
```

Hacer el push solo al remoto del proyecto ConsultaEC tras `git remote -v` y `git log --oneline -5`.

**Nota:** el código permite conectar Redis Cloud, pero para declarar que la conexión funciona hay que realizar y documentar la prueba real con credenciales propias. Los unit tests y el modo SIMULADO por sí solos no lo demuestran.
