import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from 'react'
import {
  consultarContribuyente, consultarPuntos, consultarVehiculo, ErrorConsulta,
  obtenerEstadoSistema, type EstadoSistema, type RespuestaConsulta,
} from './api'
import {
  camposResultado, esSimulacion, formatearFecha,
  validarCedula, validarCorreo, validarPlaca, validarRuc,
} from './validaciones'

type TipoConsulta = 'contribuyente' | 'vehiculo' | 'puntos'
type NombreCampo = 'correo' | 'ruc' | 'placa' | 'cedula'
type Formulario = { correo: string; ruc: string; placa: string; cedula: string }
type Aviso = { titulo: string; mensaje: string; codigo?: string; detalles?: string[] }

const textoError = (error: unknown): Aviso => {
  if (error instanceof ErrorConsulta) {
    const detalles = error.detalles
      .filter((detalle) => detalle && typeof detalle.campo === 'string' && typeof detalle.mensaje === 'string')
      .map((detalle) => `${detalle.campo}: ${detalle.mensaje}`)
    const titulos: Record<string, string> = {
      VALIDACION_ERROR: 'Revisa la información ingresada',
      RUC_NO_ENCONTRADO: 'No se encontró el contribuyente',
      VEHICULO_NO_ENCONTRADO: 'No se encontró el vehículo',
      NO_ES_PERSONA_NATURAL: 'Esta consulta requiere una persona natural',
      ANT_NO_DISPONIBLE: 'La ANT no está disponible',
      ANT_SIN_CONTRATO_PUNTOS: 'No hay una integración oficial de puntos verificable',
      CONTRATO_INTERNO_PENDIENTE: 'Servicio en preparación',
      TIEMPO_AGOTADO: 'Se agotó el tiempo de espera',
    }
    return { titulo: titulos[error.codigo] || 'No se completó la consulta', mensaje: error.message, codigo: error.codigo, detalles }
  }
  return { titulo: 'No se completó la consulta', mensaje: 'Ocurrió un error inesperado. Intenta nuevamente.' }
}

function Campo({ id, titulo, valor, alCambiar, alSalir, error, ejemplo, ayuda, maximo, modo = 'text', deshabilitado = false }: {
  id: NombreCampo
  titulo: string
  valor: string
  alCambiar: (valor: string) => void
  alSalir: () => void
  error: string | null
  ejemplo: string
  ayuda: string
  maximo: number
  modo?: 'text' | 'email' | 'numeric'
  deshabilitado?: boolean
}) {
  const idAyuda = `${id}-ayuda`
  const idError = `${id}-error`
  return (
    <div className={`campo ${error ? 'campo-con-error' : ''}`}>
      <label htmlFor={id}>{titulo} <span aria-hidden="true">*</span></label>
      <div className="campo-entrada">
        <input
          id={id}
          name={id}
          type={modo === 'email' ? 'email' : 'text'}
          inputMode={modo === 'numeric' ? 'numeric' : modo === 'email' ? 'email' : 'text'}
          autoComplete="off"
          placeholder={ejemplo}
          maxLength={maximo}
          value={valor}
          disabled={deshabilitado}
          onChange={(evento) => alCambiar(evento.target.value)}
          onBlur={alSalir}
          aria-invalid={Boolean(error)}
          aria-describedby={`${idAyuda}${error ? ` ${idError}` : ''}`}
          required
        />
        {valor.trim() !== '' && <span className={`campo-icono ${error ? 'icono-error' : 'icono-ok'}`} aria-hidden="true">{error ? '!' : '✓'}</span>}
      </div>
      <p id={idAyuda} className="campo-ayuda">{ayuda}</p>
      {error && <p id={idError} className="campo-mensaje-error">{error}</p>}
    </div>
  )
}

function AvisoError({ aviso }: { aviso: Aviso | null }) {
  if (!aviso) return null
  return (
    <div role="alert" className="aviso aviso-error">
      <span className="aviso-simbolo" aria-hidden="true">!</span>
      <div>
        <strong>{aviso.titulo}</strong>
        <p>{aviso.mensaje}</p>
        {aviso.detalles && aviso.detalles.length > 0 && (
          <ul>{aviso.detalles.map((detalle, indice) => <li key={indice}>{detalle}</li>)}</ul>
        )}
        {aviso.codigo && <small>Código: {aviso.codigo}</small>}
      </div>
    </div>
  )
}

function Resultado({ respuesta, titulo }: { respuesta: RespuestaConsulta; titulo: string }) {
  const cache = respuesta.metadatos.desdeCache
  const simulado = esSimulacion(respuesta.fuente)
  const filas = camposResultado(respuesta.datos)
  const puntos = respuesta.tipoConsulta === 'PUNTOS_LICENCIA' &&
    respuesta.datos !== null && typeof respuesta.datos === 'object' && !Array.isArray(respuesta.datos)
    ? (respuesta.datos as Record<string, unknown>).puntos : undefined

  return (
    <div className="resultado" aria-live="polite">
      <div className="resultado-encabezado">
        <div className="resultado-bien">
          <span aria-hidden="true">✓</span>
          <div><strong>{titulo}</strong><p>Respuesta recibida del orquestador</p></div>
        </div>
        <div className="resultado-etiquetas">
          {simulado ? <span className="chip chip-prueba">Dato simulado</span> : <span className="chip chip-ok">Respuesta del servicio</span>}
          {cache && <span className="chip chip-cache">Desde caché</span>}
        </div>
      </div>
      {simulado && (
        <div className="aviso aviso-informacion" role="note">
          <span className="aviso-simbolo" aria-hidden="true">i</span>
          <div><strong>Demostración técnica</strong><p>Este resultado es sintético y no corresponde a puntos oficiales de la ANT.</p></div>
        </div>
      )}
      {cache && (
        <div className="aviso aviso-advertencia" role="note">
          <span className="aviso-simbolo" aria-hidden="true">!</span>
          <div><strong>Información anterior</strong><p>El proveedor no respondió. Se muestra el último dato válido disponible; podría haber cambiado.</p></div>
        </div>
      )}
      {typeof puntos === 'number' && Number.isInteger(puntos) && puntos >= 0 && (
        <div className="numero-puntos"><strong>{puntos}</strong><span>Puntos registrados en esta respuesta</span></div>
      )}
      <dl className="resultado-datos">
        {filas.length === 0 ? <div className="resultado-vacio">Sin campos para mostrar.</div> : filas.map((fila, indice) => (
          <div className="resultado-fila" key={`${fila.etiqueta}-${indice}`}>
            <dt>{fila.etiqueta}</dt><dd>{fila.valor}</dd>
          </div>
        ))}
      </dl>
      <div className="resultado-pie">
        <span>Fuente: <strong>{respuesta.fuente}</strong></span>
        <span>Consultado: <strong>{formatearFecha(respuesta.metadatos.consultadoEn)}</strong></span>
      </div>
    </div>
  )
}

function EncabezadoPaso({ numero, titulo, descripcion, etiqueta, id }: { numero: string; titulo: string; descripcion: string; etiqueta?: ReactNode; id: string }) {
  return (
    <div className="paso-encabezado">
      <span className="paso-numero">{numero}</span>
      <div className="paso-texto"><h2 id={id}>{titulo}</h2><p>{descripcion}</p></div>
      {etiqueta}
    </div>
  )
}

function Servicios({ estado, error, actualizar, cargando }: {
  estado: EstadoSistema | null
  error: string | null
  actualizar: () => void
  cargando: boolean
}) {
  const visibles = estado?.servicios ?? [
    { nombre: 'SRI', estado: 'NO_DISPONIBLE', detalle: 'Sin estado confirmado' },
    { nombre: 'ANT', estado: 'NO_DISPONIBLE', detalle: 'Sin estado confirmado' },
  ]
  return (
    <section id="estado-servicios" className="tarjeta-lateral" aria-labelledby="titulo-estado">
      <div className="lateral-encabezado">
        <div><span className="micro-etiqueta">MONITOREO LOCAL</span><h2 id="titulo-estado">Estado del sistema</h2></div>
        <button type="button" className="boton-actualizar" onClick={actualizar} disabled={cargando} aria-label="Actualizar el estado de los servicios" title="Actualizar estado">↻</button>
      </div>
      <div className="estado-resumen"><span className={`estado-punto ${estado ? 'activo' : ''}`} />API de orquestación: <strong>{estado ? 'Conectada' : 'Sin confirmar'}</strong></div>
      {visibles.map((servicio) => (
        <div className="servicio-fila" key={servicio.nombre}>
          <span className={`estado-punto ${servicio.estado === 'DISPONIBLE' ? 'activo' : ''}`} />
          <div><strong>Servicio {servicio.nombre}</strong><small>{servicio.estado === 'DISPONIBLE' ? 'Conexión interna disponible' : 'Sin conexión confirmada'}</small></div>
        </div>
      ))}
      {error && <p className="estado-error" role="status">{error}</p>}
      <p className="estado-nota">Estos indicadores comprueban los procesos locales; no garantizan que el SRI o la ANT externos estén disponibles.</p>
    </section>
  )
}

export default function App() {
  const [formulario, setFormulario] = useState<Formulario>({ correo: '', ruc: '', placa: '', cedula: '' })
  const [tocado, setTocado] = useState<Record<NombreCampo, boolean>>({ correo: false, ruc: false, placa: false, cedula: false })
  const [resultados, setResultados] = useState<Partial<Record<TipoConsulta, RespuestaConsulta>>>({})
  const [avisos, setAvisos] = useState<Partial<Record<TipoConsulta, Aviso>>>({})
  const [cargando, setCargando] = useState<Partial<Record<TipoConsulta, boolean>>>({})
  const [estadoSistema, setEstadoSistema] = useState<EstadoSistema | null>(null)
  const [estadoError, setEstadoError] = useState<string | null>(null)
  const [estadoCargando, setEstadoCargando] = useState(false)

  const refrescarEstado = useCallback(async () => {
    setEstadoCargando(true)
    try {
      setEstadoSistema(await obtenerEstadoSistema())
      setEstadoError(null)
    } catch {
      setEstadoSistema(null)
      setEstadoError('No se pudo obtener el estado del orquestador. Revisa el puerto 8080.')
    } finally {
      setEstadoCargando(false)
    }
  }, [])

  useEffect(() => { void refrescarEstado() }, [refrescarEstado])

  const validadores: Record<NombreCampo, (valor: string) => string | null> = {
    correo: validarCorreo, ruc: validarRuc, placa: validarPlaca, cedula: validarCedula,
  }
  const errorCampo = (campo: NombreCampo) => tocado[campo] ? validadores[campo](formulario[campo]) : null
  const ocupado = Boolean(cargando.contribuyente || cargando.vehiculo || cargando.puntos)
  const contribuyenteConfirmado = resultados.contribuyente?.estado === 'OK'

  function actualizarCampo(campo: NombreCampo, valor: string) {
    if (campo === 'placa') valor = valor.toUpperCase()
    setFormulario((actual) => ({ ...actual, [campo]: valor }))
    if (campo === 'correo') {
      setResultados({})
      setAvisos({})
    } else if (campo === 'ruc') {
      setResultados((actual) => ({ ...actual, contribuyente: undefined, vehiculo: undefined }))
      setAvisos((actual) => ({ ...actual, contribuyente: undefined, vehiculo: undefined }))
    } else {
      const tipo = campo === 'placa' ? 'vehiculo' : 'puntos'
      setResultados((actual) => ({ ...actual, [tipo]: undefined }))
      setAvisos((actual) => ({ ...actual, [tipo]: undefined }))
    }
  }

  async function ejecutar(tipo: TipoConsulta, tarea: () => Promise<RespuestaConsulta>) {
    setCargando((actual) => ({ ...actual, [tipo]: true }))
    setAvisos((actual) => ({ ...actual, [tipo]: undefined }))
    setResultados((actual) => ({ ...actual, [tipo]: undefined }))
    try {
      const respuesta = await tarea()
      setResultados((actual) => ({ ...actual, [tipo]: respuesta }))
    } catch (error) {
      setAvisos((actual) => ({ ...actual, [tipo]: textoError(error) }))
    } finally {
      setCargando((actual) => ({ ...actual, [tipo]: false }))
    }
  }

  function consultarContribuyenteFormulario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setTocado((actual) => ({ ...actual, correo: true, ruc: true }))
    if (validarCorreo(formulario.correo) || validarRuc(formulario.ruc)) return
    void ejecutar('contribuyente', () => consultarContribuyente(formulario.correo.trim(), formulario.ruc.trim()))
  }

  function consultarVehiculoFormulario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setTocado((actual) => ({ ...actual, placa: true }))
    if (!contribuyenteConfirmado || validarPlaca(formulario.placa)) return
    void ejecutar('vehiculo', () => consultarVehiculo(formulario.correo.trim(), formulario.ruc.trim(), formulario.placa.trim().toUpperCase()))
  }

  function consultarPuntosFormulario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setTocado((actual) => ({ ...actual, correo: true, cedula: true }))
    if (validarCorreo(formulario.correo) || validarCedula(formulario.cedula)) return
    void ejecutar('puntos', () => consultarPuntos(formulario.correo.trim(), formulario.cedula.trim()))
  }

  return (
    <div className="app">
      <header className="barra-principal">
        <div className="contenedor barra-interior">
          <a className="logo" href="#inicio" aria-label="ConsultaEC, volver al inicio"><span className="logo-icono">C<span>·</span></span><span>Consulta<span className="logo-acento">EC</span></span></a>
          <nav className="menu" aria-label="Navegación"><a href="#consultas">Consultas</a><a href="#estado-servicios">Estado del sistema</a></nav>
          <span className="identificador-portal"><span className="identificador-punto" /> Portal académico</span>
        </div>
      </header>

      <main id="inicio">
        <section className="hero">
          <div className="contenedor hero-interior">
            <div className="hero-contenido">
              <div className="hero-sobrelinea"><span className="hero-linea" /> INFORMACIÓN VEHICULAR Y TRIBUTARIA</div>
              <h1>Tus consultas,<br /><em>en un solo lugar.</em></h1>
              <p>Consulta información de contribuyentes y vehículos del SRI, y verifica la disponibilidad de puntos de licencia de la ANT desde una interfaz clara y segura.</p>
              <a href="#consultas" className="hero-boton">Comenzar consulta <span aria-hidden="true">↗</span></a>
            </div>
            <div className="hero-ilustracion" aria-hidden="true">
              <span className="orbita orbita-uno" /><span className="orbita orbita-dos" />
              <div className="ilustracion-circulo"><span className="ilustracion-centro">EC</span></div>
              <div className="sello sello-arriba"><span>◈</span> Datos conectados</div>
              <div className="sello sello-abajo"><span>✓</span> Consultas organizadas</div>
            </div>
          </div>
        </section>

        <div className="contenedor contenido-principal" id="consultas">
          <div className="columna-formularios">
            <div className="titulo-seccion"><span className="micro-etiqueta">CENTRO DE CONSULTAS</span><h2>¿Qué necesitas consultar?</h2><p>Completa tus datos y avanza paso a paso. Los campos obligatorios están marcados con un asterisco.</p></div>
            <div className="campo correo-compartido">
              <Campo id="correo" titulo="Correo electrónico" valor={formulario.correo} alCambiar={(v) => actualizarCampo('correo', v)} alSalir={() => setTocado((t) => ({ ...t, correo: true }))} error={errorCampo('correo')} ejemplo="nombre@ejemplo.com" ayuda="Se utiliza para validar las solicitudes. No lo almacenamos en el navegador." maximo={254} modo="email" deshabilitado={ocupado} />
            </div>

            <section className="paso-tarjeta" aria-labelledby="titulo-contribuyente">
              <EncabezadoPaso id="titulo-contribuyente" numero="01" titulo="Datos del contribuyente" descripcion="Comprueba la información del RUC en el SRI." etiqueta={contribuyenteConfirmado ? <span className="chip chip-ok">Verificado</span> : undefined} />
              <form onSubmit={consultarContribuyenteFormulario} noValidate>
                <Campo id="ruc" titulo="RUC de persona natural" valor={formulario.ruc} alCambiar={(v) => actualizarCampo('ruc', v)} alSalir={() => setTocado((t) => ({ ...t, ruc: true }))} error={errorCampo('ruc')} ejemplo="13 dígitos" ayuda="La existencia y tipo de contribuyente se confirman con el SRI." maximo={13} modo="numeric" deshabilitado={ocupado} />
                <button type="submit" className="boton-principal" disabled={ocupado}>{cargando.contribuyente ? 'Consultando SRI…' : 'Consultar contribuyente'} <span aria-hidden="true">→</span></button>
              </form>
              <AvisoError aviso={avisos.contribuyente ?? null} />
              {resultados.contribuyente && <Resultado respuesta={resultados.contribuyente} titulo="Contribuyente encontrado" />}
            </section>

            <section className={`paso-tarjeta ${!contribuyenteConfirmado ? 'paso-bloqueado' : ''}`} aria-labelledby="titulo-vehiculo">
              <EncabezadoPaso id="titulo-vehiculo" numero="02" titulo="Información del vehículo" descripcion="Consulta los datos de un vehículo mediante su placa." etiqueta={!contribuyenteConfirmado ? <span className="chip chip-espera">Paso anterior requerido</span> : undefined} />
              {!contribuyenteConfirmado && <div className="aviso aviso-pendiente"><span aria-hidden="true">○</span><p>Primero debes confirmar que el RUC pertenece a una persona natural.</p></div>}
              <form onSubmit={consultarVehiculoFormulario} noValidate>
                <Campo id="placa" titulo="Placa del vehículo" valor={formulario.placa} alCambiar={(v) => actualizarCampo('placa', v)} alSalir={() => setTocado((t) => ({ ...t, placa: true }))} error={errorCampo('placa')} ejemplo="ABC-1234" ayuda="Formato: tres letras y tres o cuatro números." maximo={8} deshabilitado={!contribuyenteConfirmado || ocupado} />
                <button type="submit" className="boton-principal" disabled={!contribuyenteConfirmado || ocupado}>{cargando.vehiculo ? 'Consultando placa…' : 'Consultar vehículo'} <span aria-hidden="true">→</span></button>
              </form>
              <AvisoError aviso={avisos.vehiculo ?? null} />
              {resultados.vehiculo && <Resultado respuesta={resultados.vehiculo} titulo="Información vehicular disponible" />}
            </section>

            <section className="paso-tarjeta" aria-labelledby="titulo-puntos">
              <EncabezadoPaso id="titulo-puntos" numero="03" titulo="Puntos de licencia" descripcion="Consulta independiente mediante tu número de cédula." etiqueta={<span className="chip chip-independiente">Consulta independiente</span>} />
              <form onSubmit={consultarPuntosFormulario} noValidate>
                <Campo id="cedula" titulo="Número de cédula" valor={formulario.cedula} alCambiar={(v) => actualizarCampo('cedula', v)} alSalir={() => setTocado((t) => ({ ...t, cedula: true }))} error={errorCampo('cedula')} ejemplo="10 dígitos" ayuda="La ANT puede estar temporalmente indisponible." maximo={10} modo="numeric" deshabilitado={ocupado} />
                <button type="submit" className="boton-principal" disabled={ocupado}>{cargando.puntos ? 'Consultando ANT…' : 'Consultar puntos'} <span aria-hidden="true">→</span></button>
              </form>
              <AvisoError aviso={avisos.puntos ?? null} />
              {resultados.puntos && <Resultado respuesta={resultados.puntos} titulo="Respuesta de puntos de licencia" />}
            </section>
          </div>

          <aside className="columna-lateral" aria-label="Información adicional">
            <Servicios estado={estadoSistema} error={estadoError} actualizar={() => void refrescarEstado()} cargando={estadoCargando} />
            <section className="tarjeta-lateral lateral-consejos">
              <span className="micro-etiqueta">ANTES DE EMPEZAR</span><h2>Una consulta más sencilla</h2>
              <div className="consejo"><span className="consejo-numero">01</span><p>Ten a mano el RUC de la persona natural que deseas consultar.</p></div>
              <div className="consejo"><span className="consejo-numero">02</span><p>Después de validarlo, podrás consultar la información de la placa.</p></div>
              <div className="consejo"><span className="consejo-numero">03</span><p>La sección ANT es independiente y puede mostrar información anterior de caché.</p></div>
            </section>
            <section className="tarjeta-lateral lateral-transparencia">
              <span className="lateral-icono" aria-hidden="true">◇</span>
              <h2>Transparencia de datos</h2>
              <p>Cuando una respuesta provenga de una simulación o de Redis, lo indicaremos de forma visible junto a la fecha de origen.</p>
              <p>La disponibilidad de nuestros servicios no garantiza la de los portales institucionales.</p>
            </section>
          </aside>
        </div>
      </main>

      <footer className="pie-pagina"><div className="contenedor pie-interior"><span className="logo logo-pie"><span className="logo-icono">C<span>·</span></span> Consulta<span className="logo-acento">EC</span></span><p>Proyecto académico de diseño y arquitectura de software.</p><a href="#inicio">Volver arriba ↑</a></div></footer>
    </div>
  )
}
