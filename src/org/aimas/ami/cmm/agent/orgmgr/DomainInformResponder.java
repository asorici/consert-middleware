package org.aimas.ami.cmm.agent.orgmgr;

import jade.content.onto.basic.Action;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import org.aimas.ami.cmm.agent.onto.ContextDomain;
import org.aimas.ami.cmm.agent.onto.DomainDescription;
import org.aimas.ami.cmm.agent.onto.InformDomain;
import org.aimas.ami.cmm.agent.onto.impl.DefaultContextDomain;
import org.aimas.ami.cmm.agent.onto.impl.DefaultDomainDescription;

public class DomainInformResponder extends AchieveREResponder {
    private static final long serialVersionUID = -4938065778483291585L;

	@SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final OrgMgr orgMgr) {
		return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// We make a pure content match
				try {
	                Action contentAction = (Action)orgMgr.getContentManager().extractContent(msg);
	                return contentAction.getAction() instanceof InformDomain;
				}
                catch (Exception e) {
	                //e.printStackTrace();
                	return false;
                }
			}
		});
	}
	
	public DomainInformResponder(OrgMgr orgMgr) {
	    super(orgMgr, prepareTemplate(orgMgr));
    }
	
	@Override
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		OrgMgr orgMgr = (OrgMgr)myAgent;
		if (!orgMgr.domainManager.hasDimensionalityConfig()) {
			throw new RefuseException("No ContextDomain configuration exists.");
		}
		
		return null;
	}
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		OrgMgr orgMgr = (OrgMgr)myAgent;
		
		try {
			Action contentAction = (Action)orgMgr.getContentManager().extractContent(request);
			InformDomain requestContent = (InformDomain) contentAction.getAction();
		}
        catch (Exception e) {
        	e.printStackTrace();
        }
		
		ACLMessage domainInformMsg = request.createReply();
		domainInformMsg.setPerformative(ACLMessage.INFORM);
		
		DomainDescription domainDesc = new DefaultDomainDescription();
		ContextDomain domain = new DefaultContextDomain();
		domain.setDomainDimension(orgMgr.appSpecification.getLocalContextDomain().getDomainDimension().getURI());
		domain.setDomainEntity(orgMgr.appSpecification.getLocalContextDomain().getDomainEntity().getURI());
		domain.setDomainValue(orgMgr.appSpecification.getLocalContextDomain().getDomainValue().getURI());
		domainDesc.setDomain(domain);
		
		// Set also the coordinator and query responder for this context domain.
		domainDesc.setCoordinator(orgMgr.agentManager.getManagedCoordinator(orgMgr.appSpecification.getAppIdentifier()).getAgentAID());
		domainDesc.setQueryHandler(orgMgr.agentManager.getManagedQueryHandler(orgMgr.appSpecification.getAppIdentifier()).getAgentAID());
		
		try {
	        orgMgr.getContentManager().fillContent(domainInformMsg, domainDesc);
        }
        catch (Exception e) {}
		
		return domainInformMsg;
	}
}
