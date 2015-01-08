package org.aimas.ami.cmm.agent.orgmgr;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREResponder;

import java.util.List;

import org.aimas.ami.cmm.agent.onto.QueryBase;
import org.aimas.ami.cmm.agent.onto.QueryBaseItem;
import org.aimas.ami.cmm.agent.onto.ResolveQueryBase;
import org.aimas.ami.cmm.agent.onto.UserQuery;
import org.aimas.ami.cmm.agent.onto.impl.DefaultQueryBase;

public class ResolveQueryBaseResponder extends AchieveREResponder {
    private static final long serialVersionUID = -3600787596799231557L;

	@SuppressWarnings("serial")
    private static MessageTemplate prepareTemplate(final OrgMgr orgMgr) {
		return new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
				// We make a pure content match
				try {
	                Action contentAction = (Action)orgMgr.getContentManager().extractContent(msg);
	                return contentAction.getAction() instanceof ResolveQueryBase;
				}
                catch (Exception e) {
	                //e.printStackTrace();
                	return false;
                }
			}
		});
	}
	
	private OrgMgr orgMgr;
	
	public ResolveQueryBaseResponder(OrgMgr orgMgr) {
	    super(orgMgr, prepareTemplate(orgMgr));
	    this.orgMgr = orgMgr;
	}
	
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		response = request.createReply();
		response.setPerformative(ACLMessage.INFORM);
		
		try {
            Action contentAction = (Action)orgMgr.getContentManager().extractContent(request);
            ResolveQueryBase queryBaseRequest = (ResolveQueryBase)contentAction.getAction();
            
            UserQuery queryDesc = queryBaseRequest.getQuery();
            AID receivedFromAgent = queryBaseRequest.getReceivedFromAgent();
            
            List<QueryBaseItem> queryBaseItems = orgMgr.domainManager.resolveQueryBase(queryDesc, receivedFromAgent);
            if (queryBaseItems.isEmpty()) {
            	throw new FailureException("No query handlers available for requested domain limits.");
            }
            
            QueryBase queryBase = new DefaultQueryBase();
            for (QueryBaseItem item : queryBaseItems) {
            	queryBase.addBaseItem(item);
            }
            
            orgMgr.getContentManager().fillContent(response, queryBase);
            return response;
		}
        catch (Exception e) {
            e.printStackTrace();
            throw new FailureException(e.getMessage());
        }
	}
}
