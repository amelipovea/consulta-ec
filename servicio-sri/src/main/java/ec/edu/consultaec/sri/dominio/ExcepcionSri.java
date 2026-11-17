package ec.edu.consultaec.sri.dominio;

import org.springframework.http.HttpStatus;

public class ExcepcionSri extends RuntimeException {
    private final HttpStatus estadoHttp;
    private final String codigo;

    public ExcepcionSri(HttpStatus estadoHttp, String codigo, String mensaje) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
        this.codigo = codigo;
    }

    public HttpStatus getEstadoHttp() { return estadoHttp; }
    public String getCodigo() { return codigo; }
}
