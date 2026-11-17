package ec.edu.consultaec.sri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ec.edu.consultaec.sri.aplicacion.ServicioContribuyentes;
import ec.edu.consultaec.sri.aplicacion.ServicioVehiculos;
import ec.edu.consultaec.sri.dominio.ExcepcionSri;
import ec.edu.consultaec.sri.infraestructura.ClienteHttpSri;
import ec.edu.consultaec.sri.infraestructura.NormalizadorSri;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas solo contra HTTP local; nunca consulta identidades reales. */
class ServicioSriIntegracionSimuladaTests {
    private static final String RUC = "1712345678001";
    private HttpServer servidor;
    private ServicioContribuyentes contribuyentes;
    private ServicioVehiculos vehiculos;
    private String tipoContribuyente;
    private boolean existe;
    private String respuestaVehicular;
    private final AtomicInteger llamadasVehiculos = new AtomicInteger();

    @BeforeEach
    void preparar() throws IOException {
        tipoContribuyente = "PERSONA NATURAL";
        existe = true;
        respuestaVehicular = """
                {"data":[{"numeroPlaca":"ABC-1234", "marca":"MARCA SINTETICA"}]}
                """;
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", this::responder);
        servidor.start();
        ObjectMapper json = new ObjectMapper();
        String url = "http://127.0.0.1:" + servidor.getAddress().getPort();
        var cliente = new ClienteHttpSri(url, 1000, 1500, json);
        var normalizador = new NormalizadorSri(json);
        contribuyentes = new ServicioContribuyentes(cliente, normalizador);
        vehiculos = new ServicioVehiculos(contribuyentes, cliente, normalizador);
    }

    @AfterEach
    void cerrar() { servidor.stop(0); }

    @Test
    void obtienePersonaNaturalDesdeProveedorLocal() {
        var datos = contribuyentes.consultarPersonaNatural(RUC);
        assertEquals(RUC, datos.path("ruc").asText());
        assertEquals("PERSONA NATURAL", datos.path("tipoContribuyente").asText());
    }

    @Test
    void noConsultaDetallesSiElRucNoExiste() {
        existe = false;
        var error = assertThrows(ExcepcionSri.class, () -> contribuyentes.consultarPersonaNatural(RUC));
        assertEquals("RUC_NO_ENCONTRADO", error.getCodigo());
    }

    @Test
    void nuncaConsultaVehiculoSiEsSociedad() {
        tipoContribuyente = "SOCIEDAD";
        var error = assertThrows(ExcepcionSri.class, () -> vehiculos.consultar(RUC, "ABC-1234"));
        assertEquals("NO_ES_PERSONA_NATURAL", error.getCodigo());
        assertEquals(0, llamadasVehiculos.get());
    }

    @Test
    void consultaVehiculoSoloDespuesDeValidarContribuyente() {
        var datos = vehiculos.consultar(RUC, "ABC-1234");
        assertEquals(1, llamadasVehiculos.get());
        assertEquals("MARCA SINTETICA", datos.path("vehiculos").get(0).path("marca").asText());
    }

    @Test
    void vehiculoVacioNoSePresentaComoResultadoReal() {
        respuestaVehicular = """
                {"data":[]}
                """;
        var error = assertThrows(ExcepcionSri.class, () -> vehiculos.consultar(RUC, "ABC-1234"));
        assertEquals("VEHICULO_NO_ENCONTRADO", error.getCodigo());
    }

    private void responder(HttpExchange intercambio) throws IOException {
        String path = intercambio.getRequestURI().getPath();
        String cuerpo;
        if (path.endsWith("existePorNumeroRuc")) cuerpo = Boolean.toString(existe);
        else if (path.endsWith("obtenerPorNumerosRuc")) {
            cuerpo = "[{\"numeroRuc\":\"" + RUC + "\",\"tipoContribuyente\":\""
                    + tipoContribuyente + "\",\"razonSocial\":\"NOMBRE SINTETICO\"}]";
        } else if (path.endsWith("obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn")) {
            llamadasVehiculos.incrementAndGet();
            cuerpo = respuestaVehicular;
        } else {
            intercambio.sendResponseHeaders(404, -1);
            intercambio.close();
            return;
        }
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        intercambio.sendResponseHeaders(200, bytes.length);
        try (var salida = intercambio.getResponseBody()) { salida.write(bytes); }
    }
}
