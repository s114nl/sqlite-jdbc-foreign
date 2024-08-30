package org.sqlite.core.assertion;

import java.text.MessageFormat;

public class Asserts {

    /**
     * Assert a boolean expression, throwing an IllegalStateException if the expression evaluates to false.
     * <p/>
     * Messages are formatted using {@link MessageFormat}
     *
     * @param condition a boolean expression
     * @param message   the exception message to use if the assertion fails as {@link MessageFormat}
     * @param args      the <code>message</code> interpolation arguments
     */
    public static void state(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new IllegalArgumentException(new MessageFormat(message).format(args));
        }
    }

    /**
     * Assert that an object is not null.
     *
     * @param object  the object to check
     * @param message the exception message to use if the assertion fails as {@link MessageFormat}
     * @param args    the <code>message</code> interpolation arguments
     */
    public static void notNull(Object object, String message, Object... args) {
        if (object == null) {
            throw new NullPointerException(new MessageFormat(message).format(args));
        }
    }
}
