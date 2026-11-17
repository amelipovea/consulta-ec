package ec.edu.consultaec.ant.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PeticionPuntos(
        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "[0-9]{10}", message = "La cédula debe tener exactamente 10 dígitos")
        String cedula) { }
