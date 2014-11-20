package org.aimas.ami.cmm;

import org.aimas.ami.cmm.api.CMMPlatformManagementService.CMMInstanceState;

public class CMMInstanceStateWrapper {
	
	private ContextDomainInfoWrapper contextDomainInfo;
	private CMMInstanceState state;
	private CMMPlatformRequestExecutor platformRequestExecutor;
	
	public CMMInstanceStateWrapper(ContextDomainInfoWrapper contextDomainInfo, CMMInstanceState state) {
		this.contextDomainInfo = contextDomainInfo;
		this.state = state;
	}
	
	public CMMInstanceStateWrapper(String applicationId,
            String contextDimensionURI, String contextDomainValueURI, CMMInstanceState state) {
		
		this.contextDomainInfo = new ContextDomainInfoWrapper(contextDimensionURI, contextDomainValueURI, applicationId);
		this.state = state;	    
    }
	
	public CMMInstanceStateWrapper(String applicationID,
            String contextDimensionURI, String contextDomainValueURI) {
	    this(applicationID, contextDimensionURI, contextDomainValueURI, CMMInstanceState.NOT_INSTALLED);
    }
	
	
	public void setInstanceState(CMMInstanceState state) {
		this.state = state;
	}

	public String getApplicationId() {
		return contextDomainInfo.getApplicationId();
	}

	public String getContextDimensionURI() {
		return contextDomainInfo.getContextDimensionURI();
	}

	public String getContextDomainValueURI() {
		return contextDomainInfo.getContextDomainValueURI();
	}

	public CMMInstanceState getState() {
		return state;
	}

	public CMMPlatformRequestExecutor getPlatformRequestExecutor() {
		return platformRequestExecutor;
	}

	public void setPlatformRequestExecutor(CMMPlatformRequestExecutor platformRequestExecutor) {
		this.platformRequestExecutor = platformRequestExecutor;
	}
}
