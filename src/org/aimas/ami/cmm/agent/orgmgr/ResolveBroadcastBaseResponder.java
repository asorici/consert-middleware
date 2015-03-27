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

import java.util.List;

import org.aimas.ami.cmm.agent.onto.BroadcastBase;
import org.aimas.ami.cmm.agent.onto.BroadcastBaseItem;
import org.aimas.ami.cmm.agent.onto.ResolveBroadcastBase;
import org.aimas.ami.cmm.agent.onto.impl.DefaultBroadcastBase;

public class ResolveBroadcastBaseResponder extends AchieveREResponder {
    private static final long serialVersionUID = -3600787596799231557L;

	@SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final OrgMgr orgMgr) {
		return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// We make a pure content match
				try {
	                Action contentAction = (Action)orgMgr.getContentManager().extractContent(msg);
	                return contentAction.getAction() instanceof ResolveBroadcastBase;
				}
                catch (Exception e) {
	                //e.printStackTrace();
                	return false;
                }
			}
		});
	}
	
	private OrgMgr orgMgr;
	
	public ResolveBroadcastBaseResponder(OrgMgr orgMgr) {
	    super(orgMgr, prepareTemplate(orgMgr));
	    this.orgMgr = orgMgr;
	}
	
	@Override
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		return null;
	}
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		response = request.createReply();
		response.setPerformative(ACLMessage.INFORM);
		
		try {
            Action contentAction = (Action)orgMgr.getContentManager().extractContent(request);
            ResolveBroadcastBase broadcastBaseRequest = (ResolveBroadcastBase)contentAction.getAction();
            
            String domainLowerBoundType = broadcastBaseRequest.getBroadcastLowerBound();
            String domainUpperBoundType = broadcastBaseRequest.getBroadcastUpperBound();
            AID receivedFromAgent = broadcastBaseRequest.getReceivedFromAgent();
            
            List<BroadcastBaseItem> broadcastBaseItems = orgMgr.domainManager.resolveDomainRangeBroadcastBase(domainLowerBoundType, domainUpperBoundType, receivedFromAgent);
            if (broadcastBaseItems.isEmpty()) {
            	throw new FailureException("No query handlers available for requested domain limits.");
            }
            
            BroadcastBase broadcastBase = new DefaultBroadcastBase();
            for (BroadcastBaseItem item : broadcastBaseItems) {
            	broadcastBase.addBroadcastBaseItem(item);
            }
            
            orgMgr.getContentManager().fillContent(response, broadcastBase);
            return response;
		}
        catch (Exception e) {
            e.printStackTrace();
            throw new FailureException(e.getMessage());
        }
	}
}
