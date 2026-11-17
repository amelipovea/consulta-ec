# API de Acceso y Orquestación

- Ejecutable Spring Boot independiente con Java 17.
- Puerto local: `8080`.
- Health: `http://127.0.0.1:8080/actuator/health`.
- En esta fase solo se comprueba el arranque, **no** se consume ninguna API externa.

Desde la raíz: `.\mvnw.cmd -f orquestador\pom.xml spring-boot:run`.
