package ec.edu.consultaec.orquestador.api.dto;

import java.time.Instant;

public record MetadatosConsulta(Instant consultadoEn, boolean desdeCache) { }
