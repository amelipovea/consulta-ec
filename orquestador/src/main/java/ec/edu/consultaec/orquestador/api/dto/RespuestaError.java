package ec.edu.consultaec.orquestador.api.dto;

import java.time.Instant;
import java.util.List;

public record RespuestaError(Error error) {
    public record Error(
            String codigo,
            String mensaje,
            String solicitudId,
            Instant instante,
            List<Detalle> detalles
    ) { }

    public record Detalle(String campo, String mensaje) { }
}
