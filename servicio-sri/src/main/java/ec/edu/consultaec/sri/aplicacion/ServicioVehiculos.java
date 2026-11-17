package ec.edu.consultaec.sri.aplicacion;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ec.edu.consultaec.sri.infraestructura.ClienteHttpSri;
import ec.edu.consultaec.sri.infraestructura.NormalizadorSri;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ServicioVehiculos {
    private final ServicioContribuyentes contribuyentes;
    private final ClienteHttpSri cliente;
    private final NormalizadorSri normalizador;

    public ServicioVehiculos(ServicioContribuyentes contribuyentes,
                             ClienteHttpSri cliente, NormalizadorSri normalizador) {
        this.contribuyentes = contribuyentes;
        this.cliente = cliente;
        this.normalizador = normalizador;
    }

    public ObjectNode consultar(String ruc, String placa) {
        // La matrícula solamente se consulta después de validar el RUC de persona natural.
        contribuyentes.consultarPersonaNatural(ruc);
        return normalizador.normalizarVehiculos(cliente.vehiculo(placa.toUpperCase(Locale.ROOT)), placa.toUpperCase(Locale.ROOT));
    }
}
