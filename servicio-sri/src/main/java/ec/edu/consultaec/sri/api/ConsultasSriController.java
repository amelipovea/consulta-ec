package ec.edu.consultaec.sri.api;

import ec.edu.consultaec.sri.api.dto.PeticionRuc;
import ec.edu.consultaec.sri.api.dto.PeticionVehiculo;
import ec.edu.consultaec.sri.api.dto.RespuestaInternaSri;
import ec.edu.consultaec.sri.aplicacion.ServicioContribuyentes;
import ec.edu.consultaec.sri.aplicacion.ServicioVehiculos;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/interno", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConsultasSriController {
    private final ServicioContribuyentes contribuyentes;
    private final ServicioVehiculos vehiculos;

    public ConsultasSriController(ServicioContribuyentes contribuyentes, ServicioVehiculos vehiculos) {
        this.contribuyentes = contribuyentes;
        this.vehiculos = vehiculos;
    }

    @PostMapping(path = "/contribuyentes/consultas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RespuestaInternaSri consultarContribuyente(@Valid @RequestBody PeticionRuc solicitud) {
        return RespuestaInternaSri.exito(contribuyentes.consultarPersonaNatural(solicitud.ruc()));
    }

    @PostMapping(path = "/vehiculos/consultas", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RespuestaInternaSri consultarVehiculo(@Valid @RequestBody PeticionVehiculo solicitud) {
        return RespuestaInternaSri.exito(vehiculos.consultar(solicitud.ruc(), solicitud.placa()));
    }
}
