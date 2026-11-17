package ec.edu.consultaec.orquestador.aplicacion;

import org.springframework.http.HttpStatus;

public class DependenciaException extends RuntimeException {
    private final HttpStatus estadoHttp;
    private final String codigo;

    public DependenciaException(HttpStatus estadoHttp, String codigo, String mensaje) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
        this.codigo = codigo;
    }

    public HttpStatus getEstadoHttp() {
        return estadoHttp;
    }

    public String getCodigo() {
        return codigo;
    }
}
