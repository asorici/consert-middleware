package org.aimas.ami.cmm.api;

/**
 * An exception thrown when the application interfacing agent (CtxUser) of the CONSERT Middleware
 * cannot find/connect to the CONSERT Middleware internal agent which manages
 * query execution (CtxQueryHandler). 
 * @author Alex Sorici
 *
 */
public class DisconnectedQueryHandlerException extends Exception {
    private static final long serialVersionUID = 1L;

	public DisconnectedQueryHandlerException() {
	    super("CtxQueryHandler not found.");
    }

	public DisconnectedQueryHandlerException(String message, Throwable cause) {
	    super(message, cause);
    }

	public DisconnectedQueryHandlerException(String message) {
	    super(message);
    }

	public DisconnectedQueryHandlerException(Throwable cause) {
	    super(cause);
    }
	
}
