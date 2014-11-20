package org.aimas.ami.cmm.agent.orgmgr;

import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.onto.RegisterCMMAgent;
import org.aimas.ami.cmm.agent.orgmgr.CMMAgentManager.State;

public class RegisterCMMAgentResponder extends AchieveREResponder {
    private static final long serialVersionUID = -2410395858545932234L;

	@SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final OrgMgr orgMgr) {
		return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// We make a pure content match
				try {
	                Action contentAction = (Action)orgMgr.getContentManager().extractContent(msg);
	                return contentAction.getAction() instanceof RegisterCMMAgent;
				}
                catch (Exception e) {
	                //e.printStackTrace();
                	return false;
                }
			}
		});
	}
	
	public RegisterCMMAgentResponder(OrgMgr orgMgr) {
		super(orgMgr, prepareTemplate(orgMgr));
	}
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		OrgMgr orgMgr = (OrgMgr)myAgent;
		
		try {
			Action contentAction = (Action)orgMgr.getContentManager().extractContent(request);
			RegisterCMMAgent requestContent = (RegisterCMMAgent) contentAction.getAction();
	        
			String appIdentifier = requestContent.getAppIdentifier();
			AgentType agentType = AgentType.fromServiceName(requestContent.getAgentService());
			State agentState = requestContent.isAgentActive() ? State.ACTIVE : State.INACTIVE;
			
			if (agentType == AgentType.CTX_COORD) {
				orgMgr.agentManager.setManagedCoordinator(request.getSender(), agentState);
			}
			else if (agentType == AgentType.CTX_QUERY_HANDLER) {
				orgMgr.agentManager.addManagedQueryHandler(request.getSender(), agentState);
			}
			else if (agentType == AgentType.CTX_SENSOR) {
				orgMgr.agentManager.addManagedSensor(appIdentifier, request.getSender(), agentState);
			}
			else if (agentType == AgentType.CTX_USER) {
				orgMgr.agentManager.setManagedUser(appIdentifier, request.getSender(), agentState);
			}
		}
        catch (Exception e) {
        	e.printStackTrace();
        }
		
		ACLMessage registerConfirmMsg = request.createReply();
		registerConfirmMsg.setPerformative(ACLMessage.INFORM);
		
		return registerConfirmMsg;
	}
}
