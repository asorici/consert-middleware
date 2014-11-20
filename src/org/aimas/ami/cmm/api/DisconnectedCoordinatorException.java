package org.aimas.ami.cmm.api;

/**
 * An exception thrown when the application interfacing agent (CtxUser) of the CONSERT Middleware
 * cannot find/connect to the CONSERT Middleware internal agent which manages
 * context provisioning (CtxCoord). This can be either because the coordinator has not been reached yet, 
 * or, in case of a mobile node, because the CMM instance has been started outside any ContextDomain. 
 * @author Alex Sorici
 *
 */
public class DisconnectedCoordinatorException extends Exception {
	private static final long serialVersionUID = 1L;

	public DisconnectedCoordinatorException() {
	    super("CtxCoordinator not found.");
    }

	public DisconnectedCoordinatorException(String message, Throwable cause) {
	    super(message, cause);
    }

	public DisconnectedCoordinatorException(String message) {
	    super(message);
    }

	public DisconnectedCoordinatorException(Throwable cause) {
	    super(cause);
    }
}
