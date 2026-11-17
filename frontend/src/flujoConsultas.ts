import { validarCedula, validarCorreo, validarPlaca, validarRuc } from './validaciones.ts'

export type TipoConsulta = 'contribuyente' | 'vehiculo' | 'puntos'
export type NombreCampo = 'correo' | 'ruc' | 'placa' | 'cedula'
export type Formulario = Record<NombreCampo, string>

/** El correo compartido se revisa primero para guiar al campo que afecta a las tres consultas. */
export function primerCampoInvalido(tipo: TipoConsulta, formulario: Formulario): NombreCampo | null {
  if (validarCorreo(formulario.correo)) return 'correo'
  if (tipo === 'contribuyente' && validarRuc(formulario.ruc)) return 'ruc'
  if (tipo === 'vehiculo' && validarPlaca(formulario.placa)) return 'placa'
  if (tipo === 'puntos' && validarCedula(formulario.cedula)) return 'cedula'
  return null
}

/** Bloquea un segundo envío hasta que termine el primero, incluso antes de que React renderice. */
export class ControlEnvio {
  private ocupado = false

  estaOcupado(): boolean { return this.ocupado }

  async ejecutar(tarea: () => Promise<void>): Promise<boolean> {
    if (this.ocupado) return false
    this.ocupado = true
    try {
      await tarea()
      return true
    } finally {
      this.ocupado = false
    }
  }
}
