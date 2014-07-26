package org.aimas.ami.cmm.agent.sensor;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import org.aimas.ami.cmm.agent.config.SensorSpecification;
import org.aimas.ami.cmm.agent.onto.AssertionUpdated;

public class AssertionUpdateBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 792037104675375166L;
    
    private CtxSensor sensorAgent;
    private AssertionUpdated assertionUpdate;
    
    public AssertionUpdateBehaviour(CtxSensor sensorAgent, AssertionUpdated assertionUpdate) {
    	super(sensorAgent);
    	this.sensorAgent = sensorAgent;
    	this.assertionUpdate = assertionUpdate;
    }
    
    
	@Override
	public void action() {
		// Send an AssertionUpdate message to the connected CtxCoordinator
		// TODO: this method will need to be revised to perform a check of sending settings, i.e.
		// which ContextAssertions need to be sent to which CtxCoord agents. For now we assume there
		// is only one active CtxCoord (as is the case in the Centralized Local deployment)
		
		SensorSpecification sensorSpec = sensorAgent.getSensorSpecification();
		AID coordinatorAID = sensorSpec.getAssignedCoordinatorAddress().getAID();
		
		String conversationId = sensorAgent.getName() + "-AssertionUpdate-" + System.currentTimeMillis();
		ACLMessage updateMsg = new ACLMessage(ACLMessage.INFORM);
		updateMsg.addReceiver(coordinatorAID);
		updateMsg.setConversationId(conversationId);
		updateMsg.setLanguage(sensorAgent.getCMMCodec().getName());
		updateMsg.setOntology(sensorAgent.getCMMOntology().getName());
		
		try {
	        sensorAgent.getContentManager().fillContent(updateMsg, assertionUpdate);
			sensorAgent.send(updateMsg);
        }
        catch (CodecException e) {
	        e.printStackTrace();
        }
        catch (OntologyException e) {
	        e.printStackTrace();
        }
	}
	
}
