package net.celestialdata.plexbot.discord.commandhandler;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A CDI qualifier that is used for internal beans that should not be injected into client code and injection points
 * where no client beans should get injected.
 */
@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Documented
@Qualifier
public @interface Internal {
    /**
     * An annotation literal for programmatic CDI lookup.
     */
    class Literal extends AnnotationLiteral<Internal> implements Internal {
        /**
         * The annotation literal instance.
         */
        public static final Literal INSTANCE = new Literal();

        /**
         * The serial version UID of this class.
         */
        private static final long serialVersionUID = 1;

        /**
         * Constructs a new internal annotation literal.
         */
        private Literal() {
        }
    }
}
