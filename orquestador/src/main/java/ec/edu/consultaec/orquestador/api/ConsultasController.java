package ec.edu.consultaec.orquestador.api;

import ec.edu.consultaec.orquestador.api.dto.RespuestaConsulta;
import ec.edu.consultaec.orquestador.api.dto.SolicitudContribuyente;
import ec.edu.consultaec.orquestador.api.dto.SolicitudPuntos;
import ec.edu.consultaec.orquestador.api.dto.SolicitudVehiculo;
import ec.edu.consultaec.orquestador.aplicacion.CoordinadorConsultas;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConsultasController {
    private final CoordinadorConsultas coordinador;

    public ConsultasController(CoordinadorConsultas coordinador) {
        this.coordinador = coordinador;
    }

    @PostMapping(path = "/contribuyentes/consultas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RespuestaConsulta contribuyente(@Valid @RequestBody SolicitudContribuyente solicitud) {
        return coordinador.consultarContribuyente(solicitud);
    }

    @PostMapping(path = "/vehiculos/consultas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RespuestaConsulta vehiculo(@Valid @RequestBody SolicitudVehiculo solicitud) {
        return coordinador.consultarVehiculo(solicitud);
    }

    @PostMapping(path = "/licencias/puntos/consultas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RespuestaConsulta puntos(@Valid @RequestBody SolicitudPuntos solicitud) {
        return coordinador.consultarPuntos(solicitud);
    }
}
