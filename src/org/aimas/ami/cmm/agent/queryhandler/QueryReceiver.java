package org.aimas.ami.cmm.agent.queryhandler;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;

import org.aimas.ami.cmm.agent.CMMAgent;

public class QueryReceiver extends SimpleBehaviour {
	private static final long serialVersionUID = -6981189612500189702L;
	
	@SuppressWarnings("serial")
    private static MessageTemplate getQueryReceiverTemplate(final CtxQueryHandler ctxQueryAgent) {
		// We consider a match if we receive either a QUERY_REF, QUERY_IF, SUBSCRIBE, OR CANCEL message
		MessageTemplate mt = new MessageTemplate(new MatchExpression() {
			
			@Override
			public boolean match(ACLMessage msg) {
				return matchesLanguage(msg) && matchesPerformative(msg);
			}
			
			private boolean matchesLanguage(ACLMessage msg) {
				if (msg.getLanguage() != null && msg.getOntology() != null) {
					return msg.getLanguage().equals(CMMAgent.cmmCodec.getName()) &&
						msg.getOntology().equals(CMMAgent.cmmOntology.getName());
				}
				
				return false;
			}
			
			private boolean matchesPerformative(ACLMessage msg) {
				return msg.getPerformative() == ACLMessage.QUERY_IF ||
						msg.getPerformative() == ACLMessage.QUERY_REF ||
						msg.getPerformative() == ACLMessage.SUBSCRIBE ||
						msg.getPerformative() == ACLMessage.CANCEL;
			}
		});
		
		return mt;
	}
	
	private MessageTemplate template;
	private boolean finished = false;
	
	public QueryReceiver(CtxQueryHandler ctxQueryAgent) {
		super(ctxQueryAgent);
		this.template = getQueryReceiverTemplate(ctxQueryAgent);
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
		CtxQueryHandler ctxQueryAgent = (CtxQueryHandler)myAgent;
		QueryManager queryManager = ctxQueryAgent.getQueryManager();
		
		if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {
			queryManager.registerSubscription(msg.getSender(), msg);
		}
		else if (msg.getPerformative() == ACLMessage.CANCEL) {
			queryManager.cancelSubscription(msg.getSender(), msg);
		}
		else {
			queryManager.executeQuery(msg.getSender(), msg);
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
