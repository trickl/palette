package com.trickl.validation.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.IntegerValidator;

/**
 * <p>IntRangeValidator class.</p>
 *
 * @author tgee
 * @version $Id: $Id
 */
public class IntRangeValidator implements ConstraintValidator<IntRange, Integer> {

    private float from;
    private float to;
    
    private final IntegerValidator intValidator = new IntegerValidator();
    
    /** {@inheritDoc} */
    @Override
    public void initialize(IntRange constraintAnnotation) {
        this.from = constraintAnnotation.from();
        this.to = constraintAnnotation.to();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintContext) {
        if ( value == null ) {
            return true;
        }

        return intValidator.isInRange(value, from, to);
    }
}
