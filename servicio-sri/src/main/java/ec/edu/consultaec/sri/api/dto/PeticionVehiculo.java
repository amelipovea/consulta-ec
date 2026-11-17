package ec.edu.consultaec.sri.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PeticionVehiculo(
        @NotBlank(message = "El RUC es obligatorio")
        @Pattern(regexp = "[0-9]{13}", message = "El RUC debe tener 13 dígitos")
        String ruc,
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "(?i)[A-Z]{3}-?[0-9]{3,4}", message = "Formato de placa inválido")
        String placa
) { }
