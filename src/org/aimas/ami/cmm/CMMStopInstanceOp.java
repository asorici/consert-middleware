package org.aimas.ami.cmm;

import jade.util.Event;

import org.aimas.ami.cmm.agent.orgmgr.CMMEventResult;
import org.aimas.ami.cmm.api.CMMPlatformManagementService.CMMInstanceState;

public class CMMStopInstanceOp extends CMMInstanceStateOp {
	
	public CMMStopInstanceOp(ContextDomainInfoWrapper contextDomainInfo, CMMPlatformManager cmmPlatformManager) {
		super(contextDomainInfo, CMMStateOperationType.STOP, cmmPlatformManager);
	}
	
	@Override
	public Void call() throws Exception {
		// STEP 0: verify if we have to do this at all
		CMMInstanceState currentState = cmmPlatformManager.getCMMInstanceState(contextDomainInfo);
		
		if (currentState == CMMInstanceState.ACTIVE) {
			CMMPlatformRequestExecutor executor = cmmPlatformManager.getRequestExecutor(contextDomainInfo);
			if (executor != null) {
				Event stopEvent = executor.stopCMMInstance();
				CMMEventResult stopResult = (CMMEventResult) stopEvent.waitUntilProcessed();
				
				if (stopResult.hasError()) {
					throw stopResult.getError();
				}
				
				cmmPlatformManager.setCMMInstanceState(contextDomainInfo, CMMInstanceState.INSTALLED);
			}
		}
		
		return null;
	}
	
}
