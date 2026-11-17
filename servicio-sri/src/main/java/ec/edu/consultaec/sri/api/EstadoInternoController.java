package ec.edu.consultaec.sri.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interno")
public class EstadoInternoController {
    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("servicio", "SRI", "estado", "LISTO",
                "detalle", "Adaptador SRI integrado; disponibilidad externa no garantizada");
    }
}
