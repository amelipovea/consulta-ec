# Evaluación de usabilidad de ConsultaEC

## Objetivo

Evaluar si las personas pueden realizar consultas de contribuyentes y vehículos mediante el SRI y de puntos mediante la simulación ANT, reconocer los datos obligatorios e interpretar la fuente, la fecha y los avisos de los resultados.

## Metodología y alcance de la evidencia

Una persona interactuó con la interfaz y comunicó una observación cualitativa en una conversación. No se dispone de su identidad, características demográficas, tareas terminadas ni tiempos medidos. El hallazgo documentado se limita a la visibilidad del correo obligatorio. Las mejoras adicionales de esta página proceden de una revisión técnica de la interfaz; no se atribuyen a la participante.

La comprobación posterior de usabilidad con personas **está pendiente**. Las pruebas automatizadas y las capturas técnicas verifican comportamientos de software, pero no miden facilidad de uso percibida.

## Hallazgo real: correo obligatorio poco visible

> «Le cambiaría que sea más obvio que el mail es necesario para realizar cualquier consulta.»

La persona explicó: «Porque me pasó a mí al probarlo.» El principio afectado es la **prevención de errores**: al no advertir a tiempo el requisito, la persona puede avanzar en otros campos y descubrir el problema solo al enviar.

La propuesta fue destacar un único correo compartido antes de las tres consultas, explicar su obligatoriedad y guiar al campo cuando falta o es inválido. La implementación añade una sección titulada «Un correo para todas tus consultas», el texto «Obligatorio para realizar cualquier consulta.», una ayuda que nombra las tres operaciones, foco sobre el campo incorrecto, un error en línea anunciado mediante ARIA y una validación adicional en el cliente HTTP que impide enviar correos inválidos. El backend conserva su propia validación.

## Antes y después

| Aspecto | Antes, confirmado en el commit `ada34d3` | Mejora aplicada | Resultado esperado, no medido |
|---|---|---|---|
| Visibilidad del requisito | El correo aparecía en una tarjeta compartida sin un encabezado que explicara su alcance. | Sección destacada antes de los formularios, con encabezado y frase explícita de obligatoriedad. | Reconocer el requisito antes de comenzar una consulta. |
| Relación con las tres consultas | El campo era único, pero la ayuda solo indicaba que se usaba para validar solicitudes. | La ayuda y el texto de la sección nombran contribuyentes, vehículos y puntos. | Entender que se reutiliza el mismo correo. |
| Error al enviar sin correo | Se mostraba una validación en el campo situado por encima de los formularios, sin mover el foco hacia él. | Se valida primero el correo, se muestra un mensaje en línea y se enfoca el campo. | Corregir el error con menos búsqueda visual. |
| Corrección de un correo inválido | Los valores de RUC, placa y cédula ya permanecían en memoria. | Se conserva ese comportamiento; el error desaparece al corregir el correo. | Continuar sin repetir los otros datos. |
| Entrada del correo | El campo tenía tipo `email`, con autocompletado desactivado. | Se mantiene `type="email"` y se usa `autocomplete="email"`; la aplicación no guarda datos en almacenamiento persistente. | Facilitar la entrada en dispositivos compatibles. |

## Mejoras adicionales de la revisión técnica

Estas oportunidades se identificaron en la auditoría de ingeniería, **no** en el comentario de la participante.

| Problema u oportunidad | Propuesta | Implementación | Cómo se verificó |
|---|---|---|---|
| Una pulsación repetida podía iniciar otra solicitud antes de que React actualizara el botón. | Bloquear de forma inmediata los envíos simultáneos. | `ControlEnvio` protege la ejecución y libera el bloqueo tras éxito o error. | Prueba automatizada de envío duplicado y recuperación. |
| La carga dependía principalmente del texto del botón. | Anunciar el progreso de cada consulta. | Estado visible con `role="status"` y `aria-busy` en cada formulario. | Inspección del componente, pruebas y compilación. |
| Ayudas y errores eran pequeños y de contraste tenue. | Mejorar legibilidad sin cambiar la identidad visual. | Etiquetas, ayudas, errores y botones con tamaños y colores ajustados; botones de ancho completo en móvil. | Revisión de CSS y capturas técnicas locales en escritorio y ancho móvil; no constituyen un estudio con personas. |
| Un contrato ANT recuperado debe conservar su procedencia y fecha. | Mantener una advertencia inequívoca. | Se conservaron la etiqueta «Dato simulado», «Desde caché», el aviso «Información anterior» y `consultadoEn`. | Prueba de renderizado del componente con datos sintéticos. |

## Protocolo de validación posterior

Preparar la versión mejorada y usar solo datos sintéticos o propios autorizados. Observar sin dar pistas antes de cada tarea; anotar cualquier ayuda proporcionada. Para la prueba de caché, iniciar los cuatro procesos y Redis según el README y reiniciar solo ANT con fallo simulado.

| Tarea | Finalización | Duración real | Errores | Ayuda | Comentarios |
|---|---|---|---|---|---|
| 1. Intentar consultar un contribuyente con el correo inicialmente vacío. | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir |
| 2. Corregir un correo inválido y completar los campos requeridos. | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir |
| 3. Pasar a la consulta de vehículo después de confirmar persona natural. | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir |
| 4. Solicitar puntos ANT simulados e identificar su carácter sintético. | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir |
| 5. Interpretar el aviso de datos ANT recuperados y localizar la fecha original. | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir | Pendiente de medir |

Registrar fecha, versión, tamaño de pantalla y código anónimo de cada participante solo cuando se realice la sesión. No publicar correos, RUC, placas, cédulas ni capturas con datos personales reales. Hasta entonces, no se puede afirmar una mejora cuantitativa de usabilidad.
