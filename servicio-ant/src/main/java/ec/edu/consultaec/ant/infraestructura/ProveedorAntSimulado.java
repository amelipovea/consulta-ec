package ec.edu.consultaec.ant.infraestructura;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.consultaec.ant.aplicacion.ProveedorPuntosAnt;
import ec.edu.consultaec.ant.dominio.ExcepcionAnt;
import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "consultaec.ant.modo", havingValue = "SIMULADO")
public class ProveedorAntSimulado implements ProveedorPuntosAnt {
    private final ObjectMapper json;
    private final boolean falla;
    private final int puntos;

    public ProveedorAntSimulado(ObjectMapper json,
            @Value("${consultaec.ant.simulacion-falla:false}") boolean falla,
            @Value("${consultaec.ant.simulacion-puntos:20}") int puntos) {
        this.json = json;
        this.falla = falla;
        this.puntos = puntos;
    }

    @Override
    public UltimoDatoAnt consultar(String cedula) {
        if (falla) throw new ExcepcionAnt(HttpStatus.SERVICE_UNAVAILABLE, "ANT_NO_DISPONIBLE",
                "Fallo simulado del proveedor ANT", true);
        if (puntos < 0) throw new ExcepcionAnt(HttpStatus.BAD_GATEWAY, "RESPUESTA_ANT_INVALIDA",
                "El simulador produjo un resultado incorrecto", true);
        return new UltimoDatoAnt(json.createObjectNode().put("puntos", puntos), Instant.now(), "SIMULACION_ANT");
    }
}
