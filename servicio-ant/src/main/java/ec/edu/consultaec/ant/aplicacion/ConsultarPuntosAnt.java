package ec.edu.consultaec.ant.aplicacion;

import ec.edu.consultaec.ant.api.dto.RespuestaInternaAnt;
import ec.edu.consultaec.ant.dominio.ExcepcionAnt;
import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ConsultarPuntosAnt {
    private final ProveedorPuntosAnt proveedor;
    private final AlmacenUltimoDato cache;

    public ConsultarPuntosAnt(ProveedorPuntosAnt proveedor, AlmacenUltimoDato cache) {
        this.proveedor = proveedor;
        this.cache = cache;
    }

    public RespuestaInternaAnt consultar(String cedula) {
        try {
            UltimoDatoAnt dato = proveedor.consultar(cedula);
            validarDato(dato);
            try {
                cache.guardar(cedula, dato);
            } catch (RuntimeException ignorado) {
                // Redis fuera de servicio no invalida una respuesta reciente y válida.
            }
            return RespuestaInternaAnt.actual(dato.fuente(), dato.datos(), dato.consultadoEn());
        } catch (ExcepcionAnt error) {
            if (!error.falloTecnico()) throw error;
            try {
                Optional<UltimoDatoAnt> previo = cache.recuperar(cedula);
                if (previo.isPresent()) {
                    UltimoDatoAnt dato = previo.get();
                    validarDato(dato);
                    return RespuestaInternaAnt.cache(dato.fuente(), dato.datos(), dato.consultadoEn());
                }
            } catch (RuntimeException ignorado) {
                // Si Redis también falla, el resultado es una indisponibilidad explícita.
            }
            throw error;
        }
    }

    private void validarDato(UltimoDatoAnt dato) {
        if (dato == null || dato.consultadoEn() == null || dato.fuente() == null
                || dato.fuente().isBlank() || dato.datos() == null || !dato.datos().isObject()
                || !dato.datos().path("puntos").isIntegralNumber()
                || dato.datos().path("puntos").asInt() < 0) {
            throw new ExcepcionAnt(HttpStatus.BAD_GATEWAY, "RESPUESTA_ANT_INVALIDA",
                    "La respuesta ANT no contiene puntos válidos", true);
        }
    }
}
