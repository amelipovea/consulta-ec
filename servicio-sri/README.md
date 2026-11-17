# Servicio independiente de Consultas SRI

- Ejecutable Spring Boot independiente con Java 17.
- Puerto local: `8081`.
- Health: `http://127.0.0.1:8081/actuator/health`.
- En esta fase solo se comprueba el arranque, **no** se consume ninguna API externa.

Desde la raíz: `.\mvnw.cmd -f servicio-sri\pom.xml spring-boot:run`.
