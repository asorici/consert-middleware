package org.aimas.ami.cmm.agent.coordinator;

import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import org.aimas.ami.cmm.agent.onto.UpdateEntityDescriptions;

public class UserUpdateStaticResponder extends AchieveREResponder {
    private static final long serialVersionUID = -6444374409698359351L;
    
    private CtxCoord coordAgent;
    
    @SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final CtxCoord coordAgent) {
    	return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// We make a pure content match
				try {
	                Action contentAction = (Action)coordAgent.getContentManager().extractContent(msg);
	                return contentAction.getAction() instanceof UpdateEntityDescriptions;
				}
                catch (Exception e) {
	                //e.printStackTrace();
                	return false;
                }
			}
		});
    }
    
	public UserUpdateStaticResponder(CtxCoord coordAgent) {
		super(coordAgent, prepareTemplate(coordAgent));
		this.coordAgent = coordAgent;
	}
	
	@Override
	protected ACLMessage handleRequest(ACLMessage request) {
		try {
            Action contentAction = (Action)coordAgent.getContentManager().extractContent(request);
            UpdateEntityDescriptions entitiesUpdate = (UpdateEntityDescriptions)contentAction.getAction();
            coordAgent.getContextUpdateManager().updateEntityDescriptions(entitiesUpdate);
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
