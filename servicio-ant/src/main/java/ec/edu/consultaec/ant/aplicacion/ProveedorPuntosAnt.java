package ec.edu.consultaec.ant.aplicacion;

import ec.edu.consultaec.ant.dominio.UltimoDatoAnt;

public interface ProveedorPuntosAnt {
    UltimoDatoAnt consultar(String cedula);
}
