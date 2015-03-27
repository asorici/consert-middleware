package org.aimas.ami.cmm.agent.orgmgr;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.DomainDescription;
import org.aimas.ami.cmm.agent.onto.RegisterManager;
import org.aimas.ami.cmm.agent.onto.impl.DefaultRegisterManager;

public class RegisterManagerInitiator extends SimpleAchieveREInitiator {
    private static final long serialVersionUID = -126197066709890854L;
    
    private OrgMgr orgMgr;
    private String relationType;
    private AID targetManager;
    
	public RegisterManagerInitiator(OrgMgr orgMgr, String relationType, AID targetManager) {
		super(orgMgr, null);
		
		this.orgMgr = orgMgr;
		this.relationType = relationType;
		this.targetManager = targetManager;
	}
	
	@Override
	protected ACLMessage prepareRequest(ACLMessage msg) {
		msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(targetManager);
		msg.setOntology(CMMAgent.cmmOntology.getName());
		msg.setLanguage(CMMAgent.cmmCodec.getName());
		
		String conversationId = "RegisterChildManager" + "-" + orgMgr.getName() + "-" + System.currentTimeMillis();
		msg.setConversationId(conversationId);
		
		RegisterManager requestContent = new DefaultRegisterManager();
		requestContent.setDomainEntity(orgMgr.domainManager.getDomainRangeEntity().getURI());
		requestContent.setDomainValue(orgMgr.domainManager.getDomainRangeValue().getURI());
		requestContent.setAgent(orgMgr.getAID());
		
		// We take query handler registered for the applicationId configured for this OrgMgr as the domain query responder
		AID domainQueryResponder = orgMgr.agentManager.getManagedQueryHandler(orgMgr.appSpecification.getAppIdentifier()).getAgentAID();
		requestContent.setQueryHandler(domainQueryResponder);
		
		// We take coordinator registered for the applicationId configured for this OrgMgr as the domain coordinator
		AID domainCoordinator = orgMgr.agentManager.getManagedCoordinator(orgMgr.appSpecification.getAppIdentifier()).getAgentAID();
		requestContent.setCoordinator(domainCoordinator);
		
		requestContent.setRelationType(relationType);
		
		Action requestAction = new Action(orgMgr.mySpecification.getParentManagerAddress().getAID(), requestContent);
		
		try {
	        orgMgr.getContentManager().fillContent(msg, requestAction);
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
		
		return msg;
	}
	
	protected void handleInform(ACLMessage msg) {
		// The returned message is a DomainDescription predicate which informs us of the domain of which
		// our parent OrgMgr is in charge.
		try {
	        DomainDescription domainDesc = (DomainDescription)orgMgr.getContentManager().extractContent(msg);
	        String domainEntityURI = domainDesc.getDomain().getDomainEntity();
	        String domainValueURI = domainDesc.getDomain().getDomainValue();
	        
	        AID domainCoordinator = domainDesc.getCoordinator();
	        AID domainQueryResponder = domainDesc.getQueryHandler();
	        
	        if (relationType.equals(RegisterManager.CHILD)) {
	        	orgMgr.domainManager.setParentManagerDomain(domainEntityURI, domainValueURI, targetManager, domainCoordinator, domainQueryResponder);
	        }
	        else if (relationType.equals(RegisterManager.ROOT)) {
	        	orgMgr.domainManager.registerRootManager(domainEntityURI, domainValueURI, targetManager, domainCoordinator, domainQueryResponder);
	        }
		}
        catch (Exception e) {
	        e.printStackTrace();
        }
	}
	
	protected void handleFailure(ACLMessage msg) {
		// TODO: not sure if anything must be done here, except maybe try again later - in an incremental backup mode
	}
}
