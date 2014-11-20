package org.aimas.ami.cmm.agent.queryhandler;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.config.QueryHandlerSpecification;
import org.aimas.ami.cmm.agent.onto.QueryHandlerPresent;
import org.aimas.ami.cmm.agent.onto.impl.DefaultQueryHandlerPresent;

public class ConnectToCoordinatorBehaviour extends SimpleAchieveREInitiator {
    private static final long serialVersionUID = 5797596671426167715L;

	public ConnectToCoordinatorBehaviour(CtxQueryHandler ctxQueryAgent) {
		super(ctxQueryAgent, null);
	}
	
	@Override
	protected ACLMessage prepareRequest(ACLMessage msg) {
		CtxQueryHandler ctxQueryAgent = (CtxQueryHandler)myAgent;
		QueryHandlerSpecification spec = (QueryHandlerSpecification)ctxQueryAgent.getAgentSpecification();
		
		ACLMessage connectRequest = new ACLMessage(ACLMessage.REQUEST);
		connectRequest.setLanguage(CMMAgent.cmmCodec.getName());
		connectRequest.setOntology(CMMAgent.cmmOntology.getName());
		
		String conversationId = ctxQueryAgent.getName() + "-QueryHandlerPresent-" + System.currentTimeMillis();
		connectRequest.setConversationId(conversationId);
		
		AID assignedCoord = spec.getAssignedManagerAddress().getAID();
		connectRequest.addReceiver(assignedCoord);
		
		QueryHandlerPresent presenceContent = new DefaultQueryHandlerPresent();
		presenceContent.setAgent(ctxQueryAgent.getAID());
		presenceContent.setIsPrimary(spec.isPrimary());
		
		try {
	        ctxQueryAgent.getContentManager().fillContent(connectRequest, presenceContent);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		
		return connectRequest;
	}
	
	@Override
	protected void handleNotUnderstood(ACLMessage msg){}
	
	@Override
	protected void handleRefuse(ACLMessage msg){}
	
	@Override
	protected void handleInform(ACLMessage msg) {
		CtxQueryHandler ctxQueryAgent = (CtxQueryHandler)myAgent;
		ctxQueryAgent.setConnectedToCoordinator(true);
	}
	
}
