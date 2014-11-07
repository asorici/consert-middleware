package org.aimas.ami.cmm.agent.orgmgr;


public class CMMEventResult {
	private Exception error;
	private Object result;
	
    public CMMEventResult(Exception error, Object result) {
	    this.error = error;
	    this.result = result;
    }
    
    public CMMEventResult() {
    	this(null, null);
    }
    
    public CMMEventResult(Exception error) {
    	this(error, null);
    }
    
    public CMMEventResult(Object result) {
	    this(null, result);
    }
    
    
    
	public Exception getError() {
		return error;
	}


	public Object getResult() {
		return result;
	}
	
	
	public boolean hasError() {
		return error != null || result != null;
	}
	
	
	public boolean hasResult() {
		return result != null;
	}
}
