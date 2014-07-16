package org.aimas.ami.cmm.exceptions;

public class AgentConfigException extends Exception {
    private static final long serialVersionUID = -8062745006350848664L;

	public AgentConfigException() {
		this("Unknown error in agent configuration.");
	}
	
	public AgentConfigException(String message) {
		super(message);
	}
	
	public AgentConfigException(Throwable cause) {
		super(cause);
	}
	
	public AgentConfigException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public AgentConfigException(String message, Throwable cause,
	        boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
