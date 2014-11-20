package org.aimas.ami.cmm.utils;

import jade.core.Agent;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.ServiceHelper;

import org.aimas.ami.cmm.CMMPlatformManager;

public class JadeOSGIBridgeService extends BaseService {
	
	public static final String NAME = JadeOSGIBridgeHelper.SERVICE_NAME;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public ServiceHelper getHelper(Agent a) throws ServiceException {
		// The only agents that can ask for this service are our CMM Agents which all stem from
		// this bundle, so we create a helper which sets the bundle context to that given by the CMMPlatformManager
		return new JadeOSGIBridgeHelperImpl(CMMPlatformManager.getInstance().getBundleContext());
	}
	
	// Redefine this method to avoid requiring the OSGiBridgeService class in the Main Container classpath
	@Override
	public  boolean isLocal() {
		return true;
	}
}
