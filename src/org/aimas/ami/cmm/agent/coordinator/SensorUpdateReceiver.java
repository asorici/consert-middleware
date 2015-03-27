package org.aimas.ami.cmm.agent.coordinator;

import jade.content.ContentElement;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.AssertionUpdated;

public class SensorUpdateReceiver extends SimpleBehaviour {
    private static final long serialVersionUID = 9159640468516161996L;
	private CtxCoord coordAgent;
	private MessageTemplate template;
	
	private boolean finished = false;
	
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
				
				if (!coordAgent.getContextUpdateManager().isRegistered(msg.getSender()))
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
		super(coordAgent);
		
		this.coordAgent = coordAgent;
		this.template = prepareTemplate(coordAgent);
	}
	
	public void cancelSensorUpdateReceiver() {
		finished = true;
	}
	
	@Override
    public void action() {
		if (!finished) {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				handleMessage(msg);
			}
			else {
				block();
			}
		}
    }
	
	private void handleMessage(ACLMessage msg) {
		// Get the content
		try {
			//if (coordAgent.getName().contains("SmartClassroom")) {
			//	System.out.println("["+ coordAgent.getName() +"]: receving AssertionUpdate from " + msg.getSender().getName() );
			//}
			
			AssertionUpdated assertionUpdate = (AssertionUpdated)coordAgent.getContentManager().extractContent(msg);
	        coordAgent.getContextUpdateManager().insertAssertion(msg.getSender(), assertionUpdate);
		}
        catch (Exception e) {
        	e.printStackTrace();
        }
	}

	@Override
    public boolean done() {
	    return finished;
    }
	
	@Override
	public void reset() {
	    finished = false;
	    super.reset();
	}
}
