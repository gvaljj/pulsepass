package com.pulsepass.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// PRD §9.3: el máximo de contraseña se expresa en BYTES (72, límite que procesa BCrypt),
// no en caracteres. Con contraseñas no ASCII, 72 caracteres pueden ocupar más de 72 bytes y
// BCrypt las truncaría; por eso la longitud se valida aquí sobre la codificación UTF-8.
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordBytesValidator.class)
public @interface PasswordBytes {

    String message() default "La contraseña no debe superar 72 bytes (límite de BCrypt)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // Límite de bytes UTF-8 (72 por defecto, máximo que BCrypt procesa sin truncar).
    int max() default 72;
}
