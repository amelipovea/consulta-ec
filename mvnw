#!/bin/sh
set -eu
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$ROOT_DIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo '[ConsultaEC] Descargando Maven Wrapper 3.3.4 por primera vez...'
  if command -v curl >/dev/null 2>&1; then
    curl -fLSs 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar' -o "$WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar' -O "$WRAPPER_JAR"
  else
    echo 'Se requiere curl o wget para descargar Maven Wrapper.' >&2
    exit 1
  fi
fi
cd "$ROOT_DIR"
exec java "-Dmaven.multiModuleProjectDirectory=$ROOT_DIR" -cp "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
