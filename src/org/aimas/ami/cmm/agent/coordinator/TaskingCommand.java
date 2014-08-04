package org.aimas.ami.cmm.agent.coordinator;

import jade.lang.acl.ACLMessage;

import org.aimas.ami.cmm.agent.onto.ExecTask;

public abstract class TaskingCommand {
	private ExecTask task;
	
	protected TaskingCommand(ExecTask task) {
        this.task = task;
    }
	
	public ExecTask getTask() {
		return task;
	}
	
	protected abstract void handleSuccess(ACLMessage responseMsg);
	
	protected abstract void handleFailure(ACLMessage responseMsg);
}