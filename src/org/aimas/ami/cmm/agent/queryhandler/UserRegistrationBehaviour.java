package org.aimas.ami.cmm.agent.queryhandler;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.RegisterUser;

public class UserRegistrationBehaviour extends AchieveREResponder {
    private static final long serialVersionUID = -417343153663138535L;

	private static MessageTemplate getUserRegistrationTemplate(final CtxQueryHandler ctxQueryAgent) {
		// We consider a match if we receive either a QUERY_REF, QUERY_IF, SUBSCRIBE, OR CANCEL message
		MessageTemplate mt = new MessageTemplate(new MatchExpression() {
            private static final long serialVersionUID = 1L;

			@Override
			public boolean match(ACLMessage msg) {
				return matchesLanguage(msg) && matchesContent(msg);
			}
			
			private boolean matchesLanguage(ACLMessage msg) {
				return msg.getLanguage().equals(CMMAgent.cmmCodec.getName()) &&
						msg.getOntology().equals(CMMAgent.cmmOntology.getName());
			}
			
			private boolean matchesContent(ACLMessage msg) {
				if (msg.getPerformative() != ACLMessage.REQUEST) {
					return false;
				}
				
				try {
	                Action actionContent = (Action)ctxQueryAgent.getContentManager().extractContent(msg);
	                if (!(actionContent.getAction() instanceof RegisterUser)) {
	                	return false;
	                }
				}
                catch (Exception e) {
	                e.printStackTrace();
                	return false;
                }
                
				
				return true;
			}
		});
		
		return mt;
	}
	
	public UserRegistrationBehaviour(CtxQueryHandler ctxQueryAgent) {
		super(ctxQueryAgent, getUserRegistrationTemplate(ctxQueryAgent));
	}
	
	@Override
	protected ACLMessage handleRequest(ACLMessage request) {
		// Handle the registration here, but return null. We end the protocol with
		// an INFORM-DONE message down below.
		AID user = request.getSender();
		CtxQueryHandler ctxQueryHandler = (CtxQueryHandler)myAgent;
		ctxQueryHandler.getQueryManager().registerUser(user);
		
		return null;
	}
	
	@Override
	public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		// We just have to return an INFORM message
		ACLMessage reply = request.createReply();
		reply.setPerformative(ACLMessage.INFORM);
		
		return reply;
	}
}
