package org.aimas.ami.cmm.agent;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import org.aimas.ami.cmm.agent.onto.FoundCoordinatorAgent;
import org.aimas.ami.cmm.agent.onto.SearchCoordinatorAgent;
import org.aimas.ami.cmm.agent.onto.impl.DefaultSearchCoordinatorAgent;

public class SearchCoordinatorInitiator extends SimpleAchieveREInitiator {
    private static final long serialVersionUID = -976705902275054320L;
	
    public static interface SearchCoordinatorListener {
    	public void coordinatorAgentFound(AID agentAID);
    	
    	public void coordinatorAgentNotFound(ACLMessage msg);
    }
    
    private CMMAgent cmmAgent;
    private SearchCoordinatorListener searchListener;
    
	public SearchCoordinatorInitiator(CMMAgent agent, SearchCoordinatorListener searchListener) {
		super(agent, null);
		this.cmmAgent = agent;
		this.searchListener = searchListener;
	}
	
	@Override
	protected ACLMessage prepareRequest(ACLMessage msg) {
		ACLMessage searchRequest = new ACLMessage(ACLMessage.REQUEST);
		searchRequest.setLanguage(CMMAgent.cmmCodec.getName());
		searchRequest.setOntology(CMMAgent.cmmOntology.getName());
		
		String conversationId = cmmAgent.getName() + "-SearchCoordinatorAgent-" + System.currentTimeMillis();
		searchRequest.setConversationId(conversationId);
		
		if (cmmAgent.assignedOrgMgr != null) {
			searchRequest.addReceiver(cmmAgent.assignedOrgMgr);
		}
		else {
			searchRequest.addReceiver(cmmAgent.localOrgMgr);
		}
		
		SearchCoordinatorAgent searchContent = new DefaultSearchCoordinatorAgent();
		try {
			Action searchAction = new Action(cmmAgent.getAID(), searchContent);
			cmmAgent.getContentManager().fillContent(searchRequest, searchAction);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		
		return searchRequest;
	}
	
	@Override
	protected void handleNotUnderstood(ACLMessage msg) {
		searchListener.coordinatorAgentNotFound(msg);
	}
	
	@Override
	protected void handleFailure(ACLMessage msg) {
		searchListener.coordinatorAgentNotFound(msg);
	}
	
	@Override
	protected void handleRefuse(ACLMessage msg) {
		searchListener.coordinatorAgentNotFound(msg);
	}
	
	@Override
	protected void handleInform(ACLMessage msg) {
		AID agentAID = null;
		
		try {
			FoundCoordinatorAgent agentFoundContent = (FoundCoordinatorAgent)myAgent.getContentManager().extractContent(msg);
			agentAID = agentFoundContent.getAgent();
			
			searchListener.coordinatorAgentFound(agentAID);
		}
		catch (Exception e) {
        	e.printStackTrace();
        }
	}
}
