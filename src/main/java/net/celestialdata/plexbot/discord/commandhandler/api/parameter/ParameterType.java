package net.celestialdata.plexbot.discord.commandhandler.api.parameter;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A CDI qualifier for {@link ParameterConverter}s that defines the parameter type aliases for which the annotated
 * parameter converter works.
 */
@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Documented
@Repeatable(ParameterTypes.class)
@Qualifier
public @interface ParameterType {
    /**
     * Returns the parameter type alias for the annotated converter.
     *
     * @return the parameter type alias for the annotated converter
     */
    @SuppressWarnings("unused") String value();

    /**
     * An annotation literal for programmatic CDI lookup.
     */
    class Literal extends AnnotationLiteral<ParameterType> implements ParameterType {
        /**
         * The serial version UID of this class.
         */
        private static final long serialVersionUID = 1;

        /**
         * The parameter type alias for the annotated converter.
         */
        private final String alias;

        /**
         * Constructs a new parameter type annotation literal.
         *
         * @param alias the parameter type alias for the annotated converter
         */
        @SuppressWarnings({"unused", "CdiInjectionPointsInspection"})
        public Literal(String alias) {
            this.alias = alias;
        }

        @SuppressWarnings("unused")
        @Override
        public String value() {
            return alias;
        }
    }
}
