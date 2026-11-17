# Servicio independiente de Puntos ANT

- Ejecutable Spring Boot independiente con Java 17.
- Puerto local: `8082`.
- Health: `http://127.0.0.1:8082/actuator/health`.
- En esta fase solo se comprueba el arranque, **no** se consume ninguna API externa.

Desde la raíz: `.\mvnw.cmd -f servicio-ant\pom.xml spring-boot:run`.
