package org.aimas.ami.cmm;

import java.util.concurrent.Callable;

import org.aimas.ami.cmm.api.CMMPlatformManagementService.CMMInstanceState;

public abstract class CMMInstanceStateOp implements Callable<Void> {
	public static enum CMMStateOperationType {
		INSTALL, START, STOP, UNINSTALL;
		
		public boolean opposes(CMMStateOperationType op) {
			if (this == INSTALL && op == UNINSTALL) {
				return true;
			}
			else if (this == START && (op == STOP || op == UNINSTALL)) {
				return true;
			}
			else if (this == STOP && op == START) {
				return true;
			}
			else if (this == UNINSTALL && (op == INSTALL || op == START)) {
				return true;
			}
			
			return false;
		}
	}
	
	protected ContextDomainWrapper contextDomainInfo;
	protected CMMStateOperationType operationType;
	protected CMMPlatformManager cmmPlatformManager;
	
	
	protected CMMInstanceStateOp(ContextDomainWrapper contextDomainInfo, 
			CMMStateOperationType operationType, CMMPlatformManager cmmPlatformManager) {
		this.contextDomainInfo = contextDomainInfo;
		this.operationType = operationType;
		this.cmmPlatformManager = cmmPlatformManager;
	}
	
	public ContextDomainWrapper getContextDomainInfo() {
		return contextDomainInfo;
	}
	
	public CMMStateOperationType getType() {
		return operationType;
	}
	
	public boolean opposes(CMMInstanceStateOp op) {
		return operationType.opposes(op.getType());
	}
	
	public CMMInstanceState getCurrentState() {
		return cmmPlatformManager.getCMMInstanceState(contextDomainInfo);
	}
	
	@Override
	public abstract Void call() throws Exception;
}
