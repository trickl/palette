package com.trickl.validation.constraints;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * <p>IntRange class.</p>
 *
 * @author tgee
 * @version $Id: $Id
 */
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = IntRangeValidator.class)
@Documented
public @interface IntRange {
    String message() default "Value not in range";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
    
    int from() default Integer.MIN_VALUE;
    
    int to() default Integer.MAX_VALUE;
    
    @Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        IntRange[] value();
    }
}
