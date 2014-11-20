package org.aimas.ami.cmm.agent.user;

import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.UpdateProfiledAssertion;

public class UserUpdateProfiledInitiator extends SimpleAchieveREInitiator {
    private static final long serialVersionUID = 3561746691002330530L;
    
    private CtxUser ctxUser;
    private UpdateProfiledAssertion profiledUpdateContent;
    
	public UserUpdateProfiledInitiator(CtxUser ctxUser, UpdateProfiledAssertion profiledUpdateContent) {
	    super(ctxUser, null);
	    
	    this.ctxUser = ctxUser;
	    this.profiledUpdateContent = profiledUpdateContent;
    }
	
	@Override
	protected ACLMessage prepareRequest(ACLMessage msg) {
		ACLMessage updateRequest = new ACLMessage(ACLMessage.REQUEST);
		updateRequest.setLanguage(CMMAgent.cmmCodec.getName());
		updateRequest.setOntology(CMMAgent.cmmOntology.getName());
		
		String conversationId = ctxUser.getName() + "-UpdateProfiledAssertion-" + System.currentTimeMillis();
		updateRequest.setConversationId(conversationId);
		updateRequest.addReceiver(ctxUser.getCoordinatorAgent());
		
		try {
			Action updateAction = new Action(ctxUser.getCoordinatorAgent(), profiledUpdateContent);
			ctxUser.getContentManager().fillContent(updateRequest, updateAction);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		
		return updateRequest;
	}
	
	
	@Override
	protected void handleRefuse(ACLMessage msg) {
		// See if we need to treat the case in which update is refused (e.g. when updates for a 
		// profiled ContextAssertion are disabled, but the application user sends this one-shot update one anyway) 
	}
	
	@Override
	protected void handleInform(ACLMessage msg) {
		// See if any notification needs to take place. The default is to do nothing.
	}
}
