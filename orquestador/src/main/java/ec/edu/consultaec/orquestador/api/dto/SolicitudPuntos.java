package ec.edu.consultaec.orquestador.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SolicitudPuntos(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo,
        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "[0-9]{10}", message = "La cédula debe tener exactamente 10 dígitos")
        String cedula
) { }
