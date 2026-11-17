package ec.edu.consultaec.orquestador;

import ec.edu.consultaec.orquestador.aplicacion.CoordinadorConsultas;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidacionesConsultasTests {
    @Autowired MockMvc mvc;
    @MockitoBean CoordinadorConsultas coordinador;

    @Test
    void rucCortoDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/contribuyentes/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"usuario@ejemplo.com\",\"ruc\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("VALIDACION_ERROR"))
                .andExpect(jsonPath("$.error.detalles[0].campo").value("ruc"));
        verifyNoInteractions(coordinador);
    }

    @Test
    void correoInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/contribuyentes/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"no-es-correo\",\"ruc\":\"1712345678001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.detalles[0].campo").value("correo"));
        verifyNoInteractions(coordinador);
    }

    @Test
    void placaInvalidaDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/vehiculos/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"usuario@ejemplo.com\",\"ruc\":\"1712345678001\",\"placa\":\"1?\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.detalles[0].campo").value("placa"));
        verifyNoInteractions(coordinador);
    }

    @Test
    void cedulaInvalidaDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/licencias/puntos/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"usuario@ejemplo.com\",\"cedula\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.detalles[0].campo").value("cedula"));
        verifyNoInteractions(coordinador);
    }

    @Test
    void sinCuerpoDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/contribuyentes/consultas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("CUERPO_JSON_INVALIDO"));
    }

    @Test
    void jsonMalFormadoDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/contribuyentes/consultas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{json inválido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("CUERPO_JSON_INVALIDO"));
    }

    @Test
    void tipoContenidoIncorrectoDevuelve415() throws Exception {
        mvc.perform(post("/api/v1/contribuyentes/consultas")
                .contentType(MediaType.TEXT_PLAIN)
                .content("texto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.codigo").value("TIPO_CONTENIDO_NO_SOPORTADO"));
    }
}
