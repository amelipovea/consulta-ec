package ec.edu.consultaec.orquestador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ec.edu.consultaec.orquestador.aplicacion.ClienteServiciosInternos;
import ec.edu.consultaec.orquestador.aplicacion.DependenciaException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteServiciosInternosTests {
    private HttpServer servidor;
    private ClienteServiciosInternos cliente;
    private int estado;
    private String respuesta;

    @BeforeEach
    void preparar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", this::responder);
        estado = 200;
        respuesta = """
                {"estado":"OK","fuente":"SRI","datos":{"comprobacion":true},
                 "metadatos":{"consultadoEn":"2026-11-17T12:00:00Z","desdeCache":false}}
                """;
        servidor.start();
        String url = "http://127.0.0.1:" + servidor.getAddress().getPort();
        cliente = new ClienteServiciosInternos(url, url, 1200, 1500, new ObjectMapper());
    }

    @AfterEach
    void cerrar() {
        servidor.stop(0);
    }

    @Test
    void respuestaValidaSeConvierteEnContrato() {
        var resultado = cliente.consultarContribuyente("1712345678001");
        assertEquals("SRI", resultado.fuente());
        assertTrue(resultado.datos().path("comprobacion").asBoolean());
    }

    @Test
    void rutaInternaAusenteSeInformaComoPendiente() {
        estado = 404;
        respuesta = "{\"timestamp\":\"2026-11-17\",\"status\":404}";
        var error = assertThrows(DependenciaException.class, () -> cliente.consultarContribuyente("1712345678001"));
        assertEquals(501, error.getEstadoHttp().value());
        assertEquals("CONTRATO_INTERNO_PENDIENTE", error.getCodigo());
    }

    @Test
    void ausenciaDeDatosConErrorDocumentadoSePropagaComo404() {
        estado = 404;
        respuesta = "{\"error\":{\"codigo\":\"RUC_NO_ENCONTRADO\",\"mensaje\":\"No existe\"}}";
        var error = assertThrows(DependenciaException.class, () -> cliente.consultarContribuyente("1712345678001"));
        assertEquals(404, error.getEstadoHttp().value());
        assertEquals("RUC_NO_ENCONTRADO", error.getCodigo());
    }

    @Test
    void noPersonaNaturalSePropagaComo422() {
        estado = 422;
        respuesta = "{\"error\":{\"codigo\":\"NO_ES_PERSONA_NATURAL\"}}";
        var error = assertThrows(DependenciaException.class, () -> cliente.consultarVehiculo("1712345678001", "ABC-1234"));
        assertEquals(422, error.getEstadoHttp().value());
    }

    @Test
    void contratoInvalidoSeRechaza() {
        respuesta = "{\"datos\":{\"puntos\":25}}";
        var error = assertThrows(DependenciaException.class, () -> cliente.consultarPuntos("1712345678"));
        assertEquals(502, error.getEstadoHttp().value());
        assertEquals("CONTRATO_INTERNO_INVALIDO", error.getCodigo());
    }

    @Test
    void errorRemoto500SeTraduceA502() {
        estado = 500;
        respuesta = "{\"error\":\"error interno\"}";
        var error = assertThrows(DependenciaException.class, () -> cliente.consultarPuntos("1712345678"));
        assertEquals(502, error.getEstadoHttp().value());
    }

    private void responder(HttpExchange intercambio) throws IOException {
        byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        intercambio.sendResponseHeaders(estado, bytes.length);
        try (var salida = intercambio.getResponseBody()) {
            salida.write(bytes);
        }
    }
}
