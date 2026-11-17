/** Este cliente HTTP consume exclusivamente la API del orquestador (puerto 8080). */
import { validarCorreo } from './validaciones.ts'
export type ServicioEstado = {
  nombre: string
  estado: 'DISPONIBLE' | 'NO_DISPONIBLE' | string
  detalle: string
}

export type EstadoSistema = {
  aplicacion: string
  fase: string
  orquestador: string
  servicios: ServicioEstado[]
}

export type RespuestaConsulta = {
  tipoConsulta: 'CONTRIBUYENTE' | 'VEHICULO' | 'PUNTOS_LICENCIA'
  estado: string
  fuente: string
  datos: unknown
  metadatos: {
    consultadoEn: string
    desdeCache: boolean
  }
}

export type ErrorCampo = { campo: string; mensaje: string }

type CuerpoError = {
  error?: {
    codigo?: string
    mensaje?: string
    detalles?: ErrorCampo[]
    solicitudId?: string
  }
}

export class ErrorConsulta extends Error {
  readonly codigo: string
  readonly estadoHttp: number
  readonly detalles: ErrorCampo[]
  readonly solicitudId?: string

  constructor(codigo: string, message: string, estadoHttp: number, detalles: ErrorCampo[] = [], solicitudId?: string) {
    super(message)
    this.name = 'ErrorConsulta'
    this.codigo = codigo
    this.estadoHttp = estadoHttp
    this.detalles = detalles
    this.solicitudId = solicitudId
  }
}

async function solicitud<T>(ruta: string, datos?: Record<string, string>): Promise<T> {
  const abortador = new AbortController()
  const tiempo = window.setTimeout(() => abortador.abort(), 12000)
  try {
    const respuesta = await fetch(ruta, {
      method: datos ? 'POST' : 'GET',
      headers: {
        Accept: 'application/json',
        ...(datos ? { 'Content-Type': 'application/json' } : {}),
      },
      ...(datos ? { body: JSON.stringify(datos) } : {}),
      credentials: 'same-origin',
      cache: 'no-store',
      signal: abortador.signal,
    })
    let cuerpo: unknown
    try {
      cuerpo = await respuesta.json()
    } catch {
      throw new ErrorConsulta('RESPUESTA_NO_JSON', 'El servidor no devolvió una respuesta JSON válida.', respuesta.status)
    }
    if (!respuesta.ok) {
      const error = (cuerpo as CuerpoError | null)?.error
      throw new ErrorConsulta(
        error?.codigo || `HTTP_${respuesta.status}`,
        error?.mensaje || `La solicitud no pudo completarse (HTTP ${respuesta.status}).`,
        respuesta.status,
        Array.isArray(error?.detalles) ? error.detalles : [],
        error?.solicitudId,
      )
    }
    if (!cuerpo || typeof cuerpo !== 'object') {
      throw new ErrorConsulta('CONTRATO_INVALIDO', 'La API devolvió un formato no reconocido.', respuesta.status)
    }
    return cuerpo as T
  } catch (error) {
    if (error instanceof ErrorConsulta) throw error
    if (error instanceof Error && error.name === 'AbortError') {
      throw new ErrorConsulta('TIEMPO_AGOTADO', 'La consulta tardó demasiado. Vuelve a intentarlo.', 0)
    }
    throw new ErrorConsulta('SIN_CONEXION', 'No se pudo conectar con la API de orquestación. Revisa el servicio del puerto 8080.', 0)
  } finally {
    window.clearTimeout(tiempo)
  }
}

function validarRespuesta(respuesta: RespuestaConsulta, tipoEsperado: RespuestaConsulta['tipoConsulta']): RespuestaConsulta {
  if (respuesta.tipoConsulta !== tipoEsperado || respuesta.estado !== 'OK' ||
      typeof respuesta.fuente !== 'string' || !respuesta.fuente.trim() || !respuesta.metadatos ||
      typeof respuesta.metadatos.desdeCache !== 'boolean' ||
      typeof respuesta.metadatos.consultadoEn !== 'string' ||
      Number.isNaN(Date.parse(respuesta.metadatos.consultadoEn)) ||
      respuesta.datos === null || respuesta.datos === undefined) {
    throw new ErrorConsulta('CONTRATO_INVALIDO', 'La API devolvió una respuesta de consulta incompleta.', 200)
  }
  if (tipoEsperado === 'PUNTOS_LICENCIA') {
    const puntos = typeof respuesta.datos === 'object' && !Array.isArray(respuesta.datos) &&
      (respuesta.datos as Record<string, unknown>).puntos
    if (typeof puntos !== 'number' || !Number.isInteger(puntos) || puntos < 0) {
      throw new ErrorConsulta('CONTRATO_INVALIDO', 'La API devolvió puntos de licencia inválidos.', 200)
    }
  }
  return respuesta
}

export async function obtenerEstadoSistema(): Promise<EstadoSistema> {
  const estado = await solicitud<EstadoSistema>('/api/v1/sistema/estado')
  if (!Array.isArray(estado.servicios)) {
    throw new ErrorConsulta('CONTRATO_INVALIDO', 'No se pudo interpretar el estado de los servicios.', 200)
  }
  return estado
}

export async function consultarContribuyente(correo: string, ruc: string): Promise<RespuestaConsulta> {
  return validarRespuesta(await solicitud<RespuestaConsulta>('/api/v1/contribuyentes/consultas', { correo: exigirCorreo(correo), ruc }), 'CONTRIBUYENTE')
}

export async function consultarVehiculo(correo: string, ruc: string, placa: string): Promise<RespuestaConsulta> {
  return validarRespuesta(await solicitud<RespuestaConsulta>('/api/v1/vehiculos/consultas', { correo: exigirCorreo(correo), ruc, placa }), 'VEHICULO')
}

export async function consultarPuntos(correo: string, cedula: string): Promise<RespuestaConsulta> {
  return validarRespuesta(await solicitud<RespuestaConsulta>('/api/v1/licencias/puntos/consultas', { correo: exigirCorreo(correo), cedula }), 'PUNTOS_LICENCIA')
}

function exigirCorreo(correo: string): string {
  const mensaje = validarCorreo(correo)
  if (mensaje) throw new ErrorConsulta('VALIDACION_ERROR', mensaje, 400, [{ campo: 'correo', mensaje }])
  return correo.trim()
}
