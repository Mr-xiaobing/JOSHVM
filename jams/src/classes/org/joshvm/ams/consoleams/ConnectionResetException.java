package org.joshvm.ams.consoleams;


public class ConnectionResetException extends Exception {

    /**
     * Constructs a new {@code ConnectionResetException} instance with {@code null} as
     * its detailed reason message.
     */
    public ConnectionResetException() {
        super();
    }

    /**
     * Constructs a new {@code ConnectionResetException} instance with the specified
     * detailed reason message. The error message string {@code message} can later be retrieved by
     * the {@link Throwable#getMessage() getMessage} method.
     *
     * @param message
     *            the detailed reason of the exception (may be {@code null}).
     */
    public ConnectionResetException(String message) {
        super(message);
    }
}

