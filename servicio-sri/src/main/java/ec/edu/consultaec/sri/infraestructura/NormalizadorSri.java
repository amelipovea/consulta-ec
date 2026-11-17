package ec.edu.consultaec.sri.infraestructura;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ec.edu.consultaec.sri.dominio.ExcepcionSri;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Evita exponer la respuesta íntegra del proveedor; solo devuelve los campos previstos. */
@Component
public class NormalizadorSri {
    private final ObjectMapper mapper;

    public NormalizadorSri(ObjectMapper mapper) { this.mapper = mapper; }

    public boolean interpretarExistencia(JsonNode respuesta) {
        if (respuesta != null && respuesta.isBoolean()) return respuesta.booleanValue();
        if (respuesta != null && respuesta.isTextual()) {
            String valor = respuesta.textValue().strip().toLowerCase(Locale.ROOT);
            if ("true".equals(valor)) return true;
            if ("false".equals(valor)) return false;
        }
        throw respuestaInvalida("No se pudo interpretar la verificación de existencia del RUC");
    }

    public JsonNode seleccionarContribuyente(JsonNode respuesta, String ruc) {
        if (respuesta == null || respuesta.isNull()) throw rucNoEncontrado();
        if (respuesta.isObject()) return verificarRuc(respuesta, ruc);
        if (respuesta.isArray()) {
            if (respuesta.isEmpty()) throw rucNoEncontrado();
            for (JsonNode elemento : respuesta) {
                if (elemento.isObject() && ruc.equals(obtenerTexto(elemento, "numeroRuc", "ruc"))) {
                    return elemento;
                }
            }
            throw respuestaInvalida("Los datos del SRI no corresponden al RUC consultado");
        }
        throw respuestaInvalida("La respuesta de contribuyente tiene un formato no esperado");
    }

    private JsonNode verificarRuc(JsonNode respuesta, String ruc) {
        if (!ruc.equals(obtenerTexto(respuesta, "numeroRuc", "ruc"))) {
            throw respuestaInvalida("Los datos del SRI no corresponden al RUC consultado");
        }
        return respuesta;
    }

    public ObjectNode normalizarContribuyente(JsonNode registro, String ruc) {
        String tipo = obtenerTexto(registro, "tipoContribuyente");
        if (tipo == null || tipo.isBlank()) {
            throw respuestaInvalida("El SRI no informó el tipo de contribuyente");
        }
        String tipoComparable = Normalizer.normalize(tipo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT);
        if (!tipoComparable.equals("PERSONA NATURAL") && !tipoComparable.equals("NATURAL")) {
            throw new ExcepcionSri(HttpStatus.UNPROCESSABLE_ENTITY, "NO_ES_PERSONA_NATURAL",
                    "El RUC consultado no corresponde a una persona natural");
        }
        ObjectNode normalizado = mapper.createObjectNode();
        normalizado.put("ruc", ruc);
        copiar(registro, normalizado, "razonSocial", "nombreComercial", "estadoContribuyente",
                "tipoContribuyente", "claseContribuyente", "actividadEconomicaPrincipal");
        return normalizado;
    }

    public ObjectNode normalizarVehiculos(JsonNode respuesta, String placa) {
        if (respuesta == null || respuesta.isNull()) throw vehiculoNoEncontrado();
        JsonNode lista = respuesta;
        if (respuesta.isObject() && respuesta.has("data")) lista = respuesta.get("data");
        if (lista == null || lista.isNull()) throw vehiculoNoEncontrado();
        ArrayNode resultado = mapper.createArrayNode();
        if (lista.isArray()) {
            if (lista.isEmpty()) throw vehiculoNoEncontrado();
            for (JsonNode elemento : lista) agregarVehiculo(resultado, elemento, placa);
        } else if (lista.isObject()) {
            agregarVehiculo(resultado, lista, placa);
        } else {
            throw respuestaInvalida("La respuesta vehicular del SRI tiene un formato no esperado");
        }
        if (resultado.isEmpty()) throw vehiculoNoEncontrado();
        ObjectNode datos = mapper.createObjectNode();
        datos.put("placaConsultada", placa);
        datos.set("vehiculos", resultado);
        return datos;
    }

    private void agregarVehiculo(ArrayNode resultado, JsonNode original, String placa) {
        if (!original.isObject()) throw respuestaInvalida("Vehículo con formato JSON no válido");
        String placaProveedor = obtenerTexto(original, "numeroPlaca", "placa");
        if (placaProveedor != null && !normalizarPlaca(placaProveedor).equals(normalizarPlaca(placa))) {
            throw respuestaInvalida("La respuesta vehicular no coincide con la placa consultada");
        }
        ObjectNode vehiculo = mapper.createObjectNode();
        copiar(original, vehiculo, "numeroPlaca", "placa", "marca", "modelo", "anioModelo",
                "anio", "año", "clase", "tipo", "color", "servicio", "cilindraje");
        if (vehiculo.isEmpty()) throw respuestaInvalida("No hay campos vehiculares reconocidos en el JSON");
        resultado.add(vehiculo);
    }

    private String normalizarPlaca(String placa) {
        return placa.replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
    }

    private void copiar(JsonNode entrada, ObjectNode salida, String... campos) {
        for (String campo : campos) {
            JsonNode valor = entrada.get(campo);
            if (valor != null && !valor.isNull() && valor.isValueNode()) {
                salida.set(campo, valor.deepCopy());
            }
        }
    }

    private String obtenerTexto(JsonNode entrada, String... campos) {
        for (String campo : campos) {
            JsonNode valor = entrada.path(campo);
            if (valor.isTextual() && !valor.asText().isBlank()) return valor.asText();
        }
        return null;
    }

    private ExcepcionSri rucNoEncontrado() {
        return new ExcepcionSri(HttpStatus.NOT_FOUND, "RUC_NO_ENCONTRADO",
                "El SRI no encontró información para el RUC consultado");
    }

    private ExcepcionSri vehiculoNoEncontrado() {
        return new ExcepcionSri(HttpStatus.NOT_FOUND, "VEHICULO_NO_ENCONTRADO",
                "El SRI no encontró vehículos para la placa consultada");
    }

    private ExcepcionSri respuestaInvalida(String mensaje) {
        return new ExcepcionSri(HttpStatus.BAD_GATEWAY, "RESPUESTA_SRI_INVALIDA", mensaje);
    }
}
