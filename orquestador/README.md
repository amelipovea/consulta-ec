# API de Acceso y Orquestación – Fase 2

La API pública recibe solicitudes JSON, valida su formato y delega la consulta a uno de los dos
microservicios independientes. No consulta directamente servicios gubernamentales.

- Java 17, Spring Boot 3.5.16, puerto local 8080.
- Endpoints: `POST /api/v1/contribuyentes/consultas`, `POST /api/v1/vehiculos/consultas`,
  `POST /api/v1/licencias/puntos/consultas`.
- Validación con Bean Validation, errores JSON uniformes, tiempos de espera y errores de dependencia.
- `correo` se valida en la API pero **no se reenvía** al microservicio ni se almacena.
- La consulta del vehículo todavía **no realiza** una validación real del RUC; esto se implementará
  en la fase 3 dentro del servicio SRI.
- No se consultan todavía SRI ni ANT: los contratos internos de negocio no existen en los
  microservicios de fase 1. Una petición válida devolverá `501 CONTRATO_INTERNO_PENDIENTE`
  mientras estos estén activos y no tengan el endpoint solicitado.
- `503 DEPENDENCIA_NO_DISPONIBLE` si no se logra establecer comunicación con el microservicio.
- `502` si el microservicio responde con un error técnico o JSON incompatible.

Pruebas: `./mvnw.cmd -B -f orquestador/pom.xml test`, desde la raíz del proyecto.
Contrato y ejemplos: `docs/contratos/FASE_2_API_REST.md`.
