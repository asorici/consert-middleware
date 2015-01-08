package org.aimas.ami.cmm.agent.orgmgr;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.ContextDomain;
import org.aimas.ami.cmm.agent.onto.DomainDescription;
import org.aimas.ami.cmm.agent.onto.RegisterManager;
import org.aimas.ami.cmm.agent.onto.impl.DefaultContextDomain;
import org.aimas.ami.cmm.agent.onto.impl.DefaultDomainDescription;

public class RegisterManagerResponder extends AchieveREResponder {
    private static final long serialVersionUID = 9019614671292587384L;
    
    @SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final OrgMgr orgMgr) {
	    return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				if (msg == null) {
					return false;
				}
				
				if (msg.getPerformative() != ACLMessage.REQUEST) 
					return false;
				
				if (!msg.getOntology().equals(CMMAgent.cmmOntology.getName())) 
					return false;
				
				try {
	                Action contentAction = (Action)orgMgr.getContentManager().extractContent(msg);
	                if (contentAction == null || !(contentAction.getAction() instanceof RegisterManager)) 
	                	return false;
				}
                catch (Exception e) {
                	return false;
                }
				
				return true;
			}
		});
    }
    
    
    private OrgMgr orgMgr; 
    
	public RegisterManagerResponder(OrgMgr orgMgr) {
		super(orgMgr, prepareTemplate(orgMgr));
		this.orgMgr = orgMgr;
	}
	
	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
		try {
			Action contentAction = (Action)orgMgr.getContentManager().extractContent(request);
			RegisterManager registrationRequest = (RegisterManager)contentAction.getAction();
			
			String domainEntityURI = registrationRequest.getDomainEntity();
			String domainValueURI = registrationRequest.getDomainValue();
			AID mgrAgent = registrationRequest.getAgent();
			AID domainQueryResponder = registrationRequest.getQueryHandler();
			String relationType = registrationRequest.getRelationType();
			
			if (relationType.equals(RegisterManager.CHILD)) {
				orgMgr.domainManager.registerChildManager(domainEntityURI, domainValueURI, mgrAgent, domainQueryResponder);
			}
			else if (relationType.equals(RegisterManager.ROOT)) {
				orgMgr.domainManager.registerRootManager(domainEntityURI, domainValueURI, mgrAgent, domainQueryResponder);
			}
		}
		catch(Exception e) {
			throw new NotUnderstoodException("Child Manager registration request not understood. " + "Reason: " + e.getMessage());
		}
		
		return null;
	}
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) 
			throws FailureException {
		
		// The response to a child manager registration request is the domain info for the parent OrgMgr  
		response = request.createReply();
		response.setPerformative(ACLMessage.INFORM);
		
		DomainDescription domainDesc = new DefaultDomainDescription();
		ContextDomain domainInfo = new DefaultContextDomain();
		
		domainInfo.setDomainDimension(orgMgr.domainManager.getDomainDimension().getURI());
		domainInfo.setDomainEntity(orgMgr.domainManager.getDomainRangeEntity().getURI());
		domainInfo.setDomainValue(orgMgr.domainManager.getDomainRangeValue().getURI());
		domainDesc.setDomain(domainInfo);
		
		// Set also the coordinator and query responder for this context domain.
		domainDesc.setCoordinator(orgMgr.agentManager.getManagedCoordinator().getAgentAID());
		domainDesc.setQueryHandler(orgMgr.agentManager.getManagedQueryHandlers().get(0).getAgentAID());
		
		try {
	        orgMgr.getContentManager().fillContent(response, domainDesc);
        }
        catch (Exception e) {
	        e.printStackTrace();
	        throw new FailureException("Failed to write response to RegisterChildManager request.");
        }
		
		return response;
	}
}
