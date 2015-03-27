package org.aimas.ami.cmm.agent.orgmgr;

import jade.content.onto.basic.Action;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import org.aimas.ami.cmm.agent.onto.FoundCoordinatorAgent;
import org.aimas.ami.cmm.agent.onto.SearchCoordinatorAgent;
import org.aimas.ami.cmm.agent.onto.impl.DefaultFoundCoordinatorAgent;

public class SearchCoordinatorResponder extends AchieveREResponder {
    private static final long serialVersionUID = 5216379650016855350L;

	@SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final OrgMgr orgMgr) {
		return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// We make a pure content match
				try {
	                Action contentAction = (Action)orgMgr.getContentManager().extractContent(msg);
	                return contentAction.getAction() instanceof SearchCoordinatorAgent;
				}
                catch (Exception e) {
	                //e.printStackTrace();
                	return false;
                }
			}
		});
	}
	
	public SearchCoordinatorResponder(OrgMgr orgMgr) {
		super(orgMgr, prepareTemplate(orgMgr));
	}
	
	@Override
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		return null;
	}
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		OrgMgr orgMgr = (OrgMgr)myAgent;
		ACLMessage searchReplyMsg = request.createReply();
		
		try {
			ManagedAgentWrapper<?> managedAgent = orgMgr.agentManager.getManagedCoordinator(orgMgr.appSpecification.getAppIdentifier());
			searchReplyMsg.setPerformative(ACLMessage.INFORM);
			
			// create the FoundCoordinatorAgent predicate
			FoundCoordinatorAgent foundContent = new DefaultFoundCoordinatorAgent();
			foundContent.setAgent(managedAgent.getAgentAID());
			
			orgMgr.getContentManager().fillContent(searchReplyMsg, foundContent);
		}
        catch (Exception e) {
        	e.printStackTrace();
        	searchReplyMsg.setPerformative(ACLMessage.FAILURE);
        }
		
		return searchReplyMsg;
	}
}
