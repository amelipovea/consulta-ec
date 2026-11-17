import assert from 'node:assert/strict'
import { after, before, test } from 'node:test'
import { createElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { createServer } from 'vite'

let vite
let App
let Campo
let Resultado

before(async () => {
  vite = await createServer({ server: { middlewareMode: true }, appType: 'custom' })
  ;({ default: App, Campo, Resultado } = await vite.ssrLoadModule('/src/App.tsx'))
})
after(async () => { await vite?.close() })

test('el correo obligatorio se presenta antes de las tres consultas con semántica accesible', () => {
  const html = renderToStaticMarkup(createElement(App))
  assert.match(html, /Un correo para todas tus consultas/)
  assert.match(html, /Obligatorio para realizar cualquier consulta/)
  assert.match(html, /id="correo"[^>]*type="email"/)
  assert.match(html, /autoComplete="email"/)
  assert.match(html, /id="correo"[^>]*required/)
  assert.ok(html.indexOf('id="correo"') < html.indexOf('id="titulo-contribuyente"'))
  assert.ok(html.indexOf('id="correo"') < html.indexOf('id="titulo-puntos"'))
  assert.match(html, /Puntos de licencia simulados/)
  assert.match(html, /no son oficiales/)
})

test('un error del correo se asocia al campo y se anuncia', () => {
  const html = renderToStaticMarkup(createElement(Campo, {
    id: 'correo', titulo: 'Correo electrónico', valor: 'incorrecto',
    alCambiar() {}, alSalir() {}, error: 'Ingresa un correo electrónico válido.',
    ejemplo: 'nombre@ejemplo.com', ayuda: 'Obligatorio para todas las consultas.',
    maximo: 254, modo: 'email',
  }))
  assert.match(html, /aria-invalid="true"/)
  assert.match(html, /aria-describedby="correo-ayuda correo-error"/)
  assert.match(html, /id="correo-error"[^>]*role="alert"/)
})

test('el resultado ANT simulado y recuperado muestra advertencia, caché y fecha original', () => {
  const html = renderToStaticMarkup(createElement(Resultado, {
    titulo: 'Puntos de licencia', respuesta: {
      tipoConsulta: 'PUNTOS_LICENCIA', estado: 'OK', fuente: 'SIMULACION_ANT_CACHE',
      datos: { puntos: 20 }, metadatos: { consultadoEn: '2026-11-17T18:28:00Z', desdeCache: true },
    },
  }))
  assert.match(html, /Dato simulado/)
  assert.match(html, /Desde caché/)
  assert.match(html, /Información anterior/)
  assert.match(html, /podría haber cambiado/)
  assert.match(html, /Consultado:/)
  assert.match(html, /2026/)
})
