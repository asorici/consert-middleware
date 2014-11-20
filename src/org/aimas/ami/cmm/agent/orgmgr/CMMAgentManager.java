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
	
	/* There is only one CtxCoordinator per OrgMgr (i.e. per ContextDomain) */
	private ManagedAgentWrapper<CoordinatorSpecification> managedCoordinator;
	
	/* There can be one or more CtxQueryHandlers (a linear scaling) per OrgMgr (i.e. per ContextDomain) */
	private ManagedAgentList<QueryHandlerSpecification> managedQueryHandlers;
	
	/* There can be one CtxUser per applicationId, that registers with the OrgMgr */
	private Map<String, ManagedAgentWrapper<UserSpecification>> managedUsers;
	
	/* There can be one or more CtxSensors per applicationId, that register with the OrgMgr 
	 * in order to get access to the CtxCoordinator */
	private Map<String, ManagedAgentList<SensorSpecification>> managedSensors;
	
	public CMMAgentManager() {
		managedQueryHandlers = new ManagedAgentList<QueryHandlerSpecification>();
		managedUsers = new HashMap<String, ManagedAgentWrapper<UserSpecification>>();
		managedSensors = new HashMap<String, ManagedAgentList<SensorSpecification>>();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	public void setManagedCoordinator(CoordinatorSpecification spec, AgentController controller) {
		managedCoordinator = new ManagedAgentWrapper<CoordinatorSpecification>(controller, spec);
	}
	
	public void setManagedCoordinator(AID agentAID, State agentState) {
		managedCoordinator = new ManagedAgentWrapper<CoordinatorSpecification>(agentAID, agentState);
	}
	
	public void addManagedQueryHandler(QueryHandlerSpecification spec, AgentController controller) {
		managedQueryHandlers.add(new ManagedAgentWrapper<QueryHandlerSpecification>(controller, spec));
	}
	
	public void addManagedQueryHandler(AID agentAID, State agentState) {
		managedQueryHandlers.add(new ManagedAgentWrapper<QueryHandlerSpecification>(agentAID, agentState));
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
	public ManagedAgentWrapper<CoordinatorSpecification> getManagedCoordinator() {
		return managedCoordinator;
	}
	
	public List<ManagedAgentWrapper<QueryHandlerSpecification>> getManagedQueryHandlers() {
		return managedQueryHandlers;
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
	
	public List<ManagedAgentWrapper<UserSpecification>> getManagedUsers() {
		return new LinkedList<ManagedAgentWrapper<UserSpecification>>(managedUsers.values());
	}
	
	// Get specific agents
	//////////////////////////////////////////////////////////////////////////////////////
	public ManagedAgentWrapper<QueryHandlerSpecification> getManagedQueryHandler(AID agentAID) {
		return managedQueryHandlers.getByAID(agentAID);
	}
	
	public List<ManagedAgentWrapper<SensorSpecification>> getManagedSensors(String applicationId) {
		return managedSensors.get(applicationId);
	}
	
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
	
	public ManagedAgentWrapper<UserSpecification> getManagedUser(String applicationId) {
		return managedUsers.get(applicationId);
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
