package org.aimas.ami.cmm.agent.coordinator;

import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import org.aimas.ami.cmm.agent.onto.UpdateProfiledAssertion;

public class UserUpdateProfiledResponder extends AchieveREResponder {
    private static final long serialVersionUID = -6882886448380845991L;

    private CtxCoord coordAgent;
    
    @SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final CtxCoord coordAgent) {
    	return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// We make a pure content match
				try {
	                Action contentAction = (Action)coordAgent.getContentManager().extractContent(msg);
	                return contentAction.getAction() instanceof UpdateProfiledAssertion;
				}
                catch (Exception e) {
	                //e.printStackTrace();
                	return false;
                }
			}
		});
    }
    
	public UserUpdateProfiledResponder(CtxCoord coordAgent) {
		super(coordAgent, prepareTemplate(coordAgent));
		this.coordAgent = coordAgent;
	}
	
	@Override
	protected ACLMessage handleRequest(ACLMessage request) {
		try {
            Action contentAction = (Action)coordAgent.getContentManager().extractContent(request);
            UpdateProfiledAssertion profiledAssertionUpdate = (UpdateProfiledAssertion)contentAction.getAction();
            
            boolean updateAllowed = coordAgent.getContextUpdateManager().updateProfiledAssertion(request.getSender(), profiledAssertionUpdate);
            
            if (!updateAllowed) {
            	ACLMessage updateReplyMsg = request.createReply();
        		updateReplyMsg.setPerformative(ACLMessage.REFUSE);
        		
        		return updateReplyMsg;
            }
		}
        catch (Exception e) {
            e.printStackTrace();
        }
		
		return null;
	}
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		ACLMessage updateReplyMsg = request.createReply();
		updateReplyMsg.setPerformative(ACLMessage.INFORM);
		
		return updateReplyMsg;
	}
}
