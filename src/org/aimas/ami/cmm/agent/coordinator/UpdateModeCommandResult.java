package org.aimas.ami.cmm.agent.coordinator;

import java.util.List;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import org.aimas.ami.cmm.agent.coordinator.SensorManager.AssertionState;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.SetUpdateMode;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionDescription;
import org.aimas.ami.cmm.agent.onto.impl.DefaultSetUpdateMode;

import com.hp.hpl.jena.rdf.model.Resource;

public class UpdateModeCommandResult extends AssertionCommandResult {
	private String updateMode;
	private int updateRate;
	
	public UpdateModeCommandResult(Resource assertionResource, String updateMode, int updateRate) {
		super(assertionResource);
		
		this.updateMode = updateMode;
		this.updateRate = updateRate;
	}
	
	@Override
	public boolean conflictsResult(CommandResult otherResult) {
		if (otherResult instanceof UpdateModeCommandResult) {
			UpdateModeCommandResult result = (UpdateModeCommandResult)otherResult;
			
			if (result.getAssertionResource().equals(assertionResource)) {
				if (!result.getUpdateMode().equals(updateMode)) {
					return true;
				}
				else if (result.getUpdateRate() != updateRate) {
					return true;
				}
			}
		}
		
		return false;
	}

	public String getUpdateMode() {
		return updateMode;
	}

	public int getUpdateRate() {
		return updateRate;
	}

	@Override
    public void apply(CommandManager commandManager) {
		// For now we can only have all sensors that can send updates for a ContextAssertion 
		// do it the same mode. In future versions the constructor of this command result subclass will include the 
		// specific source (physical sensor) for which we want to change the update mode.
		
		final AssertionDescription assertionDesc = new DefaultAssertionDescription();
		assertionDesc.setAssertionType(assertionResource.getURI());
		
		SetUpdateMode updateModeTask = new DefaultSetUpdateMode();
	    updateModeTask.setAssertion(assertionDesc);
	    updateModeTask.setUpdateMode(updateMode);
	    updateModeTask.setUpdateRate(updateRate);
	    
	    final SensorManager sensorManager = commandManager.getCoordinatorAgent().getSensorManager();
	    List<AID> providingSensors = sensorManager.getProviders(assertionResource);
	    
	    for (final AID sensorAgent: providingSensors) {
		    TaskingCommand updateModeCommand = new TaskingCommand(updateModeTask) {
				@Override
				protected void handleSuccess(ACLMessage responseMsg) {
					// If we are not timed out, confirm updateMode command execution by setting the
					// update parameters in the state corresponding to the assertionDesc of the sensorAgent
					AssertionState state = sensorManager.getSensorDescription(sensorAgent).getAssertionState(assertionDesc);
					state.setUpdateMode(updateMode);
					state.setUpdateRate(updateRate);
				}
				
				@Override
				protected void handleFailure(ACLMessage responseMsg) {
					// For now we can't do anything about it, except maybe try again later but 
					// that's not realistic
				}
			};
			
			sensorManager.submitCommand(sensorAgent, updateModeCommand);
	    }
    }
}
