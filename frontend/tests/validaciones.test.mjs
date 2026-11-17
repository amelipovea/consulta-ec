import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  validarCorreo, validarRuc, validarPlaca, validarCedula,
  esSimulacion, camposResultado, nombreCampo, formatearFecha,
} from '../src/validaciones.ts'

test('correo valida formato sin aceptar cadena vacía', () => {
  assert.equal(validarCorreo('estudiante@example.com'), null)
  assert.notEqual(validarCorreo(''), null)
  assert.notEqual(validarCorreo('sin-arroba'), null)
})
test('RUC requiere exactamente 13 dígitos', () => {
  assert.equal(validarRuc('9999999999001'), null)
  assert.notEqual(validarRuc('abc'), null)
  assert.notEqual(validarRuc('123456789012'), null)
})
test('placa acepta guion opcional y rechaza formatos incorrectos', () => {
  assert.equal(validarPlaca('ABC-1234'), null)
  assert.equal(validarPlaca('abc123'), null)
  assert.notEqual(validarPlaca('12-ABC'), null)
})
test('cédula requiere exactamente 10 dígitos', () => {
  assert.equal(validarCedula('9999999999'), null)
  assert.notEqual(validarCedula('999999999'), null)
})
test('identifica simulaciones y las distingue de fuentes oficiales', () => {
  assert.equal(esSimulacion('SIMULACION_ANT_CACHE'), true)
  assert.equal(esSimulacion('SRI'), false)
})
test('convierte estructuras internas en campos legibles', () => {
  assert.equal(nombreCampo('razonSocial'), 'Razón social')
  const filas = camposResultado({ vehiculos: [{ numeroPlaca: 'ABC-1234', marca: 'Marca de prueba' }] })
  assert.ok(filas.some((fila) => fila.etiqueta.includes('Número de placa') && fila.valor === 'ABC-1234'))
  assert.ok(filas.some((fila) => fila.valor === 'Marca de prueba'))
})
test('una fecha inválida no rompe la interfaz', () => {
  assert.equal(formatearFecha('no-es-fecha'), 'Fecha no disponible')
})
