@echo off
setlocal EnableExtensions DisableDelayedExpansion

rem La ruta de %%~dp0 termina en \; eliminar esa barra evita que
rem Windows interprete la comilla final del argumento -D de Java
rem como escapada, dejando a Java sin clase principal.
set "PROJECT_ROOT=%~dp0"
set "PROJECT_DIR=%PROJECT_ROOT:~0,-1%"
set "WRAPPER_JAR=%PROJECT_ROOT%.mvn\wrapper\maven-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
  echo [ConsultaEC] Descargando Maven Wrapper 3.3.4 por primera vez...
  powershell.exe -NoProfile -Command "$ErrorActionPreference='Stop'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar' -OutFile '%WRAPPER_JAR%'"
  if errorlevel 1 (
    echo [ERROR] No se pudo descargar Maven Wrapper. Revisa tu conexion a Internet.
    exit /b 1
  )
)
if not exist "%WRAPPER_JAR%" (
  echo [ERROR] Falta el archivo Maven Wrapper JAR.
  exit /b 1
)

set "JAVA_EXE=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

pushd "%PROJECT_ROOT%"
if errorlevel 1 exit /b 1
"%JAVA_EXE%" "-Dmaven.multiModuleProjectDirectory=%PROJECT_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
set "EXITCODE=%ERRORLEVEL%"
popd
exit /b %EXITCODE%
