package com.pulsepass.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

// PRD §9.3: valida el máximo de la contraseña en BYTES UTF-8 (no en caracteres).
// Un valor null o vacío se considera válido aquí: la obligatoriedad la cubre @NotBlank.
public class PasswordBytesValidator implements ConstraintValidator<PasswordBytes, CharSequence> {

    private int max;

    @Override
    public void initialize(PasswordBytes constraint) {
        this.max = constraint.max();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.toString().getBytes(StandardCharsets.UTF_8).length <= max;
    }
}
