package org.aimas.ami.cmm;

import jade.util.Event;

import org.aimas.ami.cmm.agent.orgmgr.CMMOpEventResult;
import org.aimas.ami.cmm.api.CMMPlatformManagementService.CMMInstanceState;
import org.osgi.framework.Bundle;

public class CMMKillInstanceOp extends CMMInstanceStateOp {
	
	public CMMKillInstanceOp(ContextDomainInfoWrapper contextDomainInfo, CMMPlatformManager cmmPlatformManager) {
		super(contextDomainInfo, CMMStateOperationType.UNINSTALL, cmmPlatformManager);
	}
	
	@Override
	public Void call() throws Exception {
		// STEP 0: verify if we have to do this at all
		CMMInstanceState currentState = cmmPlatformManager.getCMMInstanceState(contextDomainInfo);
		
		if (currentState == CMMInstanceState.ACTIVE || currentState == CMMInstanceState.INSTALLED) {
			CMMPlatformRequestExecutor executor = cmmPlatformManager.getRequestExecutor(contextDomainInfo);
			if (executor != null) {
				Event killEvent = executor.killCMMInstance();
				CMMOpEventResult killResult = (CMMOpEventResult) killEvent.waitUntilProcessed();
				
				if (killResult.hasError()) {
					throw killResult.getError();
				}
				
				// Mark instance as not installed
				cmmPlatformManager.setCMMInstanceState(contextDomainInfo, CMMInstanceState.NOT_INSTALLED);
				
				// And stop the bundle
				String applicationId = contextDomainInfo.getApplicationId();
				String contextDimensionURI = contextDomainInfo.getContextDimensionURI();
				String contextDomainValueURI = contextDomainInfo.getContextDomainValueURI();
				
				Bundle cmmInstanceResourceBundle = cmmPlatformManager.getCMMInstanceResourceBundle(applicationId, 
						contextDimensionURI, contextDomainValueURI);
				cmmInstanceResourceBundle.stop();
			}
		}
		
		return null;
	}
	
}
