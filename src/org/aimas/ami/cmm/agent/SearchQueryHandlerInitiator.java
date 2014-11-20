package org.aimas.ami.cmm.agent;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import org.aimas.ami.cmm.agent.onto.FoundQueryHandlerAgent;
import org.aimas.ami.cmm.agent.onto.SearchQueryHandlerAgent;
import org.aimas.ami.cmm.agent.onto.impl.DefaultSearchQueryHandlerAgent;

public class SearchQueryHandlerInitiator extends SimpleAchieveREInitiator {
    private static final long serialVersionUID = -976705902275054320L;
	
    public static interface SearchQueryHandlerListener {
    	public void queryHandlerAgentFound(AID agentAID);
    	
    	public void queryHandlerAgentNotFound(ACLMessage msg);
    }
    
    private CMMAgent cmmAgent;
    private SearchQueryHandlerListener searchListener;
    
	public SearchQueryHandlerInitiator(CMMAgent agent, SearchQueryHandlerListener searchListener) {
		super(agent, null);
		this.cmmAgent = agent;
		this.searchListener = searchListener;
	}
	
	@Override
	protected ACLMessage prepareRequest(ACLMessage msg) {
		ACLMessage searchRequest = new ACLMessage(ACLMessage.REQUEST);
		searchRequest.setLanguage(CMMAgent.cmmCodec.getName());
		searchRequest.setOntology(CMMAgent.cmmOntology.getName());
		
		String conversationId = cmmAgent.getName() + "-SearchQueryHandlerAgent-" + System.currentTimeMillis();
		searchRequest.setConversationId(conversationId);
		
		if (cmmAgent.assignedOrgMgr != null) {
			searchRequest.addReceiver(cmmAgent.assignedOrgMgr);
		}
		else {
			searchRequest.addReceiver(cmmAgent.localOrgMgr);
		}
		
		SearchQueryHandlerAgent searchContent = new DefaultSearchQueryHandlerAgent();
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
		searchListener.queryHandlerAgentNotFound(msg);
	}
	
	@Override
	protected void handleFailure(ACLMessage msg) {
		searchListener.queryHandlerAgentNotFound(msg);
	}
	
	@Override
	protected void handleRefuse(ACLMessage msg) {
		searchListener.queryHandlerAgentNotFound(msg);
	}
	
	@Override
	protected void handleInform(ACLMessage msg) {
		AID agentAID = null;
		
		try {
			FoundQueryHandlerAgent agentFoundContent = (FoundQueryHandlerAgent)myAgent.getContentManager().extractContent(msg);
			agentAID = agentFoundContent.getAgent();
			
			searchListener.queryHandlerAgentFound(agentAID);
		}
		catch (Exception e) {
        	e.printStackTrace();
        }
	}
}
