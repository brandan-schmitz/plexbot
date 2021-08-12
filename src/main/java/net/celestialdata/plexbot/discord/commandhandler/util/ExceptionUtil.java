package net.celestialdata.plexbot.discord.commandhandler.util;

import javax.enterprise.context.ApplicationScoped;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * A utility class with helper methods for handling exceptions.
 */
@ApplicationScoped
@SuppressWarnings("unused")
public class ExceptionUtil {

    /**
     * Allows to throw an unchecked exception without declaring it in a throw clause.
     * The given {@code Throwable} will be unconditionally thrown.
     * The return type is only present to be able to use the method as
     * last statement in a method or lambda that needs a value returned,
     * but the method will never return successfully.
     *
     * @param throwable the throwable to be thrown
     * @param <R>       the fake return type
     * @param <T>       the class of the throwable that will be thrown
     * @return nothing as the method will never return successfully
     * @throws T unconditionally
     */
    @SuppressWarnings("unchecked")
    public <R, T extends Throwable> R sneakyThrow(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Unwraps {@link CompletionException CompletionExceptions},
     * {@link InvocationTargetException InvocationTargetExceptions} and {@link ExecutionException ExecutionExceptions}
     * and returns the actual cause, or the given argument itself if it is not an instance of one of the listed
     * exception types.
     * If the bottom-most exception to unwrap does not have a cause, it is returned itself instead.
     *
     * @param throwable the throwable to unwrapped
     * @return the unwrapped throwable
     */
    public Throwable unwrapThrowable(Throwable throwable) {
        Throwable result = throwable;
        Throwable cause = result.getCause();
        while (((result instanceof CompletionException)
                || (result instanceof InvocationTargetException)
                || (result instanceof ExecutionException))
                && (cause != null)) {
            result = cause;
            cause = result.getCause();
        }
        return result;
    }
}
