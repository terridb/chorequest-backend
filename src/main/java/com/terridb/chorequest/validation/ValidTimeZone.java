package com.terridb.chorequest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Only IANA region IDs like "Europe/Amsterdam" are accepted; fixed offsets like "+02:00" would ignore daylight saving time
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTimeZoneValidator.class)
public @interface ValidTimeZone {
    String message() default "Time zone must be a valid IANA time zone, e.g. Europe/Amsterdam";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
