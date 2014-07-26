package org.aimas.ami.cmm.agent.orgmgr;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import org.aimas.ami.cmm.agent.config.AgentSpecification;
import org.aimas.ami.cmm.agent.orgmgr.CMMAgentManager.State;

public class ManagedAgentWrapper<T extends AgentSpecification> {
	AgentController agentController;
	T agentSpecification;
	State state;
	
	public ManagedAgentWrapper(AgentController agentController, T agentSpecification) {
        this.agentController = agentController;
        this.agentSpecification = agentSpecification;
        this.state = State.INACTIVE;
    }

    public AgentController getAgentController() {
		return agentController;
	}

	public T getAgentSpecification() {
		return agentSpecification;
	}
	
	public State getState() {
		return state;
	}
	
	public boolean isActive() {
		return state == State.ACTIVE;
	}
	
	public void start() throws StaleProxyException {
		if (state != State.ACTIVE) {
			agentController.start();
			state = State.ACTIVE;
		}
	}
	
	public void stop() throws StaleProxyException {
		if (state != State.INACTIVE) {
			agentController.kill();
			state = State.INACTIVE;
		}
	}
}