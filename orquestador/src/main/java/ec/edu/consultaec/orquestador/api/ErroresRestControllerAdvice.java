package ec.edu.consultaec.orquestador.api;

import ec.edu.consultaec.orquestador.api.dto.RespuestaError;
import ec.edu.consultaec.orquestador.aplicacion.DependenciaException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErroresRestControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> validacion(MethodArgumentNotValidException error) {
        List<RespuestaError.Detalle> detalles = error.getBindingResult().getFieldErrors().stream()
                .map(campo -> new RespuestaError.Detalle(campo.getField(),
                        campo.getDefaultMessage() == null ? "Valor incorrecto" : campo.getDefaultMessage()))
                .sorted(Comparator.comparing(RespuestaError.Detalle::campo)
                        .thenComparing(RespuestaError.Detalle::mensaje))
                .distinct()
                .toList();
        return crear(HttpStatus.BAD_REQUEST, "VALIDACION_ERROR",
                "Corrige los campos indicados", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> jsonInvalido(HttpMessageNotReadableException error) {
        return crear(HttpStatus.BAD_REQUEST, "CUERPO_JSON_INVALIDO",
                "La solicitud debe contener un JSON válido", List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<RespuestaError> formatoInvalido(HttpMediaTypeNotSupportedException error) {
        return crear(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_CONTENIDO_NO_SOPORTADO",
                "Envía Content-Type: application/json", List.of());
    }

    @ExceptionHandler(DependenciaException.class)
    public ResponseEntity<RespuestaError> dependencia(DependenciaException error) {
        return crear(error.getEstadoHttp(), error.getCodigo(), error.getMessage(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> inesperado(Exception error) {
        // Nunca se publican trazas, datos personales ni mensajes internos en la API pública.
        return crear(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado en el orquestador", List.of());
    }

    private ResponseEntity<RespuestaError> crear(HttpStatus estado, String codigo, String mensaje,
                                                  List<RespuestaError.Detalle> detalles) {
        return ResponseEntity.status(estado).body(new RespuestaError(
                new RespuestaError.Error(codigo, mensaje,
                        UUID.randomUUID().toString(), Instant.now(), detalles)));
    }
}
