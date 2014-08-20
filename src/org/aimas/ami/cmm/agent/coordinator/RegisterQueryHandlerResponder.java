package org.aimas.ami.cmm.agent.coordinator;

import jade.content.ContentElement;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.QueryHandlerPresent;

public class RegisterQueryHandlerResponder extends AchieveREResponder {
    private static final long serialVersionUID = 611534667671517221L;
	
    private CtxCoord coordAgent;
	
	@SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final CtxCoord coordAgent) {
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
	                ContentElement ce = coordAgent.getContentManager().extractContent(msg);
	                if (ce == null || !(ce instanceof QueryHandlerPresent)) 
	                	return false;
				}
                catch (Exception e) {
                	return false;
                }
				
				return true;
			}
		});
    }
    
	public RegisterQueryHandlerResponder(CtxCoord coordAgent) {
		super(coordAgent, prepareTemplate(coordAgent));
		this.coordAgent = coordAgent;
	}
	
	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
		// Register the QueryHandler with the QueryHandlerManager
		try {
	        QueryHandlerPresent presence = (QueryHandlerPresent)coordAgent.getContentManager().extractContent(request);
	        coordAgent.getQueryHandlerManager().registerQueryHandler(request.getSender(), presence.getIsPrimary());
		}
        catch (Exception e) {}
		
		return null;
	}
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) 
			throws FailureException {
		// For now we only send an acknowledge message. This method will get more complicated only when
		// we will consider replication of CtxQueryHandlers together with the CONSERT Engine copy
		ACLMessage result = request.createReply();
		result.setPerformative(ACLMessage.INFORM);
		
		return result;
	}
}
