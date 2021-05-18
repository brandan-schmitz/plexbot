package net.celestialdata.plexbot.discord.commandhandler.api.parameter;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation which serves as container for applying multiple {@link ParameterType @ParameterType} annotations.
 * This container annotation is used implicitly and should usually not be applied manually.
 * Just use multiple {@code @ParameterType} annotations on the same class instead.
 *
 * @see ParameterType @ParameterType
 */
@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Documented
public @interface ParameterTypes {
    /**
     * Returns the parameter types for the annotated parameter converter.
     *
     * @return the parameter types for the annotated parameter converter
     */
    @SuppressWarnings("unused") ParameterType[] value();
}
