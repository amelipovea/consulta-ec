package ec.edu.consultaec.ant.dominio;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record UltimoDatoAnt(JsonNode datos, Instant consultadoEn, String fuente) { }
