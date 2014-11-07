package org.aimas.ami.cmm.api;

public class CMMConfigException extends Exception {
    private static final long serialVersionUID = 7675767726672345495L;
    
	public CMMConfigException() {
		super("Error in CMM agent configuration.");
	}
	
	public CMMConfigException(String message) {
		super(message);
	}
	
	public CMMConfigException(Throwable cause) {
		super(cause);
	}
	
	public CMMConfigException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CMMConfigException(String message, Throwable cause,
	        boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
