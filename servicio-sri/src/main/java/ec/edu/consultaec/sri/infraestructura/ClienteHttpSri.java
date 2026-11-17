package ec.edu.consultaec.sri.infraestructura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.consultaec.sri.dominio.ExcepcionSri;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Único lugar que conoce las URL externas del SRI. No escribe datos personales en logs. */
@Component
public class ClienteHttpSri {
    private static final String CATASTRO =
            "/sri-catastro-sujeto-servicio-internet/rest/ConsolidadoContribuyente/";
    private static final String VEHICULOS =
            "/sri-matriculacion-vehicular-recaudacion-servicio-internet/rest/BaseVehiculo/";

    private final RestClient http;
    private final ObjectMapper json;

    public ClienteHttpSri(
            @Value("${consultaec.sri.url-base}") String urlBase,
            @Value("${consultaec.sri.tiempo-conexion-ms:1500}") int conexionMs,
            @Value("${consultaec.sri.tiempo-lectura-ms:4500}") int lecturaMs,
            ObjectMapper json) {
        SimpleClientHttpRequestFactory peticiones = new SimpleClientHttpRequestFactory();
        peticiones.setConnectTimeout(conexionMs);
        peticiones.setReadTimeout(lecturaMs);
        this.http = RestClient.builder().baseUrl(urlBase).requestFactory(peticiones).build();
        this.json = json;
    }

    public JsonNode existeRuc(String ruc) {
        return consultar(CATASTRO + "existePorNumeroRuc", "numeroRuc", ruc,
                "RUC_NO_ENCONTRADO");
    }

    public JsonNode datosRuc(String ruc) {
        return consultar(CATASTRO + "obtenerPorNumerosRuc", "ruc", ruc,
                "RUC_NO_ENCONTRADO");
    }

    public JsonNode vehiculo(String placa) {
        return consultar(VEHICULOS + "obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn",
                "numeroPlacaCampvCpn", placa, "VEHICULO_NO_ENCONTRADO");
    }

    private JsonNode consultar(String ruta, String parametro, String valor, String codigo404) {
        try {
            String cuerpo = http.get()
                    .uri(builder -> builder.path(ruta).queryParam(parametro, valor).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            if (cuerpo == null || cuerpo.isBlank()) {
                throw formatoInvalido();
            }
            try {
                return json.readTree(cuerpo);
            } catch (JsonProcessingException error) {
                throw formatoInvalido();
            }
        } catch (RestClientResponseException error) {
            int estado = error.getStatusCode().value();
            if (estado == 404) {
                throw new ExcepcionSri(HttpStatus.NOT_FOUND, codigo404,
                        "El SRI no encontró datos para la consulta solicitada");
            }
            if (estado == 408 || estado == 429 || estado == 403 || estado >= 500) {
                throw new ExcepcionSri(HttpStatus.SERVICE_UNAVAILABLE, "SRI_NO_DISPONIBLE",
                        "El SRI no está disponible para completar la consulta");
            }
            throw new ExcepcionSri(HttpStatus.BAD_GATEWAY, "ERROR_PROVEEDOR_SRI",
                    "El SRI devolvió un error HTTP inesperado");
        } catch (ResourceAccessException error) {
            throw new ExcepcionSri(HttpStatus.SERVICE_UNAVAILABLE, "SRI_NO_DISPONIBLE",
                    "No se logró comunicar con el SRI");
        } catch (RestClientException error) {
            throw new ExcepcionSri(HttpStatus.BAD_GATEWAY, "RESPUESTA_SRI_INVALIDA",
                    "No fue posible interpretar la respuesta técnica del SRI");
        }
    }

    private ExcepcionSri formatoInvalido() {
        return new ExcepcionSri(HttpStatus.BAD_GATEWAY, "RESPUESTA_SRI_INVALIDA",
                "El SRI no devolvió una respuesta JSON válida");
    }
}
