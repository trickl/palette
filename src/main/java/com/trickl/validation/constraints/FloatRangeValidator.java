package com.trickl.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.FloatValidator;

public class FloatRangeValidator implements ConstraintValidator<FloatRange, Float> {

    private float from;
    private float to;
    
    private final FloatValidator floatValidator = new FloatValidator();
    
    @Override
    public void initialize(FloatRange constraintAnnotation) {
        this.from = constraintAnnotation.from();
        this.to = constraintAnnotation.to();
    }

    @Override
    public boolean isValid(Float value, ConstraintValidatorContext constraintContext) {
        if ( value == null ) {
            return true;
        }

        return floatValidator.isInRange(value, from, to);
    }
}
