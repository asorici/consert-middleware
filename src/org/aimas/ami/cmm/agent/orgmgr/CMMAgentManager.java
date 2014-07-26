package org.aimas.ami.cmm.agent.orgmgr;

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
	
	private Map<String, ManagedAgentWrapper<CoordinatorSpecification>> managedCoordinators;
	private Map<String, ManagedAgentWrapper<QueryHandlerSpecification>> managedQueryHandlers;
	private Map<String, ManagedAgentWrapper<UserSpecification>> managedUsers;
	private Map<String, ManagedAgentWrapper<SensorSpecification>> managedSensors;
	
	
	public CMMAgentManager() {
		managedCoordinators = new HashMap<String, ManagedAgentWrapper<CoordinatorSpecification>>();
		managedQueryHandlers = new HashMap<String, ManagedAgentWrapper<QueryHandlerSpecification>>();
		managedUsers = new HashMap<String, ManagedAgentWrapper<UserSpecification>>();
		managedSensors = new HashMap<String, ManagedAgentWrapper<SensorSpecification>>();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	public void addManagedCoordinator(String agentName, CoordinatorSpecification spec, AgentController controller) {
		managedCoordinators.put(agentName, new ManagedAgentWrapper<CoordinatorSpecification>(controller, spec));
	}
	
	public void addManagedQueryHandler(String agentName, QueryHandlerSpecification spec, AgentController controller) {
		managedQueryHandlers.put(agentName, new ManagedAgentWrapper<QueryHandlerSpecification>(controller, spec));
	}
	
	public void addManagedUser(String agentName, UserSpecification spec, AgentController controller) {
		managedUsers.put(agentName, new ManagedAgentWrapper<UserSpecification>(controller, spec));
	}
	
	public void addManagedSensor(String agentName, SensorSpecification spec, AgentController controller) {
		managedSensors.put(agentName, new ManagedAgentWrapper<SensorSpecification>(controller, spec));
	}
	
	public List<ManagedAgentWrapper<CoordinatorSpecification>> getManagedCoordinators() {
		return new LinkedList<ManagedAgentWrapper<CoordinatorSpecification>>(managedCoordinators.values());
	}
	
	public List<ManagedAgentWrapper<QueryHandlerSpecification>> getManagedQueryHandlers() {
		return new LinkedList<ManagedAgentWrapper<QueryHandlerSpecification>>(managedQueryHandlers.values());
	}
	
	public List<ManagedAgentWrapper<SensorSpecification>> getManagedSensors() {
		return new LinkedList<ManagedAgentWrapper<SensorSpecification>>(managedSensors.values());
	}
	
	public List<ManagedAgentWrapper<UserSpecification>> getManagedUsers() {
		return new LinkedList<ManagedAgentWrapper<UserSpecification>>(managedUsers.values());
	}
	
	private ManagedAgentWrapper<?> findManagedAgent(String agentName) {
		if (managedCoordinators.containsKey(agentName)) 
			return managedCoordinators.get(agentName);
		
		if (managedQueryHandlers.containsKey(agentName)) 
			return managedQueryHandlers.get(agentName);
		
		if (managedUsers.containsKey(agentName)) 
			return managedUsers.get(agentName);
		
		return managedSensors.get(agentName);
    }
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	public void startAgent(String agentName) throws Exception {
		ManagedAgentWrapper<?> managedAgent = findManagedAgent(agentName);
		
		if (managedAgent != null)
			managedAgent.start();
	}
	
	public void stopAgent(String agentName) throws Exception {
		ManagedAgentWrapper<?> managedAgent = findManagedAgent(agentName);
		
		if (managedAgent != null)
			managedAgent.stop();
	}
	
	public State getAgentState(String agentName) {
		ManagedAgentWrapper<?> managedAgent = findManagedAgent(agentName);
		
		if (managedAgent != null)
			return managedAgent.getState();
		
		return null;
	}
	
	
	public boolean managesAgent(String agentName) {
		return findManagedAgent(agentName) != null;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	public AgentSpecification getAgentSpecification(String agentName) {
		ManagedAgentWrapper<?> managedAgent = findManagedAgent(agentName);
		
		if (managedAgent != null) {
			return managedAgent.getAgentSpecification();
		}
		
		return null;
	}
	
	public CoordinatorSpecification getCoordinatorSpecification(String agentName) {
		ManagedAgentWrapper<CoordinatorSpecification> managedAgent = managedCoordinators.get(agentName);
		
		if (managedAgent != null) {
			return managedAgent.getAgentSpecification();
		}
		
		return null;
	}
	
	public QueryHandlerSpecification getQueryHandlerSpecification(String agentName) {
		ManagedAgentWrapper<QueryHandlerSpecification> managedAgent = managedQueryHandlers.get(agentName);
		
		if (managedAgent != null) {
			return managedAgent.getAgentSpecification();
		}
		
		return null;
	}
	
	public UserSpecification getUserSpecification(String agentName) {
		ManagedAgentWrapper<UserSpecification> managedAgent = managedUsers.get(agentName);
		
		if (managedAgent != null) {
			return managedAgent.getAgentSpecification();
		}
		
		return null;
	}
	
	public SensorSpecification getSensorSpecification(String agentName) {
		ManagedAgentWrapper<SensorSpecification> managedAgent = managedSensors.get(agentName);
		
		if (managedAgent != null) {
			return managedAgent.getAgentSpecification();
		}
		
		return null;
	}
}
