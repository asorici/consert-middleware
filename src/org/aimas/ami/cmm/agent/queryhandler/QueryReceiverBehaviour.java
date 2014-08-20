package org.aimas.ami.cmm.agent.queryhandler;

import org.aimas.ami.cmm.agent.CMMAgent;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.states.MsgReceiver;

public class QueryReceiverBehaviour extends MsgReceiver {
	private static final long serialVersionUID = -6981189612500189702L;
	
	private static MessageTemplate getQueryReceiverTemplate(final CtxQueryHandler ctxQueryAgent) {
		// We consider a match if we receive either a QUERY_REF, QUERY_IF, SUBSCRIBE, OR CANCEL message
		MessageTemplate mt = new MessageTemplate(new MatchExpression() {
			
			@Override
			public boolean match(ACLMessage msg) {
				return matchesLanguage(msg) && matchesPerformative(msg);
			}
			
			private boolean matchesLanguage(ACLMessage msg) {
				return msg.getLanguage().equals(CMMAgent.cmmCodec.getName()) &&
						msg.getOntology().equals(CMMAgent.cmmOntology.getName());
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
	
	public QueryReceiverBehaviour(CtxQueryHandler ctxQueryAgent) {
		super(ctxQueryAgent, getQueryReceiverTemplate(ctxQueryAgent), INFINITE, null, null);
	}
	
	@Override
	public void handleMessage(ACLMessage msg) {
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
}
