package ec.edu.consultaec.sri.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record RespuestaInternaSri(String estado, String fuente, JsonNode datos, Metadatos metadatos) {
    public record Metadatos(Instant consultadoEn, boolean desdeCache) { }

    public static RespuestaInternaSri exito(JsonNode datos) {
        return new RespuestaInternaSri("OK", "SRI", datos, new Metadatos(Instant.now(), false));
    }
}
