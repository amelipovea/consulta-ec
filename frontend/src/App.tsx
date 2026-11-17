import { useEffect, useState } from 'react'

type Servicio = {
  nombre: string
  estado: 'DISPONIBLE' | 'NO_DISPONIBLE'
  detalle: string
}

type RespuestaEstado = {
  aplicacion: string
  fase: string
  orquestador: string
  servicios: Servicio[]
}

export default function App() {
  const [estado, setEstado] = useState<RespuestaEstado | null>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const comprobar = async () => {
    setCargando(true)
    setError(null)
    try {
      const controller = new AbortController()
      const timeout = window.setTimeout(() => controller.abort(), 5000)
      try {
        const response = await fetch('/api/v1/sistema/estado', {
          signal: controller.signal,
          headers: { Accept: 'application/json' },
        })
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        const data = (await response.json()) as RespuestaEstado
        if (!Array.isArray(data.servicios)) throw new Error('Respuesta inesperada')
        setEstado(data)
      } finally {
        window.clearTimeout(timeout)
      }
    } catch {
      setEstado(null)
      setError('No se pudo comunicar con la API de orquestación. Comprueba el puerto 8080.')
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    void comprobar()
  }, [])

  return (
    <div className="sitio">
      <header className="cabecera">
        <a className="marca" href="#inicio" aria-label="ConsultaEC, ir al inicio">
          <span className="marca-simbolo">CE</span>
          <span>ConsultaEC</span>
        </a>
        <span className="etiqueta-superior">Entorno de desarrollo · Fase 1</span>
      </header>

      <main id="inicio" className="contenido">
        <p className="sobrelinea">Arquitectura de microservicios</p>
        <h1>Cuatro aplicaciones. <span>Una experiencia.</span></h1>
        <p className="introduccion">
          Esta pantalla comprueba que React se comunique con la API de orquestación,
          y que esta pueda localizar los servicios independientes de SRI y ANT.
        </p>

        <div className="barra-acciones">
          <button type="button" onClick={() => void comprobar()} disabled={cargando}>
            {cargando ? 'Comprobando…' : 'Actualizar estados'}
          </button>
          <span className="informacion">Sin consultas a instituciones externas.</span>
        </div>

        {error && <div className="alerta" role="alert">{error}</div>}

        <section aria-label="Estado de servicios" className="panel">
          <div className="panel-titulo">
            <div>
              <p className="sobrelinea">Verificación local</p>
              <h2>Estado de los servicios</h2>
            </div>
            <span className="estado-orquestador">Orquestador: {estado?.orquestador ?? 'SIN CONEXIÓN'}</span>
          </div>

          <div className="tarjetas">
            {(estado?.servicios ?? [
              { nombre: 'SRI', estado: 'NO_DISPONIBLE', detalle: 'Sin información del orquestador' },
              { nombre: 'ANT', estado: 'NO_DISPONIBLE', detalle: 'Sin información del orquestador' },
            ]).map((servicio) => (
              <article className="tarjeta" key={servicio.nombre}>
                <div className="tarjeta-cabecera">
                  <strong>Servicio {servicio.nombre}</strong>
                  <span className={servicio.estado === 'DISPONIBLE' ? 'indicador disponible' : 'indicador ausente'}>
                    {servicio.estado === 'DISPONIBLE' ? '● Conectado' : '○ No disponible'}
                  </span>
                </div>
                <p>{servicio.detalle}</p>
              </article>
            ))}
          </div>
        </section>

        <p className="nota">
          <strong>Alcance:</strong> los estados muestran comunicación entre procesos locales.
          No confirman que el SRI o la ANT estén disponibles. Las consultas reales y Redis Cloud
          se implementarán en fases posteriores.
        </p>
      </main>
      <footer>ConsultaEC · Proyecto académico · Primera fase</footer>
    </div>
  )
}
