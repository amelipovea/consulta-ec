package ec.edu.consultaec.orquestador.api;

import ec.edu.consultaec.orquestador.aplicacion.EstadoDependenciasService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sistema")
public class EstadoSistemaController {

    private final EstadoDependenciasService estadoDependenciasService;

    public EstadoSistemaController(EstadoDependenciasService estadoDependenciasService) {
        this.estadoDependenciasService = estadoDependenciasService;
    }

    @GetMapping("/estado")
    public Map<String, Object> consultarEstado() {
        return Map.of(
                "aplicacion", "ConsultaEC",
                "fase", "2 - Contratos REST y orquestación",
                "orquestador", "DISPONIBLE",
                "servicios", estadoDependenciasService.consultarServicios()
        );
    }
}
