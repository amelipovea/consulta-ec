package ec.edu.consultaec.orquestador;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.consultaec.orquestador.api.dto.MetadatosConsulta;
import ec.edu.consultaec.orquestador.api.dto.RespuestaConsulta;
import ec.edu.consultaec.orquestador.api.dto.SolicitudContribuyente;
import ec.edu.consultaec.orquestador.api.dto.SolicitudPuntos;
import ec.edu.consultaec.orquestador.api.dto.SolicitudVehiculo;
import ec.edu.consultaec.orquestador.aplicacion.CoordinadorConsultas;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConsultasControllerTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockitoBean CoordinadorConsultas coordinador;

    @Test
    void contribuyenteValidoLlegaAlCoordinador() throws Exception {
        SolicitudContribuyente solicitud = new SolicitudContribuyente("usuario@ejemplo.com", "1712345678001");
        RespuestaConsulta respuesta = respuesta("CONTRIBUYENTE", "SRI");
        when(coordinador.consultarContribuyente(eq(solicitud))).thenReturn(respuesta);
        mvc.perform(post("/api/v1/contribuyentes/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoConsulta").value("CONTRIBUYENTE"))
                .andExpect(jsonPath("$.fuente").value("SRI"))
                .andExpect(jsonPath("$.metadatos.desdeCache").value(false));
    }

    @Test
    void vehiculoValidoLlegaAlCoordinador() throws Exception {
        SolicitudVehiculo solicitud = new SolicitudVehiculo("usuario@ejemplo.com", "1712345678001", "ABC-1234");
        when(coordinador.consultarVehiculo(eq(solicitud))).thenReturn(respuesta("VEHICULO", "SRI"));
        mvc.perform(post("/api/v1/vehiculos/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoConsulta").value("VEHICULO"));
    }

    @Test
    void puntosValidosLleganAlCoordinador() throws Exception {
        SolicitudPuntos solicitud = new SolicitudPuntos("usuario@ejemplo.com", "1712345678");
        when(coordinador.consultarPuntos(eq(solicitud))).thenReturn(respuesta("PUNTOS_LICENCIA", "REDIS_CLOUD"));
        mvc.perform(post("/api/v1/licencias/puntos/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoConsulta").value("PUNTOS_LICENCIA"))
                .andExpect(jsonPath("$.fuente").value("REDIS_CLOUD"));
    }

    private RespuestaConsulta respuesta(String tipo, String fuente) {
        return new RespuestaConsulta(tipo, "OK", fuente, json.createObjectNode(),
                new MetadatosConsulta(Instant.parse("2026-01-01T10:00:00Z"), false));
    }
}
