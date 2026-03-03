package com.moneycontas.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RecurrenceValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRecurrence {

    String message() default "frequency is required when isRecurrent is true, and must be null when isRecurrent is false";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
