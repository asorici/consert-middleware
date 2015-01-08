package org.aimas.ami.cmm.agent.sensor;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.AssertionUpdated;

public class AssertionUpdateBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 792037104675375166L;
    
    private CMMAgent senderAgent;
    private AID coordinatorAID;
    private AssertionUpdated assertionUpdate;
    
    public AssertionUpdateBehaviour(CMMAgent senderAgent, AID coordinatorAID, AssertionUpdated assertionUpdate) {
    	super(senderAgent);
    	
    	this.senderAgent = senderAgent;
    	this.coordinatorAID = coordinatorAID;
    	this.assertionUpdate = assertionUpdate;
    }
    
    
	@Override
	public void action() {
		// Send an AssertionUpdate message to the specified CtxCoordinator
		String conversationId = senderAgent.getName() + "-AssertionUpdate-" 
				+ System.currentTimeMillis() + "-" + getCnt();
		
		ACLMessage updateMsg = new ACLMessage(ACLMessage.INFORM);
		updateMsg.addReceiver(coordinatorAID);
		updateMsg.setConversationId(conversationId);
		updateMsg.setLanguage(CMMAgent.cmmCodec.getName());
		updateMsg.setOntology(CMMAgent.cmmOntology.getName());
		
		try {
	        senderAgent.getContentManager().fillContent(updateMsg, assertionUpdate);
			senderAgent.send(updateMsg);
        }
        catch (CodecException e) {
	        e.printStackTrace();
        }
        catch (OntologyException e) {
	        e.printStackTrace();
        }
	}
	
	private static int counter = 0;
	private static synchronized int getCnt() {
		return counter++;
	}
}
