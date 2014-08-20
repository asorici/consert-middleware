package org.aimas.ami.cmm.agent.coordinator;

import jade.content.ContentElement;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.states.MsgReceiver;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.AssertionUpdated;

public class SensorUpdateReceiver extends MsgReceiver {
    private static final long serialVersionUID = 9159640468516161996L;
	private CtxCoord coordAgent;
	
	@SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final CtxCoord coordAgent) {
	    return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				if (msg == null) {
					return false;
				}
				
				if (msg.getPerformative() != ACLMessage.INFORM) 
					return false;
				
				if (!coordAgent.getSensorManager().isRegistered(msg.getSender()))
					return false;
				
				if (!msg.getOntology().equals(CMMAgent.cmmOntology.getName())) 
					return false;
				
				try {
	                ContentElement ce = coordAgent.getContentManager().extractContent(msg);
	                if (ce == null || !(ce instanceof AssertionUpdated)) 
	                	return false;
				}
                catch (Exception e) {
                	return false;
                }
				
				return true;
			}
		});
    }
	
	public SensorUpdateReceiver(CtxCoord coordAgent) {
		super(coordAgent, prepareTemplate(coordAgent), MsgReceiver.INFINITE, null, null);
	}
	
	@Override
	protected void handleMessage(ACLMessage msg) {
		// Get the content
		try {
	        AssertionUpdated assertionUpdate = (AssertionUpdated)coordAgent.getContentManager().extractContent(msg);
	        coordAgent.getSensorManager().insertAssertion(assertionUpdate);
		}
        catch (Exception e) {}
	}
}
