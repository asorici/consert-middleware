package org.aimas.ami.cmm.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.wrapper.AgentController;

public interface AgentManagementService {
	
	public AgentController createNewAgent(String localName, String className, Object[] args) throws Exception;
	
	public void removeAgent(AID agentAID);
	
	public AgentController acceptNewAgent(String localName, Agent agent) throws Exception;
	
	public String getContainerName();

	public String getPlatformName();
}
