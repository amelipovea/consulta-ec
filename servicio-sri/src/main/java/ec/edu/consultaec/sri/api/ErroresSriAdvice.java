package ec.edu.consultaec.sri.api;

import ec.edu.consultaec.sri.dominio.ExcepcionSri;
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
public class ErroresSriAdvice {

    @ExceptionHandler(ExcepcionSri.class)
    public ResponseEntity<Map<String, Object>> errorSri(ExcepcionSri error) {
        return responder(error.getEstadoHttp(), error.getCodigo(), error.getMessage());
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
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO_SRI",
                "Ocurrió un error interno en el microservicio SRI");
    }

    private ResponseEntity<Map<String, Object>> responder(HttpStatus estado, String codigo, String mensaje) {
        Map<String, Object> detalle = Map.of(
                "codigo", codigo,
                "mensaje", mensaje,
                "solicitudId", UUID.randomUUID().toString(),
                "instante", Instant.now().toString(),
                "detalles", List.of()
        );
        return ResponseEntity.status(estado).body(Map.of("error", detalle));
    }
}
