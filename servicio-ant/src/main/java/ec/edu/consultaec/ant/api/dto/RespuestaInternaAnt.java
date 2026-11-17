package ec.edu.consultaec.ant.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record RespuestaInternaAnt(String estado, String fuente, JsonNode datos, Metadatos metadatos) {
    public record Metadatos(Instant consultadoEn, boolean desdeCache) { }
    public static RespuestaInternaAnt actual(String fuente, JsonNode datos, Instant consultadoEn) {
        return new RespuestaInternaAnt("OK", fuente, datos, new Metadatos(consultadoEn, false));
    }
    public static RespuestaInternaAnt cache(String fuente, JsonNode datos, Instant consultadoEn) {
        return new RespuestaInternaAnt("OK", fuente + "_CACHE", datos, new Metadatos(consultadoEn, true));
    }
}
