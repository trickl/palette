package com.trickl.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.FloatValidator;

/**
 * <p>FloatRangeValidator class.</p>
 *
 * @author tgee
 * @version $Id: $Id
 */
public class FloatRangeValidator implements ConstraintValidator<FloatRange, Float> {

    private float from;
    private float to;
    
    private final FloatValidator floatValidator = new FloatValidator();
    
    /** {@inheritDoc} */
    @Override
    public void initialize(FloatRange constraintAnnotation) {
        this.from = constraintAnnotation.from();
        this.to = constraintAnnotation.to();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValid(Float value, ConstraintValidatorContext constraintContext) {
        if ( value == null ) {
            return true;
        }

        return floatValidator.isInRange(value, from, to);
    }
}
