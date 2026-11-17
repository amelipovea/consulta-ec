import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  consultarContribuyente, consultarVehiculo, consultarPuntos,
  obtenerEstadoSistema, ErrorConsulta,
} from '../src/api.ts'

// Estas pruebas interceptan fetch: no envían identificaciones a ningún servidor.
globalThis.window = { setTimeout, clearTimeout }

const resultado = {
  tipoConsulta: 'CONTRIBUYENTE', estado: 'OK', fuente: 'SRI',
  datos: { ruc: '9999999999001', tipoContribuyente: 'PERSONA NATURAL' },
  metadatos: { consultadoEn: '2026-11-17T00:00:00Z', desdeCache: false },
}

function responder(estado, cuerpo) {
  return { ok: estado >= 200 && estado < 300, status: estado, async json() { return cuerpo } }
}

test('contribuyente consulta exclusivamente la API pública del orquestador', async () => {
  let ruta = ''
  let carga
  globalThis.fetch = async (url, opciones) => {
    ruta = url
    carga = JSON.parse(opciones.body)
    return responder(200, resultado)
  }
  const respuesta = await consultarContribuyente('ejemplo@example.com', '9999999999001')
  assert.equal(ruta, '/api/v1/contribuyentes/consultas')
  assert.deepEqual(carga, { correo: 'ejemplo@example.com', ruc: '9999999999001' })
  assert.equal(respuesta.fuente, 'SRI')
})

test('vehículo envía correo, RUC y placa al orquestador', async () => {
  let carga
  globalThis.fetch = async (_, opciones) => {
    carga = JSON.parse(opciones.body)
    return responder(200, { ...resultado, tipoConsulta: 'VEHICULO' })
  }
  await consultarVehiculo('ejemplo@example.com', '9999999999001', 'ABC-1234')
  assert.equal(carga.placa, 'ABC-1234')
})

test('puntos de licencia conserva etiqueta de simulación y caché', async () => {
  globalThis.fetch = async () => responder(200, {
    ...resultado, tipoConsulta: 'PUNTOS_LICENCIA', fuente: 'SIMULACION_ANT_CACHE',
    datos: { puntos: 20 }, metadatos: { ...resultado.metadatos, desdeCache: true },
  })
  const respuesta = await consultarPuntos('ejemplo@example.com', '9999999999')
  assert.equal(respuesta.metadatos.desdeCache, true)
  assert.equal(respuesta.fuente, 'SIMULACION_ANT_CACHE')
})

test('un error HTTP 400 conserva el código de validación del backend', async () => {
  globalThis.fetch = async () => responder(400, {
    error: { codigo: 'VALIDACION_ERROR', mensaje: 'Corrige los campos indicados',
      detalles: [{ campo: 'ruc', mensaje: 'RUC inválido' }] },
  })
  await assert.rejects(
    consultarContribuyente('ejemplo@example.com', '0000000000000'),
    (error) => error instanceof ErrorConsulta && error.codigo === 'VALIDACION_ERROR'
      && error.detalles[0].campo === 'ruc',
  )
})

test('un error ANT 503 no inventa resultados', async () => {
  globalThis.fetch = async () => responder(503, { error: { codigo: 'ANT_NO_DISPONIBLE', mensaje: 'Servicio no disponible' } })
  await assert.rejects(consultarPuntos('ejemplo@example.com', '9999999999'),
    (error) => error instanceof ErrorConsulta && error.codigo === 'ANT_NO_DISPONIBLE')
})

test('el estado de servicios se obtiene de la API propia', async () => {
  let ruta = ''
  globalThis.fetch = async (url) => {
    ruta = url
    return responder(200, { aplicacion: 'ConsultaEC', orquestador: 'DISPONIBLE', servicios: [] })
  }
  await obtenerEstadoSistema()
  assert.equal(ruta, '/api/v1/sistema/estado')
})

test('rechaza puntos inválidos y fecha o tipo de consulta incorrectos', async () => {
  const base = { ...resultado, tipoConsulta: 'PUNTOS_LICENCIA', datos: { puntos: -1 } }
  globalThis.fetch = async () => responder(200, base)
  await assert.rejects(consultarPuntos('prueba@example.com', '9999999999'),
    (error) => error instanceof ErrorConsulta && error.codigo === 'CONTRATO_INVALIDO')
  globalThis.fetch = async () => responder(200, { ...base, datos: { puntos: 20 },
    metadatos: { ...base.metadatos, consultadoEn: 'fecha-inválida' } })
  await assert.rejects(consultarPuntos('prueba@example.com', '9999999999'),
    (error) => error instanceof ErrorConsulta && error.codigo === 'CONTRATO_INVALIDO')
  globalThis.fetch = async () => responder(200, { ...base, datos: { puntos: 20 }, tipoConsulta: 'VEHICULO' })
  await assert.rejects(consultarPuntos('prueba@example.com', '9999999999'),
    (error) => error instanceof ErrorConsulta && error.codigo === 'CONTRATO_INVALIDO')
})
