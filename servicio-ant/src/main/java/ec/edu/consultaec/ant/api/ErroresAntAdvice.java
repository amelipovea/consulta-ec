package ec.edu.consultaec.ant.api;

import ec.edu.consultaec.ant.dominio.ExcepcionAnt;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErroresAntAdvice {
    @ExceptionHandler(ExcepcionAnt.class)
    public ResponseEntity<Map<String, Object>> proveedor(ExcepcionAnt error) {
        return responder(error.estadoHttp(), error.codigo(), error.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException error) {
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION_ERROR", "Revisa los datos enviados");
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException error) {
        return responder(HttpStatus.BAD_REQUEST, "CUERPO_JSON_INVALIDO", "Envía un JSON válido");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception error) {
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO_ANT",
                "Error interno al consultar puntos");
    }
    private ResponseEntity<Map<String, Object>> responder(HttpStatus estado, String codigo, String mensaje) {
        Map<String, Object> datos = Map.of("codigo", codigo, "mensaje", mensaje,
                "solicitudId", UUID.randomUUID().toString(), "instante", Instant.now().toString(),
                "detalles", List.of());
        return ResponseEntity.status(estado).body(Map.of("error", datos));
    }
}
