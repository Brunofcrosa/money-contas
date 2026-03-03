package com.moneycontas.validation;

import com.moneycontas.domain.entity.Transaction;
import com.moneycontas.dto.request.TransactionRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class RecurrenceValidator implements ConstraintValidator<ValidRecurrence, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        boolean recurrent;
        boolean hasFrequency;

        if (obj instanceof TransactionRequest request) {
            recurrent = request.isRecurrent();
            hasFrequency = request.frequency() != null;
        } else if (obj instanceof Transaction entity) {
            recurrent = Boolean.TRUE.equals(entity.getIsRecurrent());
            hasFrequency = entity.getFrequency() != null;
        } else {
            
            return true;
        }

        if (recurrent && !hasFrequency) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "frequency is required when isRecurrent is true")
                    .addPropertyNode("frequency")
                    .addConstraintViolation();
            return false;
        }

        if (!recurrent && hasFrequency) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "frequency must be null when isRecurrent is false")
                    .addPropertyNode("frequency")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
