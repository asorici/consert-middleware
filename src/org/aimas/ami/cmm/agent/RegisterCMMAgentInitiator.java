package org.aimas.ami.cmm.agent;

import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import org.aimas.ami.cmm.agent.onto.RegisterCMMAgent;
import org.aimas.ami.cmm.agent.onto.impl.DefaultRegisterCMMAgent;

public class RegisterCMMAgentInitiator extends SimpleAchieveREInitiator {
    private static final long serialVersionUID = -5365253149880996392L;
    
    private CMMAgent cmmAgent;
    
	public RegisterCMMAgentInitiator(CMMAgent agent) {
		super(agent, null);
		this.cmmAgent = agent;
	}
	
	@Override
	protected ACLMessage prepareRequest(ACLMessage msg) {
		ACLMessage registerRequest = new ACLMessage(ACLMessage.REQUEST);
		registerRequest.setLanguage(CMMAgent.cmmCodec.getName());
		registerRequest.setOntology(CMMAgent.cmmOntology.getName());
		
		String conversationId = cmmAgent.getName() + "-RegisterCMMAgent-" + System.currentTimeMillis();
		registerRequest.setConversationId(conversationId);
		
		registerRequest.addReceiver(cmmAgent.assignedOrgMgr);
		
		RegisterCMMAgent registerContent = new DefaultRegisterCMMAgent();
		registerContent.setAgentService(cmmAgent.getAgentType().getServiceName());
		registerContent.setAppIdentifier(cmmAgent.getAppIdentifier());
		registerContent.setAgentActive(true);
		
		try {
			Action registerAction = new Action(cmmAgent.getAID(), registerContent);
			cmmAgent.getContentManager().fillContent(registerRequest, registerAction);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		
		return registerRequest;
	}
	
	@Override
	protected void handleNotUnderstood(ACLMessage msg){}
	
	@Override
	protected void handleRefuse(ACLMessage msg){}
	
	@Override
	protected void handleInform(ACLMessage msg) {
		// TODO: see if we do something once we receive confirmation. For now we assume
		// this always succeeds and there's no reason in keeping any score.
	}
}
