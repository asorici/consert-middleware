package org.aimas.ami.cmm.agent.orgmgr;


public class CMMInitResult {
	private Exception error;
	private ManagedAgentWrapper<?> faultyAgent;
	
	
    public CMMInitResult(Exception error, ManagedAgentWrapper<?> faultyAgent) {
	    this.error = error;
	    this.faultyAgent = faultyAgent;
    }
    
    public CMMInitResult() {
    	this(null, null);
    }
    
    public CMMInitResult(Exception error) {
    	this(error, null);
    }
    
    public CMMInitResult(ManagedAgentWrapper<?> faultyAgent) {
	    this(null, faultyAgent);
    }
    
    
    
	public Exception getError() {
		return error;
	}


	public ManagedAgentWrapper<?> getFaultyAgent() {
		return faultyAgent;
	}
	
	
	public boolean hasError() {
		return error != null || faultyAgent != null;
	}
	
	
	public boolean hasFaultyAgent() {
		return faultyAgent != null;
	}
}
