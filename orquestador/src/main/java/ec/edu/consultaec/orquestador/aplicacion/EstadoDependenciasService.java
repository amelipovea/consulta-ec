package ec.edu.consultaec.orquestador.aplicacion;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class EstadoDependenciasService {

    private final RestClient restClient;
    private final String sriUrl;
    private final String antUrl;

    public EstadoDependenciasService(
            @Value("${consultaec.servicios.sri.url}") String sriUrl,
            @Value("${consultaec.servicios.ant.url}") String antUrl) {
        this.sriUrl = sriUrl;
        this.antUrl = antUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1200);
        factory.setReadTimeout(1500);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public List<Map<String, String>> consultarServicios() {
        return List.of(
                comprobar("SRI", sriUrl),
                comprobar("ANT", antUrl)
        );
    }

    private Map<String, String> comprobar(String nombre, String baseUrl) {
        try {
            // Solo se comprueban endpoints internos de los procesos locales.
            restClient.get()
                    .uri(baseUrl + "/api/v1/interno/estado")
                    .retrieve()
                    .toBodilessEntity();
            return Map.of(
                    "nombre", nombre,
                    "estado", "DISPONIBLE",
                    "detalle", "Microservicio local conectado; no prueba el proveedor externo"
            );
        } catch (RestClientException error) {
            return Map.of(
                    "nombre", nombre,
                    "estado", "NO_DISPONIBLE",
                    "detalle", "No se pudo conectar al microservicio local"
            );
        }
    }
}
