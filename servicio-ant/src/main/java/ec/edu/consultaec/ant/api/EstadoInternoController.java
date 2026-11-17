package ec.edu.consultaec.ant.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interno")
public class EstadoInternoController {

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of(
                "servicio", "ANT",
                "estado", "LISTO",
                "detalle", "Servicio de puntos ANT; la simulación y Redis local se configuran por entorno"
        );
    }
}
