package org.aimas.ami.cmm.agent.orgmgr;

import jade.core.AID;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import org.aimas.ami.cmm.agent.config.AgentSpecification;
import org.aimas.ami.cmm.agent.orgmgr.CMMAgentManager.State;

public class ManagedAgentWrapper<T extends AgentSpecification> {
	AgentController agentController;
	T agentSpecification;
	AID agentAID;
	State state;
	
	public ManagedAgentWrapper(AgentController agentController, T agentSpecification, State state) {
        this.agentController = agentController;
        this.agentSpecification = agentSpecification;
        this.agentAID = agentSpecification.getAgentAddress().getAID();
        this.state = state;
    }
	
	public ManagedAgentWrapper(AgentController agentController, T agentSpecification) {
        this(agentController, agentSpecification, State.INACTIVE);
    }
	
	public ManagedAgentWrapper(AID agentAID, State state) {
		this.agentAID = agentAID;
		this.state = state;
	}
	
	public ManagedAgentWrapper(AID agentAID) {
		this(agentAID, State.INACTIVE);
	}
	
    public AgentController getAgentController() {
		return agentController;
	}
    
    public boolean isLocalAgent() {
    	return agentController != null;
    }
    
	public T getAgentSpecification() {
		return agentSpecification;
	}
	
	public boolean hasAgentSpecification() {
		return agentSpecification != null;
	}
	
	public AID getAgentAID() {
		return agentAID;
	}
	
	public State getState() {
		return state;
	}
	
	public boolean isActive() {
		return state == State.ACTIVE;
	}
	
	public void start() throws StaleProxyException {
		if (state != State.ACTIVE) {
			if (agentController != null) {
				agentController.start();
				state = State.ACTIVE;
			}
			else {
				// TODO: otherwise we must send a message to start the agent
			}
		}
	}
	
	public void stop() throws StaleProxyException {
		if (state != State.INACTIVE) {
			if (agentController != null) {
				agentController.kill();
				state = State.INACTIVE;
			}
			else {
				// TODO: otherwise we must send a message to stop the agent
			}
		}
	}
}