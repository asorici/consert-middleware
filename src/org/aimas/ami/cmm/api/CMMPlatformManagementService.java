package org.aimas.ami.cmm.api;




public interface CMMPlatformManagementService {
	public static enum CMMInstanceState {
		NOT_INSTALLED, INSTALLED, ACTIVE
	}
	
	// Manage Platform start/stop management
	//////////////////////////////////////////////////////////////////////////////
	public void startCMMPlatform() throws Exception;
	public void stopCMMPlatform() throws Exception;
	
	// Manage CONSERT Provisioning Group deployment
	//////////////////////////////////////////////////////////////////////////////
	public CMMInstanceState getProvisioningGroupState(String applicationId, String contextDimensionURI, 
			String contextDomainValueURI);
	
	public void installProvisioningGroup(String applicationId, String contextDimensionURI, String contextDomainValueURI, 
			CMMInstanceStateOpCallback operationCallback);
	
	public CMMOperationFuture<?> installProvisioningGroup(String applicationId, 
			String contextDimensionURI, String contextDomainValueURI);
	
	
	public void startProvisioningGroup(String applicationId, String contextDimensionURI, String contextDomainValueURI, 
			CMMInstanceStateOpCallback operationCallback);
	
	public CMMOperationFuture<?> startProvisioningGroup(String applicationId, 
			String contextDimensionURI, String contextDomainValueURI);
	
	
	public void stopProvisioningGroup(String applicationId, String contextDimensionURI, String contextDomainValueURI,
			CMMInstanceStateOpCallback operationCallback);
	
	public CMMOperationFuture<?> stopProvisioningGroup(String applicationId, 
			String contextDimensionURI, String contextDomainValueURI);
	
	
	public void uninstallProvisioningGroup(String applicationId, String contextDimensionURI, String contextDomainValueURI,
			CMMInstanceStateOpCallback operationCallback);
	
	public CMMOperationFuture<?> uninstallProvisioningGroup(String applicationId, 
			String contextDimensionURI, String contextDomainValueURI);
	
	// Manage Deployment Platform changes
	//////////////////////////////////////////////////////////////////////////////
	public boolean addPlatformMtpAddress(String host, int port);
	
	public void removePlatformMtpAddress(String host, int port);
}
