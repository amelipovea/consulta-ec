package ec.edu.consultaec.orquestador.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SolicitudVehiculo(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String correo,
        @NotBlank(message = "El RUC es obligatorio")
        @Pattern(regexp = "[0-9]{13}", message = "El RUC debe tener exactamente 13 dígitos")
        String ruc,
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "(?i)[A-Z]{3}-?[0-9]{3,4}",
                 message = "La placa debe tener 3 letras y 3 o 4 números, con guion opcional")
        String placa
) { }
