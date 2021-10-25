package org.joshvm.ams.jams;


public class InstallVerifyErrorException extends Exception {

    /**
     * Constructs a new {@code InstallVerifyErrorException} instance with {@code null} as
     * its detailed reason message.
     */
    public InstallVerifyErrorException() {
        super();
    }

    /**
     * Constructs a new {@code InstallVerifyErrorException} instance with the specified
     * detailed reason message. The error message string {@code message} can later be retrieved by
     * the {@link Throwable#getMessage() getMessage} method.
     *
     * @param message
     *            the detailed reason of the exception (may be {@code null}).
     */
    public InstallVerifyErrorException(String message) {
        super(message);
    }
}

