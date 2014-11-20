package org.aimas.ami.cmm.agent;

import java.util.HashMap;
import java.util.Map;


public enum AgentType {
	ORG_MGR("OrgMgr"), CTX_COORD("CtxCoord"), CTX_QUERY_HANDLER("CtxQueryHandler"),
	CTX_SENSOR("CtxSensor"), CTX_USER("CtxUser"); 
	
	private String serviceName;
	private static Map<String, AgentType> agentServiceMap;
	static {
		agentServiceMap = new HashMap<String, AgentType>();
		agentServiceMap.put(ORG_MGR.getServiceName(), ORG_MGR);
		agentServiceMap.put(CTX_COORD.getServiceName(), CTX_COORD);
		agentServiceMap.put(CTX_QUERY_HANDLER.getServiceName(), CTX_QUERY_HANDLER);
		agentServiceMap.put(CTX_SENSOR.getServiceName(), CTX_SENSOR);
		agentServiceMap.put(CTX_USER.getServiceName(), CTX_USER);
	}
	
	private AgentType(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public static AgentType fromServiceName(String serviceName) {
		return agentServiceMap.get(serviceName);
	}
}
