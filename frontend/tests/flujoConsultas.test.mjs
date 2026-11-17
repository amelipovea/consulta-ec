import assert from 'node:assert/strict'
import { test } from 'node:test'
import { ControlEnvio, primerCampoInvalido } from '../src/flujoConsultas.ts'

const datos = { correo: 'prueba@example.com', ruc: '9999999999001', placa: 'ABC-1234', cedula: '9999999999' }

test('el correo compartido tiene prioridad en las tres consultas', () => {
  for (const tipo of ['contribuyente', 'vehiculo', 'puntos']) {
    assert.equal(primerCampoInvalido(tipo, { ...datos, correo: '' }), 'correo')
    assert.equal(primerCampoInvalido(tipo, { ...datos, correo: 'incorrecto' }), 'correo')
    assert.equal(primerCampoInvalido(tipo, datos), null)
  }
})

test('corregir el correo conserva RUC, placa y cédula; luego se guía al siguiente campo incorrecto', () => {
  const formulario = { ...datos, correo: 'incorrecto', ruc: '' }
  assert.equal(primerCampoInvalido('contribuyente', formulario), 'correo')
  formulario.correo = 'prueba@example.com'
  assert.equal(primerCampoInvalido('contribuyente', formulario), 'ruc')
  assert.equal(formulario.placa, datos.placa)
  assert.equal(formulario.cedula, datos.cedula)
})

test('el control impide envíos repetidos durante la carga y se libera tras éxito o error', async () => {
  const control = new ControlEnvio()
  let finalizar
  let llamadas = 0
  const primera = control.ejecutar(() => { llamadas++; return new Promise((resolver) => { finalizar = resolver }) })
  assert.equal(control.estaOcupado(), true)
  assert.equal(await control.ejecutar(async () => { llamadas++ }), false)
  assert.equal(llamadas, 1)
  finalizar()
  assert.equal(await primera, true)
  assert.equal(control.estaOcupado(), false)
  await assert.rejects(control.ejecutar(async () => { throw new Error('Fallo simulado') }))
  assert.equal(control.estaOcupado(), false)
  assert.equal(await control.ejecutar(async () => { llamadas++ }), true)
  assert.equal(llamadas, 2)
})
