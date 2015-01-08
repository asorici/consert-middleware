package org.aimas.ami.cmm;

import jade.util.Event;

import org.aimas.ami.cmm.agent.orgmgr.CMMOpEventResult;
import org.aimas.ami.cmm.api.CMMPlatformManagementService.CMMInstanceState;

public class CMMStartInstanceOp extends CMMInstanceStateOp {
	
	public CMMStartInstanceOp(ContextDomainInfoWrapper contextDomainInfo, CMMPlatformManager cmmPlatformManager) {
		super(contextDomainInfo, CMMStateOperationType.START, cmmPlatformManager);
	}
	
	@Override
	public Void call() throws Exception {
		// STEP 0: verify if we have to do this at all
		CMMInstanceState currentState = cmmPlatformManager.getCMMInstanceState(contextDomainInfo);
		
		if (currentState == CMMInstanceState.INSTALLED) {
			CMMPlatformRequestExecutor executor = cmmPlatformManager.getRequestExecutor(contextDomainInfo);
			if (executor != null) {
				Event startEvent = executor.startCMMInstance();
				CMMOpEventResult startResult = (CMMOpEventResult) startEvent.waitUntilProcessed();
				
				if (startResult.hasError()) {
					throw startResult.getError();
				}
				
				cmmPlatformManager.setCMMInstanceState(contextDomainInfo, CMMInstanceState.ACTIVE);
			}
		}
		
		return null;
	}
	
}
