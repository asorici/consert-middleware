package org.aimas.ami.cmm;

import org.aimas.ami.cmm.api.CMMPlatformManagementService.CMMInstanceState;

public class CMMInstanceStateWrapper {
	
	private String applicationID;
	private String contextDimensionURI;
	private String contextDomainValueURI;
	private CMMInstanceState state;
	
	public CMMInstanceStateWrapper(String applicationID,
            String contextDimensionURI, String contextDomainValueURI, CMMInstanceState state) {
	    
		this.applicationID = applicationID;
	    this.contextDimensionURI = contextDimensionURI;
	    this.contextDomainValueURI = contextDomainValueURI;
	    this.state = state;	    
    }
	
	public CMMInstanceStateWrapper(String applicationID,
            String contextDimensionURI, String contextDomainValueURI) {
	    this(applicationID, contextDimensionURI, contextDomainValueURI, CMMInstanceState.NOT_INSTALLED);
    }
	
	
	public void setInstanceState(CMMInstanceState state) {
		this.state = state;
	}

	public String getApplicationID() {
		return applicationID;
	}

	public String getContextDimensionURI() {
		return contextDimensionURI;
	}

	public String getContextDomainValueURI() {
		return contextDomainValueURI;
	}

	public CMMInstanceState getState() {
		return state;
	}
}
