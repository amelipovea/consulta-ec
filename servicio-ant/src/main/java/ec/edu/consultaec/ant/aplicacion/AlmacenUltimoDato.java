package ec.edu.consultaec.ant.aplicacion;

import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;
import java.util.Optional;

public interface AlmacenUltimoDato {
    void guardar(String cedula, UltimoDatoAnt dato);
    Optional<UltimoDatoAnt> recuperar(String cedula);
}
