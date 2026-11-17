package ec.edu.consultaec.ant;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.consultaec.ant.api.dto.RespuestaInternaAnt;
import ec.edu.consultaec.ant.aplicacion.AlmacenUltimoDato;
import ec.edu.consultaec.ant.aplicacion.ConsultarPuntosAnt;
import ec.edu.consultaec.ant.aplicacion.ProveedorPuntosAnt;
import ec.edu.consultaec.ant.dominio.ExcepcionAnt;
import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsultarPuntosAntTests {
    private final ProveedorPuntosAnt proveedor = mock(ProveedorPuntosAnt.class);
    private final AlmacenUltimoDato cache = mock(AlmacenUltimoDato.class);
    private final ConsultarPuntosAnt servicio = new ConsultarPuntosAnt(proveedor, cache);
    private final ObjectMapper json = new ObjectMapper();
    private final String cedula = "9999999999";

    private UltimoDatoAnt dato() {
        return new UltimoDatoAnt(json.createObjectNode().put("puntos", 20), Instant.parse("2026-01-01T10:00:00Z"), "SIMULACION_ANT");
    }
    private ExcepcionAnt fallo() {
        return new ExcepcionAnt(HttpStatus.SERVICE_UNAVAILABLE, "ANT_NO_DISPONIBLE", "ANT falla", true);
    }

    @Test void respuestaNuevaSeGuardaEnCache() {
        when(proveedor.consultar(cedula)).thenReturn(dato());
        RespuestaInternaAnt resultado = servicio.consultar(cedula);
        assertEquals("SIMULACION_ANT", resultado.fuente());
        assertFalse(resultado.metadatos().desdeCache());
        assertEquals(20, resultado.datos().path("puntos").asInt());
        verify(cache).guardar(eq(cedula), any(UltimoDatoAnt.class));
    }
    @Test void falloTecnicoRecuperaUltimoDato() {
        when(proveedor.consultar(cedula)).thenThrow(fallo());
        when(cache.recuperar(cedula)).thenReturn(Optional.of(dato()));
        RespuestaInternaAnt resultado = servicio.consultar(cedula);
        assertTrue(resultado.metadatos().desdeCache());
        assertEquals("SIMULACION_ANT_CACHE", resultado.fuente());
        assertEquals(Instant.parse("2026-01-01T10:00:00Z"), resultado.metadatos().consultadoEn());
    }
    @Test void sinCacheSePropagaIndisponibilidad() {
        when(proveedor.consultar(cedula)).thenThrow(fallo());
        when(cache.recuperar(cedula)).thenReturn(Optional.empty());
        assertEquals("ANT_NO_DISPONIBLE", assertThrows(ExcepcionAnt.class,
                () -> servicio.consultar(cedula)).codigo());
    }
    @Test void errorDeNegocioNoConsultaCache() {
        when(proveedor.consultar(cedula)).thenThrow(new ExcepcionAnt(HttpStatus.NOT_FOUND,
                "PUNTOS_NO_ENCONTRADOS", "Sin datos", false));
        assertThrows(ExcepcionAnt.class, () -> servicio.consultar(cedula));
        verify(cache, never()).recuperar(any());
    }
    @Test void falloDeRedisNoInvalidaDatoReciente() {
        when(proveedor.consultar(cedula)).thenReturn(dato());
        doThrow(new IllegalStateException("Redis desconectado")).when(cache).guardar(any(), any());
        assertFalse(servicio.consultar(cedula).metadatos().desdeCache());
    }
    @Test void datoInvalidoNuncaSeGuarda() {
        when(proveedor.consultar(cedula)).thenReturn(new UltimoDatoAnt(
                json.createObjectNode().put("error", "timeout"), Instant.now(), "SIMULACION_ANT"));
        when(cache.recuperar(cedula)).thenReturn(Optional.empty());
        assertThrows(ExcepcionAnt.class, () -> servicio.consultar(cedula));
        verify(cache, never()).guardar(any(), any());
    }
}
