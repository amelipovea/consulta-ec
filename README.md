# ConsultaEC

Proyecto académico de arquitectura C4 con React, Java/Spring Boot y una futura integración de Redis Cloud.

**Esta descarga corresponde a la fase 1: estructura y comunicación de arranque.** Las integraciones externas reales todavía NO están implementadas. No contiene credenciales ni datos personales.

## Arquitectura acordada en IcePanel

```text
Ciudadano → React :5173 → Orquestador Spring Boot :8080
                                  ├── Servicio SRI :8081 → [SRI externo: FASE FUTURA]
                                  └── Servicio ANT :8082 → [ANT externo: FASE FUTURA]
                                                          ↘ [Redis Cloud: FASE FUTURA]
```

Los tres proyectos Java son aplicaciones ejecutables **independientes**. El `pom.xml` de la raíz únicamente facilita compilarlas juntas; NO las convierte en un único microservicio.

Modelo de referencia: https://s.icepanel.io/n43YWpkfdsi2st/hsOQ (en IcePanel buscar los diagramas `ConsultaEC`). Cuando se cree el repositorio de la compañera, deberá enlazar su propio repositorio en IcePanel.

## Requisitos

- Windows 10/11 y PowerShell o CMD.
- Java 17, comprobado con `java -version`.
- Node.js y npm; desde PowerShell utilizar `npm.cmd` si `npm.ps1` está bloqueado.
- Git para los commits.
- Conexión a Internet **la primera vez**, para que Maven Wrapper y npm descarguen dependencias.
- No se necesita Docker ni Redis para esta fase.

## Preparación

1. Extrae el contenido del ZIP dentro de `C:\AME\JAVA\consulta-ec` (debe quedar `C:\AME\JAVA\consulta-ec\pom.xml`, no dos carpetas `consulta-ec` anidadas).
2. Abre esa carpeta en VS Code.
3. Comprueba las herramientas: `java -version`, `node -v`, `npm.cmd -v`, `git --version`.
4. La primera vez, compila desde la raíz:

```powershell
.\mvnw.cmd -B clean test
```

La primera ejecución de `mvnw.cmd` descargará el Maven Wrapper y una distribución de Maven. Es normal que tarde más la primera vez. Los archivos descargados se ignoran en Git.

## Iniciar las cuatro aplicaciones (4 terminales diferentes)

**Terminal 1: Servicio SRI**

```powershell
cd C:\AME\JAVA\consulta-ec
.\mvnw.cmd -f servicio-sri\pom.xml spring-boot:run
```

**Terminal 2: Servicio ANT**

```powershell
cd C:\AME\JAVA\consulta-ec
.\mvnw.cmd -f servicio-ant\pom.xml spring-boot:run
```

**Terminal 3: API de Orquestación**

```powershell
cd C:\AME\JAVA\consulta-ec
.\mvnw.cmd -f orquestador\pom.xml spring-boot:run
```

**Terminal 4: React**

```powershell
cd C:\AME\JAVA\consulta-ec\frontend
npm.cmd install
npm.cmd run dev
```

Abre **http://127.0.0.1:5173**. Verás un panel de arranque con el estado de SRI y ANT obtenido pasando por el orquestador. Los estados son del propio sistema local, **no** corresponden a la disponibilidad real de los portales externos.

## Probar si realmente están conectados

Con las cuatro aplicaciones iniciadas, abre una **quinta terminal** en la raíz y ejecuta:

```powershell
.\probar-fase1.cmd
```

Deberías ver: 3 servicios Java operativos, SRI y ANT `DISPONIBLE` **localmente**, y React accesible. También puedes visitar:

- http://127.0.0.1:8080/actuator/health
- http://127.0.0.1:8081/actuator/health
- http://127.0.0.1:8082/actuator/health
- http://127.0.0.1:8080/api/v1/sistema/estado

Cuando cierres una terminal de servicio con Ctrl+C y vuelvas a pulsar **Actualizar estados** en React, verás ese servicio como `NO_DISPONIBLE`. Este escenario es solo una comprobación del encadenamiento HTTP local.

## Qué NO hace aún la fase 1

- No consulta RUC, personas naturales ni placas en el SRI real.
- No realiza consultas ANT ni interpreta su página JSP.
- No usa Redis Cloud ni cachea resultados.
- No solicita cédulas ni RUC reales.
- No implementa todavía el contrato de negocio definitivo.

En fases siguientes implementaremos **orquestación → consultas SRI → consulta ANT/Redis Cloud → formularios y pruebas integrales**, conservando exactamente estos cuatro ejecutables.

## Commits en español

Antes de hacer el primer commit, comprueba `git status`. Si la carpeta no es aún un repositorio:

```powershell
git init -b main
git add .gitignore .gitattributes .editorconfig .mvn mvnw.cmd mvnw pom.xml README.md probar-fase1.cmd scripts docs frontend orquestador servicio-sri servicio-ant
git diff --cached --check
git diff --cached --stat
git commit -m "inicio: preparar estructura y arranque de ConsultaEC"
```

No ejecutes `git add .` indiscriminadamente. No ejecutes `git push` hasta comprobar que el remoto apunta a **su propio repositorio**, nunca al repositorio del otro proyecto. Después configura el remoto y publica con `git push -u origin main`.

## Nota de diseño

La URL ANT suministrada en el enunciado termina en `clp_grid_citaciones.jsp`; no asumiremos un contrato REST JSON ni puntos reales hasta verificar el flujo autorizado. El futuro fallback indicará procedencia y fecha de los datos y no convertirá errores en cero puntos.
