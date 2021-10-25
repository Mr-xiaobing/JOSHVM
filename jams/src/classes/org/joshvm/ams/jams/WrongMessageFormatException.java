package org.joshvm.ams.jams;


public class WrongMessageFormatException extends Exception {

    /**
     * Constructs a new {@code WrongMessageFormatException} instance with {@code null} as
     * its detailed reason message.
     */
    public WrongMessageFormatException() {
        super();
    }

    /**
     * Constructs a new {@code WrongMessageFormatException} instance with the specified
     * detailed reason message. The error message string {@code message} can later be retrieved by
     * the {@link Throwable#getMessage() getMessage} method.
     *
     * @param message
     *            the detailed reason of the exception (may be {@code null}).
     */
    public WrongMessageFormatException(String message) {
        super(message);
    }
}

