package ec.edu.consultaec.sri;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.consultaec.sri.dominio.ExcepcionSri;
import ec.edu.consultaec.sri.infraestructura.NormalizadorSri;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizadorSriTests {
    private final ObjectMapper json = new ObjectMapper();
    private final NormalizadorSri normalizador = new NormalizadorSri(json);
    private static final String RUC = "1712345678001"; // Sintético; no consultar al SRI real

    @Test
    void respuestaBooleanaSeInterpretaCorrectamente() throws Exception {
        assertTrue(normalizador.interpretarExistencia(json.readTree("true")));
        assertFalse(normalizador.interpretarExistencia(json.readTree("false")));
        assertEquals("RESPUESTA_SRI_INVALIDA", assertThrows(ExcepcionSri.class,
                () -> normalizador.interpretarExistencia(json.readTree("{}"))).getCodigo());
    }

    @Test
    void personaNaturalSeNormalizaSinInventarCampos() throws Exception {
        var respuesta = json.readTree("""
                [{"numeroRuc":"1712345678001", "razonSocial":"NOMBRE SINTETICO",
                  "tipoContribuyente":"PERSONA NATURAL", "campoDesconocido":"omitido"}]
                """);
        var registro = normalizador.seleccionarContribuyente(respuesta, RUC);
        var datos = normalizador.normalizarContribuyente(registro, RUC);
        assertEquals("NOMBRE SINTETICO", datos.path("razonSocial").asText());
        assertEquals(RUC, datos.path("ruc").asText());
        assertFalse(datos.has("campoDesconocido"));
        assertFalse(datos.has("estadoContribuyente"));
    }

    @Test
    void sociedadSeRechaza() throws Exception {
        var registro = json.readTree("""
                {"numeroRuc":"1712345678001", "tipoContribuyente":"SOCIEDAD"}
                """);
        var error = assertThrows(ExcepcionSri.class,
                () -> normalizador.normalizarContribuyente(registro, RUC));
        assertEquals(422, error.getEstadoHttp().value());
        assertEquals("NO_ES_PERSONA_NATURAL", error.getCodigo());
    }

    @Test
    void noConfundeUnRucDiferenteNiUnTipoAusente() throws Exception {
        var diferente = json.readTree("""
                [{"numeroRuc":"1790000000001", "tipoContribuyente":"PERSONA NATURAL"}]
                """);
        assertEquals(502, assertThrows(ExcepcionSri.class,
                () -> normalizador.seleccionarContribuyente(diferente, RUC)).getEstadoHttp().value());
        var sinTipo = json.readTree("""
                {"numeroRuc":"1712345678001"}
                """);
        assertEquals(502, assertThrows(ExcepcionSri.class,
                () -> normalizador.normalizarContribuyente(sinTipo, RUC)).getEstadoHttp().value());
    }

    @Test
    void vehiculoConDataSeNormaliza() throws Exception {
        var respuesta = json.readTree("""
                {"data":[{"numeroPlaca":"ABC1234", "marca":"MARCA DE PRUEBA", "modelo":"MODELO X"}]}
                """);
        var datos = normalizador.normalizarVehiculos(respuesta, "ABC-1234");
        assertEquals("MARCA DE PRUEBA", datos.path("vehiculos").get(0).path("marca").asText());
        assertEquals("ABC-1234", datos.path("placaConsultada").asText());
        assertEquals(404, assertThrows(ExcepcionSri.class,
                () -> normalizador.normalizarVehiculos(json.readTree("""
                        {"data":[]}
                        """), "ABC1234"))
                .getEstadoHttp().value());
    }
}
