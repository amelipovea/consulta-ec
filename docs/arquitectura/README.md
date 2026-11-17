# Arquitectura C4 vigente

- **React** es la interfaz y solo consume la API propia de orquestación.
- **Orquestador Java** recibe solicitudes y se comunica por REST interno con los dos servicios especializados.
- **Servicio SRI Java** encapsula tres operaciones: existencia del RUC, datos del contribuyente y consulta vehicular por placa.
- **Servicio ANT Java** ofrece un proveedor simulado autorizado, un adaptador para el portal JSP y recuperación ante fallos técnicos.
- **Redis local en Docker** conserva el último resultado ANT válido durante el TTL configurado. Solo ANT lo accede.

Las aplicaciones son procesos separados, con sus propios `pom.xml`, puertos y control de arranque. Se mantendrá la coherencia con los diagramas C1, C2 y C3 de ConsultaEC.

El endpoint de diagnóstico comprueba conectividad entre procesos locales. No confirma la disponibilidad externa ni la validez oficial de una consulta. Los diagramas IcePanel pueden estar desactualizados; consulte [ACTUALIZACION_ICEPANEL.md](ACTUALIZACION_ICEPANEL.md).

IcePanel: https://s.icepanel.io/n43YWpkfdsi2st/hsOQ
