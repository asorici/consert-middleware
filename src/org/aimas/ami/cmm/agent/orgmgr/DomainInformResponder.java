package org.aimas.ami.cmm.agent.orgmgr;

import jade.content.ContentElement;
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

	private static MessageTemplate prepareTemplate(final OrgMgr orgMgr) {
		return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// We make a pure content match
				try {
	                ContentElement ce = orgMgr.getContentManager().extractContent(msg);
	                return ce instanceof InformDomain;
				}
                catch (Exception e) {
	                return false;
                }
			}
		});
	}
	
	public DomainInformResponder(OrgMgr orgMgr) {
	    super(orgMgr, prepareTemplate(orgMgr));
    }
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		OrgMgr orgMgr = (OrgMgr)myAgent;
		
		// extract the appIdentifier - TODO: this is to be used later to get the context domain
		// based on the application the CMMAgent is interested in, given that the  OrgMgr can
		// manage several
		String appIdentifier = null;
		try {
	        InformDomain requestContent = (InformDomain) orgMgr.getContentManager().extractContent(request);
	        appIdentifier = requestContent.getAppIdentifier();
		}
        catch (Exception e) {}
		
		ACLMessage domainInformMsg = request.createReply();
		domainInformMsg.setPerformative(ACLMessage.INFORM);
		
		DomainDescription domainDesc = new DefaultDomainDescription();
		ContextDomain domain = new DefaultContextDomain();
		domain.setDomainDimension(orgMgr.appSpecification.getLocalContextDomain().getDomainDimension().getURI());
		domain.setDomainEntity(orgMgr.appSpecification.getLocalContextDomain().getDomainEntity().getURI());
		domain.setDomainValue(orgMgr.appSpecification.getLocalContextDomain().getDomainValue().getURI());
		domainDesc.setDomain(domain);
		
		try {
	        orgMgr.getContentManager().fillContent(domainInformMsg, domainDesc);
        }
        catch (Exception e) {}
		
		return domainInformMsg;
	}
}
