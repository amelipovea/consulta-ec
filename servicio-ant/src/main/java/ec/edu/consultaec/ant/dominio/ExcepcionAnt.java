package ec.edu.consultaec.ant.dominio;

import org.springframework.http.HttpStatus;

public class ExcepcionAnt extends RuntimeException {
    private final HttpStatus estadoHttp;
    private final String codigo;
    private final boolean falloTecnico;

    public ExcepcionAnt(HttpStatus estadoHttp, String codigo, String mensaje, boolean falloTecnico) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
        this.codigo = codigo;
        this.falloTecnico = falloTecnico;
    }

    public HttpStatus estadoHttp() { return estadoHttp; }
    public String codigo() { return codigo; }
    public boolean falloTecnico() { return falloTecnico; }
}
