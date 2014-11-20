package org.aimas.ami.cmm.agent.orgmgr;

import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import org.aimas.ami.cmm.agent.onto.FoundQueryHandlerAgent;
import org.aimas.ami.cmm.agent.onto.SearchQueryHandlerAgent;
import org.aimas.ami.cmm.agent.onto.impl.DefaultFoundQueryHandlerAgent;

public class SearchQueryHandlerResponder extends AchieveREResponder {
    private static final long serialVersionUID = 5216379650016855350L;

	@SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final OrgMgr orgMgr) {
		return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// We make a pure content match
				try {
	                Action contentAction = (Action)orgMgr.getContentManager().extractContent(msg);
	                return contentAction.getAction() instanceof SearchQueryHandlerAgent;
				}
                catch (Exception e) {
	                //e.printStackTrace();
                	return false;
                }
			}
		});
	}
	
	public SearchQueryHandlerResponder(OrgMgr orgMgr) {
		super(orgMgr, prepareTemplate(orgMgr));
	}
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		OrgMgr orgMgr = (OrgMgr)myAgent;
		ACLMessage searchReplyMsg = request.createReply();
		
		try {
			// TODO: normally here we should employ a decision based on the load assigned to each
			// query handler. However, for now we only consider the existence of a single QueryHandler
			// per ContextDomain. As a consequence, we just return the first query handler in the list.
			ManagedAgentWrapper<?> managedAgent = orgMgr.agentManager.getManagedQueryHandlers().get(0);
			searchReplyMsg.setPerformative(ACLMessage.INFORM);
			
			// create the FoundCoordinatorAgent predicate
			FoundQueryHandlerAgent foundContent = new DefaultFoundQueryHandlerAgent();
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
