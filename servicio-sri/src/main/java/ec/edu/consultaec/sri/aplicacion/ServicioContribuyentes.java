package ec.edu.consultaec.sri.aplicacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ec.edu.consultaec.sri.dominio.ExcepcionSri;
import ec.edu.consultaec.sri.infraestructura.ClienteHttpSri;
import ec.edu.consultaec.sri.infraestructura.NormalizadorSri;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ServicioContribuyentes {
    private final ClienteHttpSri cliente;
    private final NormalizadorSri normalizador;

    public ServicioContribuyentes(ClienteHttpSri cliente, NormalizadorSri normalizador) {
        this.cliente = cliente;
        this.normalizador = normalizador;
    }

    public ObjectNode consultarPersonaNatural(String ruc) {
        if (!normalizador.interpretarExistencia(cliente.existeRuc(ruc))) {
            throw new ExcepcionSri(HttpStatus.NOT_FOUND, "RUC_NO_ENCONTRADO",
                    "El RUC no está registrado en el SRI");
        }
        JsonNode registro = normalizador.seleccionarContribuyente(cliente.datosRuc(ruc), ruc);
        return normalizador.normalizarContribuyente(registro, ruc);
    }
}
