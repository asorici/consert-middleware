package org.aimas.ami.cmm.agent.orgmgr;


public class CMMOpEventResult {
	private Exception error;
	private Object result;
	
    public CMMOpEventResult(Exception error, Object result) {
	    this.error = error;
	    this.result = result;
    }
    
    public CMMOpEventResult() {
    	this(null, null);
    }
    
    public CMMOpEventResult(Exception error) {
    	this(error, null);
    }
    
    public CMMOpEventResult(Object result) {
	    this(null, result);
    }
    
    
	public Exception getError() {
		return error;
	}


	public Object getResult() {
		return result;
	}
	
	
	public boolean hasError() {
		return error != null;
	}
	
	
	public boolean hasResult() {
		return result != null;
	}
}
