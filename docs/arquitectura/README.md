# Arquitectura – fase 1

- **React** es la interfaz y solo consume la API propia de orquestación.
- **Orquestador Java** recibe solicitudes y se comunica por REST interno con los dos servicios especializados.
- **Servicio SRI Java** encapsulará las tres operaciones SRI (existencia del RUC, datos del contribuyente y consulta vehicular).
- **Servicio ANT Java** encapsulará una integración web JSP de disponibilidad baja, estrategia de timeout y fallback.
- **Redis Cloud** será un servicio administrado externo usado por ANT para conservar el último resultado válido.

Las aplicaciones son procesos separados, con sus propios `pom.xml`, puertos y control de arranque. Se mantendrá la coherencia con los diagramas C1, C2 y C3 de ConsultaEC.

**Todavía no hay integraciones de negocio:** el endpoint de diagnóstico de esta fase únicamente comprueba conectividad entre procesos locales.

IcePanel: https://s.icepanel.io/n43YWpkfdsi2st/hsOQ
