package ec.edu.consultaec.orquestador.aplicacion;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record ResultadoInterno(
        String estado,
        String fuente,
        JsonNode datos,
        Instant consultadoEn,
        boolean desdeCache
) { }
