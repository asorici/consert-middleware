package org.aimas.ami.cmm.agent;

import jade.content.onto.basic.Action;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import org.aimas.ami.cmm.agent.onto.RegisterCMMAgent;
import org.aimas.ami.cmm.agent.onto.impl.DefaultRegisterCMMAgent;

public class RegisterCMMAgentInitiator extends SimpleAchieveREInitiator {
    private static final long serialVersionUID = -5365253149880996392L;
    
    public static interface RegisterCMMAgentNotifier {
    	public void cmmAgentRegistered();
    }
    
    private CMMAgent cmmAgent;
    private RegisterCMMAgentNotifier registrationNotifier;
    
	public RegisterCMMAgentInitiator(CMMAgent agent, RegisterCMMAgentNotifier registrationNotifier) {
		super(agent, null);
		this.cmmAgent = agent;
		this.registrationNotifier = registrationNotifier;
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
		registerContent.setAgentType(cmmAgent.getAgentType().getServiceName());
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
		// After we have registered with the assigned OrgMgr, call the notifier
		// his associated CtxQueryHandler and CtxCoordinator, as well as the domain data
		if (registrationNotifier != null) {
			registrationNotifier.cmmAgentRegistered();
		}
	}
}
