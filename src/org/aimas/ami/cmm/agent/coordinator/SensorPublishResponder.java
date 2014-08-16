package org.aimas.ami.cmm.agent.coordinator;

import jade.content.ContentElement;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.ProposeResponder;
import jade.util.leap.List;

import org.aimas.ami.cmm.agent.coordinator.SensorManager.AssertionState;
import org.aimas.ami.cmm.agent.coordinator.SensorManager.SensorDescription;
import org.aimas.ami.cmm.agent.onto.AssertionCapability;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.EnableAssertions;
import org.aimas.ami.cmm.agent.onto.PublishAssertions;
import org.aimas.ami.cmm.agent.onto.SetUpdateMode;
import org.aimas.ami.cmm.agent.onto.impl.DefaultEnableAssertions;
import org.aimas.ami.cmm.agent.onto.impl.DefaultSetUpdateMode;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SensorPublishBehaviour extends ProposeResponder {
    private static final long serialVersionUID = 5021316588422631253L;
    
    private CtxCoord coordAgent;
    
    @SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final CtxCoord coordAgent) {
	    return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				if (msg.getPerformative() != ACLMessage.PROPOSE) {
					return false;
				}
				
				if (msg.getOntology() == null || 
					!msg.getOntology().equals(coordAgent.getCMMOntology().getName())) {
					return false;
				}
				
				return true;
			}
		});
    }
    
    
    public SensorPublishBehaviour(CtxCoord coordAgent) {
		super(coordAgent, prepareTemplate(coordAgent));
		this.coordAgent = coordAgent;
	}
    
    
    @Override
    protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
    	EnableAssertions enableAssertions = null;
    	try {
	        ContentElement ce = coordAgent.getContentManager().extractContent(propose);
	        if (ce instanceof PublishAssertions) {
	        	PublishAssertions publishedAssertions = (PublishAssertions)ce;
	        	enableAssertions = createDescriptionAndEnable(propose.getSender(), publishedAssertions);
	        }
	        else {
	        	throw new NotUnderstoodException("Received proposal is not a "
	        			+ "PublishAssertions message: " + propose.getContent());
	        }
        }
        catch (Exception e) {
	        throw new NotUnderstoodException("Publish Assertions message not understood: " + e.getMessage());
        }
    	
    	ACLMessage response = propose.createReply();
    	response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
    	try {
	        coordAgent.getContentManager().fillContent(response, enableAssertions);
        }
        catch (Exception e) {}
    	
    	return response;
    }
    
    
    /* TODO: WE MUST PACKAGE ALL OF THIS IN A CUSTOM PROTOCOL - THERE'S TOO MUCH ASYNCRONY GOING ON */
	private EnableAssertions createDescriptionAndEnable(AID sensorAgent, PublishAssertions publishedAssertions) {
		/* The default when a new sensor publishes his capabilities is to:
		 * 	- first search to see if a similar ContextAssertion description already exists and,
		 *    if true, copy its state
		 *  - if no similar ContextAssertion description exists, search in the control parameters for
		 *    the default initial state
		 *    
		 * ATTENTION: This mechanism might be sufficient now, but might be augmented further by analysis of the
		 * current context and the specific annotation capabilities of the assertion !!! 
		 */
		EnableAssertions enabledAssertions = new DefaultEnableAssertions();
		
		// Create the new sensor description entry
		SensorDescription sensorDescription = new SensorDescription();
		
		List capabilities = publishedAssertions.getCapability();
		for (int i = 0; i < capabilities.size(); i++) {
			AssertionCapability capability = (AssertionCapability) capabilities.get(i);
			AssertionState state = sensorDescription.addCapability(capability);
			
			SensorManager sm = coordAgent.getSensorManager();
			AssertionState existingState = sm.matchDescription(capability.getAssertion());
			
			if (existingState != null) {
				// If there is a state for such assertions
				state.setUpdatesEnabled(existingState.isUpdatesEnabled());
				deliverUpdateModeCommand(sensorAgent, capability.getAssertion(), existingState);
			}
			else {
				// Otherwise, check the control parameters
				CommandManager cm = coordAgent.getCommandManager();
				ControlParameters cp = cm.getControlParameters();
				
				Resource assertionRes = 
					ResourceFactory.createResource(capability.getAssertion().getAssertionType());
				
				if (cp.specificUpdateEnabled().containsKey(assertionRes)) {
					state.setUpdatesEnabled(cp.specificUpdateEnabled().get(assertionRes));
				}
				else {
					state.setUpdatesEnabled(cp.defaultUpdateEnabled());
				}
			}
			
			if (state.isUpdatesEnabled()) {
				enabledAssertions.addCapability(capability);
			}
		}
		
		return enabledAssertions;
    }


	private void deliverUpdateModeCommand(final AID sensorAgent, final AssertionDescription assertionDesc, 
			AssertionState existingState) {
	    
		final String updateMode = existingState.getUpdateMode();
	    final int updateRate = existingState.getUpdateRate();
	    
	    SetUpdateMode updateModeTask = new DefaultSetUpdateMode();
	    updateModeTask.setAssertion(assertionDesc);
	    updateModeTask.setUpdateMode(updateMode);
	    updateModeTask.setUpdateRate(updateRate);
	    
	    final SensorManager sensorManager = coordAgent.getSensorManager();
	    
	    TaskingCommand updateModeCommand = new TaskingCommand(updateModeTask) {
			@Override
			protected void handleSuccess(ACLMessage responseMsg) {
				// if we are not timed out, confirm updateMode command execution by setting the
				// update parameters in the state corresponding to the assertionDesc of the sensorAgent
				AssertionState state = sensorManager.getSensorDescription(sensorAgent).getAssertionState(assertionDesc);
				state.setUpdateMode(updateMode);
				state.setUpdateRate(updateRate);
			
				// finally, remove this command from the pending queue
				sensorManager.removeCommand(this);
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
