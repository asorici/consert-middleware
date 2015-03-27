package org.aimas.ami.cmm.agent.orgmgr;

import jade.core.AID;
import jade.wrapper.AgentController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aimas.ami.cmm.agent.config.AgentSpecification;
import org.aimas.ami.cmm.agent.config.CoordinatorSpecification;
import org.aimas.ami.cmm.agent.config.QueryHandlerSpecification;
import org.aimas.ami.cmm.agent.config.SensorSpecification;
import org.aimas.ami.cmm.agent.config.UserSpecification;

public class CMMAgentManager {
	public static enum State {
		ACTIVE, INACTIVE
	}
	
	/* There can be only one CtxCoordinator per applicationId that registers with this OrgMgr (the different applicationIDs
	 * come from the one beloning to the CMU managing this ContextDomain + the ones from mobile node CMUs for which this 
	 * OrgMgr becomes the __assigned__ one) */
	private Map<String, ManagedAgentWrapper<CoordinatorSpecification>> managedCoordinators;
	
	/* There can be only one CtxQueryHandler per applicationId that registers with this OrgMgr */
	private Map<String, ManagedAgentWrapper<QueryHandlerSpecification>> managedQueryHandlers;
	
	/* There can be one CtxUser per applicationId, that registers with the OrgMgr */
	private Map<String, ManagedAgentWrapper<UserSpecification>> managedUsers;
	
	/* There can be one or more CtxSensors per applicationId, that register with the OrgMgr 
	 * in order to get access to the CtxCoordinator */
	private Map<String, ManagedAgentList<SensorSpecification>> managedSensors;
	
	public CMMAgentManager() {
		managedCoordinators = new HashMap<String, ManagedAgentWrapper<CoordinatorSpecification>>();
		managedQueryHandlers = new HashMap<String, ManagedAgentWrapper<QueryHandlerSpecification>>();
		managedUsers = new HashMap<String, ManagedAgentWrapper<UserSpecification>>();
		managedSensors = new HashMap<String, ManagedAgentList<SensorSpecification>>();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	public void setManagedCoordinator(String applicationId, CoordinatorSpecification spec, AgentController controller) {
		managedCoordinators.put(applicationId, new ManagedAgentWrapper<CoordinatorSpecification>(controller, spec));
	}
	
	public void setManagedCoordinator(String applicationId, AID agentAID, State agentState) {
		managedCoordinators.put(applicationId, new ManagedAgentWrapper<CoordinatorSpecification>(agentAID, agentState));
	}
	
	public void setManagedQueryHandler(String applicationId, QueryHandlerSpecification spec, AgentController controller) {
		managedQueryHandlers.put(applicationId, new ManagedAgentWrapper<QueryHandlerSpecification>(controller, spec));
	}
	
	public void setManagedQueryHandler(String applicationId, AID agentAID, State agentState) {
		managedQueryHandlers.put(applicationId, new ManagedAgentWrapper<QueryHandlerSpecification>(agentAID, agentState));
	}
	
	
	public void setManagedUser(String applicationId, UserSpecification spec, AgentController controller) {
		managedUsers.put(applicationId, new ManagedAgentWrapper<UserSpecification>(controller, spec));
	}
	
	public void setManagedUser(String applicationId, AID agentAID, State agentState) {
		managedUsers.put(applicationId, new ManagedAgentWrapper<UserSpecification>(agentAID, agentState));
	}
	
	public void addManagedSensor(String applicationId, SensorSpecification spec, AgentController controller) {
		ManagedAgentList<SensorSpecification> agentList = managedSensors.get(applicationId);
		if (agentList == null) {
			agentList = new ManagedAgentList<SensorSpecification>();
			managedSensors.put(applicationId, agentList);
		}
		
		agentList.add(new ManagedAgentWrapper<SensorSpecification>(controller, spec));
	}
	
	public void addManagedSensor(String applicationId, AID agentAID, State agentState) {
		ManagedAgentList<SensorSpecification> agentList = managedSensors.get(applicationId);
		if (agentList == null) {
			agentList = new ManagedAgentList<SensorSpecification>();
			managedSensors.put(applicationId, agentList);
		}
		
		agentList.add(new ManagedAgentWrapper<SensorSpecification>(agentAID, agentState));
	}
	
	// Get agents 
	//////////////////////////////////////////////////////////////////////////////////////
	public ManagedAgentWrapper<CoordinatorSpecification> getManagedCoordinator(String applicationId) {
		return managedCoordinators.get(applicationId);
	}
	
	public ManagedAgentWrapper<QueryHandlerSpecification> getManagedQueryHandler(String applicationId) {
		return managedQueryHandlers.get(applicationId);
	}
	
	public List<ManagedAgentWrapper<SensorSpecification>> getManagedSensors() {
		List<ManagedAgentWrapper<SensorSpecification>> agentList = 
				new LinkedList<ManagedAgentWrapper<SensorSpecification>>();
		
		for (String applicationId : managedSensors.keySet()) {
			ManagedAgentList<SensorSpecification> appSensors = managedSensors.get(applicationId);
			agentList.addAll(appSensors);
		}
		
		return agentList;
	}
	
	public List<ManagedAgentWrapper<SensorSpecification>> getManagedSensors(String applicationId) {
		return managedSensors.get(applicationId);
	}
	
	public List<ManagedAgentWrapper<UserSpecification>> getManagedUsers() {
		return new LinkedList<ManagedAgentWrapper<UserSpecification>>(managedUsers.values());
	}
	
	public ManagedAgentWrapper<UserSpecification> getManagedUser(String applicationId) {
		return managedUsers.get(applicationId);
	}
	
	// Get specific agents
	//////////////////////////////////////////////////////////////////////////////////////
	public ManagedAgentWrapper<SensorSpecification> getManagedSensor(AID agentAID) {
		for (String applicationId : managedSensors.keySet()) {
			ManagedAgentList<SensorSpecification> appAgentList = managedSensors.get(applicationId);
			if (appAgentList.getByAID(agentAID) != null) {
				return appAgentList.getByAID(agentAID);
			}
		}
		
		return null;
	}
	
	public ManagedAgentWrapper<SensorSpecification> getManagedSensor(String applicationId, AID agentAID) {
		ManagedAgentList<SensorSpecification> appAgentList = managedSensors.get(applicationId);
		if (appAgentList != null) {
			return appAgentList.getByAID(agentAID);
		}
		
		return null;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	private static class ManagedAgentList<T extends AgentSpecification> extends LinkedList<ManagedAgentWrapper<T>> {
        private static final long serialVersionUID = 1L;

		public ManagedAgentWrapper<T> getByAID(AID agentAID) {
			for (ManagedAgentWrapper<T> managedAgent : this) {
				if (managedAgent.getAgentAID().equals(agentAID)) {
					return managedAgent;
				}
			}
			
			return null;
		}
	}
}
