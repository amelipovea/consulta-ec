package ec.edu.consultaec.orquestador.aplicacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ClienteServiciosInternos {
    private static final String RUTA_RUC = "/api/v1/interno/contribuyentes/consultas";
    private static final String RUTA_PLACA = "/api/v1/interno/vehiculos/consultas";
    private static final String RUTA_PUNTOS = "/api/v1/interno/licencias/puntos/consultas";
    private static final Set<String> NO_ENCONTRADOS = Set.of(
            "RUC_NO_ENCONTRADO", "VEHICULO_NO_ENCONTRADO", "PUNTOS_NO_ENCONTRADOS");

    private final RestClient sri;
    private final RestClient ant;
    private final ObjectMapper json;

    public ClienteServiciosInternos(
            @Value("${consultaec.servicios.sri.url}") String sriUrl,
            @Value("${consultaec.servicios.ant.url}") String antUrl,
            @Value("${consultaec.http.tiempo-conexion-ms:1500}") int conexionMs,
            @Value("${consultaec.http.tiempo-lectura-ms:3000}") int lecturaMs,
            ObjectMapper json) {
        SimpleClientHttpRequestFactory peticiones = new SimpleClientHttpRequestFactory();
        peticiones.setConnectTimeout(conexionMs);
        peticiones.setReadTimeout(lecturaMs);
        this.sri = RestClient.builder().baseUrl(sriUrl).requestFactory(peticiones).build();
        this.ant = RestClient.builder().baseUrl(antUrl).requestFactory(peticiones).build();
        this.json = json;
    }

    public ResultadoInterno consultarContribuyente(String ruc) {
        return ejecutar(sri, "SRI", RUTA_RUC, Map.of("ruc", ruc));
    }

    public ResultadoInterno consultarVehiculo(String ruc, String placa) {
        return ejecutar(sri, "SRI", RUTA_PLACA, Map.of("ruc", ruc, "placa", placa));
    }

    public ResultadoInterno consultarPuntos(String cedula) {
        return ejecutar(ant, "ANT", RUTA_PUNTOS, Map.of("cedula", cedula));
    }

    private ResultadoInterno ejecutar(RestClient cliente, String proveedor, String ruta, Object datos) {
        try {
            JsonNode cuerpo = cliente.post().uri(ruta)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(datos)
                    .retrieve()
                    .body(JsonNode.class);
            return verificarContrato(proveedor, cuerpo);
        } catch (HttpClientErrorException.NotFound error) {
            throw interpretarNoEncontrado(proveedor, error);
        } catch (RestClientResponseException error) {
            if (error.getStatusCode().value() == 422) {
                DependenciaException negocio = interpretarErrorDeNegocio(error);
                if (negocio != null) throw negocio;
            }
            if (error.getStatusCode().value() == 503) {
                throw new DependenciaException(HttpStatus.SERVICE_UNAVAILABLE,
                        "DEPENDENCIA_NO_DISPONIBLE", "El microservicio " + proveedor + " no está disponible");
            }
            throw new DependenciaException(HttpStatus.BAD_GATEWAY,
                    "ERROR_SERVICIO_INTERNO", "El microservicio " + proveedor
                    + " devolvió HTTP " + error.getStatusCode().value());
        } catch (ResourceAccessException error) {
            throw new DependenciaException(HttpStatus.SERVICE_UNAVAILABLE,
                    "DEPENDENCIA_NO_DISPONIBLE", "No se pudo conectar con el microservicio " + proveedor);
        } catch (RestClientException error) {
            throw new DependenciaException(HttpStatus.BAD_GATEWAY,
                    "ERROR_SERVICIO_INTERNO", "Respuesta incorrecta del microservicio " + proveedor);
        }
    }

    private ResultadoInterno verificarContrato(String proveedor, JsonNode cuerpo) {
        if (cuerpo == null || !cuerpo.isObject()) throw respuestaInvalida(proveedor);
        String estado = cuerpo.path("estado").asText("");
        String fuente = cuerpo.path("fuente").asText("");
        JsonNode datos = cuerpo.get("datos");
        JsonNode meta = cuerpo.get("metadatos");
        if (!"OK".equals(estado) || fuente.isBlank() || datos == null || datos.isNull()
                || meta == null || !meta.isObject()
                || !meta.path("desdeCache").isBoolean()) {
            throw respuestaInvalida(proveedor);
        }
        try {
            Instant instante = Instant.parse(meta.path("consultadoEn").asText(""));
            return new ResultadoInterno(estado, fuente, datos, instante, meta.path("desdeCache").asBoolean());
        } catch (DateTimeParseException error) {
            throw respuestaInvalida(proveedor);
        }
    }

    private DependenciaException respuestaInvalida(String proveedor) {
        return new DependenciaException(HttpStatus.BAD_GATEWAY,
                "CONTRATO_INTERNO_INVALIDO", "El microservicio " + proveedor + " devolvió un contrato JSON inválido");
    }

    private DependenciaException interpretarNoEncontrado(String proveedor, RestClientResponseException error) {
        String codigo = leerCodigoError(error);
        if (NO_ENCONTRADOS.contains(codigo)) {
            return new DependenciaException(HttpStatus.NOT_FOUND, codigo,
                    "La consulta no encontró resultados en el servicio " + proveedor);
        }
        // En fase 2, el 404 de los servicios de fase 1 indica que la ruta interna aún no existe.
        return new DependenciaException(HttpStatus.NOT_IMPLEMENTED,
                "CONTRATO_INTERNO_PENDIENTE",
                "El servicio " + proveedor + " todavía no implementa esta consulta interna");
    }

    private DependenciaException interpretarErrorDeNegocio(RestClientResponseException error) {
        if ("NO_ES_PERSONA_NATURAL".equals(leerCodigoError(error))) {
            return new DependenciaException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "NO_ES_PERSONA_NATURAL", "El RUC consultado no corresponde a una persona natural");
        }
        return null;
    }

    private String leerCodigoError(RestClientResponseException error) {
        try {
            return json.readTree(error.getResponseBodyAsString()).path("error").path("codigo").asText("");
        } catch (Exception ignorada) {
            return "";
        }
    }
}
