package ec.edu.consultaec.orquestador;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EstadoSistemaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debeInformarEstadoDeOrquestadorYAislamientoDeServicios() throws Exception {
        mockMvc.perform(get("/api/v1/sistema/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orquestador").value("DISPONIBLE"))
                .andExpect(jsonPath("$.servicios[0].nombre").value("SRI"))
                .andExpect(jsonPath("$.servicios[1].nombre").value("ANT"));
    }
}
