package ec.edu.consultaec.orquestador.aplicacion;

import ec.edu.consultaec.orquestador.api.dto.MetadatosConsulta;
import ec.edu.consultaec.orquestador.api.dto.RespuestaConsulta;
import ec.edu.consultaec.orquestador.api.dto.SolicitudContribuyente;
import ec.edu.consultaec.orquestador.api.dto.SolicitudPuntos;
import ec.edu.consultaec.orquestador.api.dto.SolicitudVehiculo;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class CoordinadorConsultas {
    private final ClienteServiciosInternos cliente;

    public CoordinadorConsultas(ClienteServiciosInternos cliente) {
        this.cliente = cliente;
    }

    public RespuestaConsulta consultarContribuyente(SolicitudContribuyente solicitud) {
        return componer("CONTRIBUYENTE", cliente.consultarContribuyente(solicitud.ruc()));
    }

    public RespuestaConsulta consultarVehiculo(SolicitudVehiculo solicitud) {
        String placa = solicitud.placa().toUpperCase(Locale.ROOT);
        // SRI comprobará que el RUC es de persona natural antes de procesar la placa.
        return componer("VEHICULO", cliente.consultarVehiculo(solicitud.ruc(), placa));
    }

    public RespuestaConsulta consultarPuntos(SolicitudPuntos solicitud) {
        return componer("PUNTOS_LICENCIA", cliente.consultarPuntos(solicitud.cedula()));
    }

    private RespuestaConsulta componer(String tipo, ResultadoInterno interno) {
        return new RespuestaConsulta(
                tipo,
                interno.estado(),
                interno.fuente(),
                interno.datos(),
                new MetadatosConsulta(interno.consultadoEn(), interno.desdeCache())
        );
    }
}
