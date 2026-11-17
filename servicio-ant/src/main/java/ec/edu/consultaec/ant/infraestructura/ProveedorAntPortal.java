package ec.edu.consultaec.ant.infraestructura;

import ec.edu.consultaec.ant.aplicacion.ProveedorPuntosAnt;
import ec.edu.consultaec.ant.dominio.ExcepcionAnt;
import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "consultaec.ant.modo", havingValue = "REAL", matchIfMissing = true)
public class ProveedorAntPortal implements ProveedorPuntosAnt {
    private static final String PORTAL = "https://consultaweb.ant.gob.ec/PortalWEB/paginas/clientes/"
            + "clp_grid_citaciones.jsp?ps_tipo_identificacion=CED&ps_identificacion=";
    private final HttpClient http;
    private final Duration timeout;

    public ProveedorAntPortal(@Value("${consultaec.ant.timeout-ms:1200}") int timeoutMs) {
        this.timeout = Duration.ofMillis(Math.max(500, Math.min(timeoutMs, 10000)));
        this.http = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    @Override
    public UltimoDatoAnt consultar(String cedula) {
        // El recurso proporcionado es una página JSP de citaciones, NO una API de puntos
        // documentada. No interpretar HTML ni afirmar puntos sin un contrato verificado.
        try {
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(PORTAL + cedula + "&ps_placa="))
                    .timeout(timeout)
                    .GET()
                    .build();
            HttpResponse<String> respuesta = http.send(solicitud, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new ExcepcionAnt(HttpStatus.SERVICE_UNAVAILABLE, "ANT_NO_DISPONIBLE",
                        "El portal ANT no respondió correctamente", true);
            }
            throw new ExcepcionAnt(HttpStatus.SERVICE_UNAVAILABLE, "ANT_SIN_CONTRATO_PUNTOS",
                    "El portal consultado no ofrece un contrato verificable para puntos", true);
        } catch (ExcepcionAnt error) {
            throw error;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new ExcepcionAnt(HttpStatus.SERVICE_UNAVAILABLE, "ANT_NO_DISPONIBLE",
                    "Se interrumpió la consulta a ANT", true);
        } catch (Exception error) {
            throw new ExcepcionAnt(HttpStatus.SERVICE_UNAVAILABLE, "ANT_NO_DISPONIBLE",
                    "No fue posible consultar ANT", true);
        }
    }
}
