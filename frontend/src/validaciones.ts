/** Se validan formatos, no la existencia real de la persona ni del vehículo. */
export function validarCorreo(valor: string): string | null {
  const correo = valor.trim()
  if (!correo) return 'Ingresa tu correo electrónico.'
  if (correo.length > 254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
    return 'Ingresa un correo electrónico válido.'
  }
  return null
}

export function validarRuc(valor: string): string | null {
  if (!valor.trim()) return 'Ingresa el RUC del contribuyente.'
  if (!/^\d{13}$/.test(valor.trim())) return 'El RUC debe tener exactamente 13 dígitos.'
  return null
}

export function validarPlaca(valor: string): string | null {
  if (!valor.trim()) return 'Ingresa la placa del vehículo.'
  if (!/^[A-Z]{3}-?\d{3,4}$/i.test(valor.trim())) {
    return 'Usa tres letras y tres o cuatro números, con guion opcional.'
  }
  return null
}

export function validarCedula(valor: string): string | null {
  if (!valor.trim()) return 'Ingresa el número de cédula.'
  if (!/^\d{10}$/.test(valor.trim())) return 'La cédula debe tener exactamente 10 dígitos.'
  return null
}

export function esSimulacion(fuente: string): boolean {
  return /SIMULACION|SIMULADO|MOCK|DEMO/i.test(fuente)
}

export function formatearFecha(valor: string | null | undefined): string {
  if (!valor) return 'Fecha no disponible'
  const fecha = new Date(valor)
  if (Number.isNaN(fecha.getTime())) return 'Fecha no disponible'
  return new Intl.DateTimeFormat('es-EC', {
    dateStyle: 'medium', timeStyle: 'short',
  }).format(fecha)
}

const nombres: Record<string, string> = {
  ruc: 'RUC', placa: 'Placa', placaConsultada: 'Placa consultada',
  razonSocial: 'Razón social', nombreComercial: 'Nombre comercial',
  tipoContribuyente: 'Tipo de contribuyente', estadoContribuyente: 'Estado del contribuyente',
  claseContribuyente: 'Clase de contribuyente', actividadEconomicaPrincipal: 'Actividad económica',
  numeroPlaca: 'Número de placa', marca: 'Marca', modelo: 'Modelo',
  anioModelo: 'Año del modelo', anio: 'Año', 'año': 'Año',
  clase: 'Clase', tipo: 'Tipo', color: 'Color', servicio: 'Servicio',
  cilindraje: 'Cilindraje', puntos: 'Puntos de licencia',
}

export function nombreCampo(clave: string): string {
  return nombres[clave] ?? clave.replace(/([a-z\d])([A-Z])/g, '$1 $2').replace(/_/g, ' ')
    .replace(/^./, (letra) => letra.toUpperCase())
}

export type CampoResultado = { etiqueta: string; valor: string }

/** Resumen legible de la respuesta del backend, sin volcar JSON sin formato. */
export function camposResultado(datos: unknown, prefijo = '', limite = 30): CampoResultado[] {
  const campos: CampoResultado[] = []
  function visitar(valor: unknown, ruta: string, profundidad: number) {
    if (campos.length >= limite) return
    if (valor === null || valor === undefined) return
    if (Array.isArray(valor)) {
      valor.slice(0, 5).forEach((elemento, indice) => visitar(elemento, `${ruta}${ruta ? ' · ' : ''}Registro ${indice + 1}`, profundidad + 1))
      return
    }
    if (typeof valor === 'object' && profundidad < 4) {
      Object.entries(valor as Record<string, unknown>)
        .forEach(([clave, contenido]) => visitar(contenido, `${ruta}${ruta ? ' · ' : ''}${nombreCampo(clave)}`, profundidad + 1))
      return
    }
    let texto = typeof valor === 'string' ? valor : String(valor)
    if (!texto.trim()) texto = '—'
    campos.push({ etiqueta: ruta || 'Valor', valor: texto.length > 240 ? `${texto.slice(0, 240)}…` : texto })
  }
  visitar(datos, prefijo, 0)
  return campos
}
