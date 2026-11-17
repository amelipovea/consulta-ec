# ConsultaEC — Interfaz React (Fase 5)

La interfaz se construye en **React 19 + TypeScript + Vite**, y consume exclusivamente la API pública del orquestador. No llama a SRI, ANT o Redis desde el navegador.

## Uso en Windows / PowerShell

Con los tres servicios Java activos en **8080, 8081 y 8082**:

```powershell
cd C:\AME\JAVA\consulta-ec\frontend
npm.cmd install
npm.cmd run test
npm.cmd run build
npm.cmd run dev
```

Abrir **http://127.0.0.1:5173**. Si Vite estaba ejecutándose antes de extraer el ZIP, detenerlo con `Ctrl+C` y reiniciarlo.

El archivo `vite.config.ts` ya redirige `/api` a `http://127.0.0.1:8080`; el archivo `.env.example` permite cambiar el destino de desarrollo si se usa otro puerto. **No** se requiere configurar CORS ni colocar credenciales SRI/ANT/Redis en React.

## Contratos utilizados (fase 2)

| Acción del formulario | Endpoint público | Campos enviados |
|---|---|---|
| Contribuyente | `POST /api/v1/contribuyentes/consultas` | `correo`, `ruc` |
| Vehículo | `POST /api/v1/vehiculos/consultas` | `correo`, `ruc`, `placa` |
| Puntos | `POST /api/v1/licencias/puntos/consultas` | `correo`, `cedula` |
| Salud interna | `GET /api/v1/sistema/estado` | Ninguno |

Las consultas se envían solamente al pulsar el botón del formulario; no se envían automáticamente al escribir. La consulta vehicular requiere haber confirmado antes un contribuyente persona natural. La consulta ANT puede realizarse sin consultar el RUC, pero siempre requiere un correo válido.

El correo obligatorio se presenta en una sección compartida antes de los tres formularios. Si falta o es inválido, la interfaz muestra un error en línea, enfoca el campo y no envía la solicitud. El cliente HTTP vuelve a validar el correo como defensa adicional. El campo usa semántica HTML de correo y autocompletado del navegador; la aplicación no lo guarda en almacenamiento persistente. Los formularios anuncian la carga e impiden solicitudes simultáneas repetidas.

## Comportamientos previstos

- Validaciones inmediatas de formato (la verificación de existencia corresponde al SRI).
- Indicación clara de errores `400`, `404`, `422`, `502`, `503` y `501` sin mostrar datos falsos.
- Manejo de carga y timeout de 12 segundos en React.
- Visualización de `fuente`, `metadatos.desdeCache` y `metadatos.consultadoEn`.
- Etiqueta **«Dato simulado»** cuando la fuente empieza por `SIMULACION` u otra marca de prueba.
- Aviso **«Información anterior»** cuando el backend recupera caché.
- Los estados de salud local **no** se presentan como disponibilidad del servicio externo.

## Privacidad y alcance

El frontend no almacena RUC, placa, cédula ni correo en `localStorage`, `sessionStorage` ni cookies propias; solo mantiene el estado en memoria mientras se utiliza la página. No hagas capturas ni commits con información personal real. Consulta el portal solamente con datos propios o autorizados.

**ANT:** el proveedor real continúa sin un contrato JSON de puntos verificable; la simulación de la fase 4 no constituye datos oficiales. **Redis local en Docker:** la conexión y el TTL se verifican por separado; una etiqueta de caché en la interfaz no basta como evidencia de Redis operativo.
