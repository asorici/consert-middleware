package org.aimas.ami.cmm.api;

public interface CMMInstanceStateOpCallback {
	
	public void operationCompleted(String applicationId, String contextDimensionURI, String contextDomainValueURI);
	
	public void operationFailed(String applicationId, String contextDimensionURI, String contextDomainValueURI, Exception error);
	
}
