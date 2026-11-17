package ec.edu.consultaec.orquestador;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.consultaec.orquestador.api.dto.SolicitudContribuyente;
import ec.edu.consultaec.orquestador.api.dto.SolicitudPuntos;
import ec.edu.consultaec.orquestador.api.dto.SolicitudVehiculo;
import ec.edu.consultaec.orquestador.aplicacion.ClienteServiciosInternos;
import ec.edu.consultaec.orquestador.aplicacion.CoordinadorConsultas;
import ec.edu.consultaec.orquestador.aplicacion.ResultadoInterno;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoordinadorConsultasTests {
    private ClienteServiciosInternos cliente;
    private CoordinadorConsultas coordinador;
    private final ResultadoInterno ejemplo = new ResultadoInterno("OK", "SRI",
            new ObjectMapper().createObjectNode(), Instant.parse("2026-01-01T10:00:00Z"), false);

    @BeforeEach
    void preparar() {
        cliente = mock(ClienteServiciosInternos.class);
        coordinador = new CoordinadorConsultas(cliente);
    }

    @Test
    void consultaContribuyenteSoloReenviaRuc() {
        when(cliente.consultarContribuyente("1712345678001")).thenReturn(ejemplo);
        var r = coordinador.consultarContribuyente(new SolicitudContribuyente("usuario@ejemplo.com", "1712345678001"));
        assertEquals("CONTRIBUYENTE", r.tipoConsulta());
        verify(cliente).consultarContribuyente("1712345678001");
    }

    @Test
    void consultaVehiculoNormalizaPlaca() {
        when(cliente.consultarVehiculo("1712345678001", "ABC-1234")).thenReturn(ejemplo);
        var r = coordinador.consultarVehiculo(new SolicitudVehiculo("usuario@ejemplo.com", "1712345678001", "abc-1234"));
        assertEquals("VEHICULO", r.tipoConsulta());
        verify(cliente).consultarVehiculo("1712345678001", "ABC-1234");
    }

    @Test
    void consultaPuntosNoEnviaCorreo() {
        when(cliente.consultarPuntos("1712345678")).thenReturn(ejemplo);
        var r = coordinador.consultarPuntos(new SolicitudPuntos("usuario@ejemplo.com", "1712345678"));
        assertEquals("PUNTOS_LICENCIA", r.tipoConsulta());
        verify(cliente).consultarPuntos("1712345678");
    }
}
