package ec.edu.consultaec.sri.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PeticionRuc(
        @NotBlank(message = "El RUC es obligatorio")
        @Pattern(regexp = "[0-9]{13}", message = "El RUC debe tener 13 dígitos")
        String ruc
) { }
