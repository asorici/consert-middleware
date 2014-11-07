package org.aimas.ami.cmm.api;



public interface CMMPlatformManagementService {
	public static enum CMMInstanceState {
		NOT_INSTALLED, INSTALLED, ACTIVE
	}
	
	// Manage CONSERT Middleware instance deployment
	//////////////////////////////////////////////////////////////////////////////
	public CMMInstanceState getCMMInstanceState(String applicationId, String contextDimensionURI, 
			String contextDomainValueURI);
	
	public void installCMMInstance(String applicationId, String contextDimensionURI, String contextDomainValueURI, 
			CMMInstanceStateOpCallback operationCallback);
	
	public CMMOperationFuture<?> installCMMInstance(String applicationId, 
			String contextDimensionURI, String contextDomainValueURI);
	
	
	public void startCMMInstance(String applicationId, String contextDimensionURI, String contextDomainValueURI, 
			CMMInstanceStateOpCallback operationCallback);
	
	public CMMOperationFuture<?> startCMMInstance(String applicationId, 
			String contextDimensionURI, String contextDomainValueURI);
	
	
	public void stopCMMInstance(String applicationId, String contextDimensionURI, String contextDomainValueURI,
			CMMInstanceStateOpCallback operationCallback);
	
	public CMMOperationFuture<?> stopCMMInstance(String applicationId, 
			String contextDimensionURI, String contextDomainValueURI);
	
	
	public void uninstallCMMInstance(String applicationId, String contextDimensionURI, String contextDomainValueURI,
			CMMInstanceStateOpCallback operationCallback);
	
	public CMMOperationFuture<?> uninstallCMMInstance(String applicationId, 
			String contextDimensionURI, String contextDomainValueURI);
	
	// Manage Deployment Platform changes
	//////////////////////////////////////////////////////////////////////////////
	public void addPlatformMtpAddress(String host, int port);
	
	public void removePlatformMtpAddress(String host, int port);
}
