package org.aimas.ami.cmm.agent.coordinator;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.List;

import org.aimas.ami.cmm.agent.coordinator.ContextUpdateManager.AssertionState;
import org.aimas.ami.cmm.agent.coordinator.ContextUpdateManager.SensorDescription;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.StartSending;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionDescription;
import org.aimas.ami.cmm.agent.onto.impl.DefaultStartSending;

import com.hp.hpl.jena.rdf.model.Resource;

public class StartAssertionCommandResult extends AssertionCommandResult {

	protected StartAssertionCommandResult(Resource assertionResource) {
	    super(assertionResource);
    }

	@Override
    public boolean conflictsResult(CommandResult otherResult) {
		if (otherResult instanceof StopAssertionCommandResult) {
			StopAssertionCommandResult result = (StopAssertionCommandResult)otherResult;
			
			return assertionResource.equals(result.getAssertionResource());
		}
		
		return false;
    }

	@Override
    public void apply(final CommandManager commandManager) {
	    // For now we can only distinguish between having all sensors that can send updates
		// for the specified ContextAssertion enabled or disabled. In future versions
		// the constructor of this command result subclass will include the specific source (physical sensor)
		// that we want to enable.
	    
		final AssertionDescription assertionDesc = new DefaultAssertionDescription();
		assertionDesc.setAssertionType(assertionResource.getURI());
		
		final StartSending enableRequest = new DefaultStartSending();
		enableRequest.setAssertion(assertionDesc);
		
		final ContextUpdateManager sensorManager = commandManager.getCoordinatorAgent().getContextUpdateManager();
		List<AID> providingSensors = sensorManager.getProviders(assertionResource);
		
		if (providingSensors != null) {
			for (final AID sensorAgent: providingSensors) {
				SensorDescription sensorDesc = sensorManager.getSensorDescription(sensorAgent); 
				List<AssertionDescription> sensorAssertionVariants = sensorDesc.listAssertionsByURI(assertionResource.getURI());
				
				boolean needsEnabling = false;
				for (AssertionDescription ad : sensorAssertionVariants) {
					if (!sensorDesc.getAssertionState(ad).isUpdatesEnabled()) {
						needsEnabling = true;
						break;
					}
				}
				
				if (needsEnabling) {
					TaskingCommand enableAssertionTask = new TaskingCommand(enableRequest) {
						@Override
						protected void handleSuccess(ACLMessage responseMsg) {
							// if we are not timed out, confirm start sending command execution by marking the
							// ContextAssertion as active in the states (there can be more since the assertionDesc
							// also includes the number of provided annotations which may differ) 
							// corresponding to assertions of the sensorAgent matching the assertionResource URI 
							List<AssertionDescription>  descriptions = sensorManager.getSensorDescription(sensorAgent).listAssertionsByURI(assertionResource.getURI());
							for (AssertionDescription ad : descriptions) {
								AssertionState state = sensorManager.getSensorDescription(sensorAgent).getAssertionState(ad);
								state.setUpdatesEnabled(true);
							}
						
							// also mark the assertion as active in the CONSERT Engine
							commandManager.getEngineCommandAdaptor().setAssertionActive(assertionResource, true);
						}
						
						@Override
						protected void handleFailure(ACLMessage responseMsg) {
							// For now we can't do anything about it, except maybe try again later but 
							// that's not realistic
						}
					};
					
					sensorManager.submitCommand(sensorAgent, enableAssertionTask);
				}
			}
		}
    }
}
