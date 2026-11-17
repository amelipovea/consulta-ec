package ec.edu.consultaec.ant.api;

import ec.edu.consultaec.ant.api.dto.PeticionPuntos;
import ec.edu.consultaec.ant.api.dto.RespuestaInternaAnt;
import ec.edu.consultaec.ant.aplicacion.ConsultarPuntosAnt;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/interno", produces = MediaType.APPLICATION_JSON_VALUE)
public class PuntosAntController {
    private final ConsultarPuntosAnt servicio;
    public PuntosAntController(ConsultarPuntosAnt servicio) { this.servicio = servicio; }

    @PostMapping(path = "/licencias/puntos/consultas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RespuestaInternaAnt consultar(@Valid @RequestBody PeticionPuntos peticion) {
        return servicio.consultar(peticion.cedula());
    }
}
