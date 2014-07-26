package org.aimas.ami.cmm.agent;


public enum AgentType {
	ORG_MGR("orgmgr"), CTX_COORD("ctx-coordinator"), CTX_QUERY_HANDLER("ctx-query-handler"),
	CTX_SENSOR("ctx-sensor"), CTX_USER("ctx-user"); 
	
	private String serviceType;
	
	private AgentType(String serviceType) {
		this.serviceType = serviceType;
	}
	
	public String getServiceType() {
		return serviceType;
	}
}
