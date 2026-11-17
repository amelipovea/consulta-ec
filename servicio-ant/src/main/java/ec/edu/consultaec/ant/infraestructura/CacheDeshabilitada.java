package ec.edu.consultaec.ant.infraestructura;

import ec.edu.consultaec.ant.aplicacion.AlmacenUltimoDato;
import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "consultaec.redis.habilitado", havingValue = "false", matchIfMissing = true)
public class CacheDeshabilitada implements AlmacenUltimoDato {
    @Override public void guardar(String cedula, UltimoDatoAnt dato) { }
    @Override public Optional<UltimoDatoAnt> recuperar(String cedula) { return Optional.empty(); }
}
