package ec.edu.consultaec.orquestador.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record RespuestaConsulta(
        String tipoConsulta,
        String estado,
        String fuente,
        JsonNode datos,
        MetadatosConsulta metadatos
) { }
