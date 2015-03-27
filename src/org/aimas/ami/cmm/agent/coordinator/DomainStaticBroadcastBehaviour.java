package org.aimas.ami.cmm.agent.coordinator;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.SimpleAchieveREInitiator;

import java.util.Vector;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.BroadcastBase;
import org.aimas.ami.cmm.agent.onto.BroadcastBaseItem;
import org.aimas.ami.cmm.agent.onto.ResolveBroadcastBase;
import org.aimas.ami.cmm.agent.onto.UpdateEntityDescriptions;
import org.aimas.ami.cmm.agent.onto.impl.DefaultResolveBroadcastBase;
import org.aimas.ami.cmm.agent.onto.impl.DefaultUpdateEntityDescriptions;

public class DomainStaticBroadcastBehaviour extends SequentialBehaviour {
	
    private static final long serialVersionUID = -8741454835506283809L;
    
    private static final String BROADCAST_BASE_ITEMS_KEY = "broadcast-base-items";
    
    private static final int BROADCAST_BASE_EMPTY =    -1;
    private static final int BROADCAST_BASE_EXISTS =  	1;
    
    private CtxCoord ctxCoordinator;
    private UpdateEntityDescriptions staticDomainBroadcast;
    private AID receivedFromAgent;
    
	public DomainStaticBroadcastBehaviour(CtxCoord coordAgent, UpdateEntityDescriptions staticDomainBroadcast, AID receivedFromAgent) {
		this.ctxCoordinator = coordAgent;
		this.staticDomainBroadcast = staticDomainBroadcast;
		this.receivedFromAgent = receivedFromAgent;
		
		addSubBehaviour(new GetBroadcastBaseInitiator());
		addSubBehaviour(new ForwardBroadcastInitiator());
	}
	
	@Override
	protected boolean checkTermination(boolean currentDone, int currentResult) {
		boolean shouldFinish = super.checkTermination(currentDone, currentResult);
		
		if (!shouldFinish) {
			if (currentDone && currentResult == BROADCAST_BASE_EMPTY) {
				// we can stop in advance since there is no broadcast base, therefore we do not need to forward anything
				shouldFinish = true;
			}
		}
		
		return shouldFinish;
	}
	
	
	// Behaviors in the sequence
	/////////////////////////////////////////////////////////////////////////////////////////
	private class GetBroadcastBaseInitiator extends SimpleAchieveREInitiator {
        private static final long serialVersionUID = 1L;
        
		private AID orgMgr;
		private boolean baseExists = true;
		
		public GetBroadcastBaseInitiator() {
	        super(ctxCoordinator, null);
	        this.orgMgr = ctxCoordinator.getAssignedOrgManager();
        }
		
		@Override
		protected ACLMessage prepareRequest(ACLMessage msg) {
			msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(orgMgr);
			msg.setOntology(CMMAgent.cmmOntology.getName());
			msg.setLanguage(CMMAgent.cmmCodec.getName());
			
			String conversationId = "ResolveBroadcastBase" + "-" + ctxCoordinator.getName() + "-" + System.currentTimeMillis();
			msg.setConversationId(conversationId);
			
			ResolveBroadcastBase requestContent = new DefaultResolveBroadcastBase();
			requestContent.setReceivedFromAgent(receivedFromAgent);
			requestContent.setBroadcastLowerBound(staticDomainBroadcast.getDomain_lower_bound());
			requestContent.setBroadcastUpperBound(staticDomainBroadcast.getDomain_upper_bound());
			
			Action requestAction = new Action(orgMgr, requestContent);
			try {
		        ctxCoordinator.getContentManager().fillContent(msg, requestAction);
	        }
	        catch (Exception e) {
		        e.printStackTrace();
	        }
			
			return msg;
		}
		
		protected void handleInform(ACLMessage msg) {
			// The returned message is a QueryBase predicate which gives us the list of CtxQueryHandlers to which
			// we have to forward the query. In this method we only have to save it in the parent behavior datastore 
			// for the next behavior in the sequence to retrieve.
			try {
		        BroadcastBase broadcastBase = (BroadcastBase)ctxCoordinator.getContentManager().extractContent(msg);
		        getParent().getDataStore().put(BROADCAST_BASE_ITEMS_KEY, broadcastBase);
		        
		        if (broadcastBase.getBroadcastBaseItem().size() == 0) {
		        	baseExists = false;
		        }
			}
	        catch (Exception e) {
		        e.printStackTrace();
		        baseExists = false;
	        }
		}
		
		
		protected void handleFailure(ACLMessage msg) {
			// It means the OrgMgr could not help us find the query base, so we report a failure
			baseExists = false;
		}
		
		
		@Override
		public int onEnd() {
			if (baseExists) 
				return BROADCAST_BASE_EXISTS;
			
			return BROADCAST_BASE_EMPTY;
		}
	}
	
	
	// ======================================================================================
	private class ForwardBroadcastInitiator extends AchieveREInitiator {
        private static final long serialVersionUID = 1L;

		public ForwardBroadcastInitiator() {
	        super(ctxCoordinator, null);
        }
		
		@Override
		protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
			// If we do not have a broadcast base, just return null to finish the initiator behavior
			BroadcastBase broadcastBase = (BroadcastBase)getParent().getDataStore().get(BROADCAST_BASE_ITEMS_KEY);
			if (broadcastBase == null || broadcastBase.getBroadcastBaseItem().isEmpty()) {
				return null;
			}
			
			// Otherwise we will iterate through the responses, see if we have to do anything ourselves and forward the rest
			Vector<ACLMessage> forwardMsgVector = new Vector<ACLMessage>();
			
			for (int i = 0; i < broadcastBase.getBroadcastBaseItem().size(); i++) {
				BroadcastBaseItem baseItem = (BroadcastBaseItem)broadcastBase.getBroadcastBaseItem().get(i);
				
				if (baseItem.getCoordinator().equals(ctxCoordinator)) {
					// If this is for my coordinator, insert locally
					// That is, we create a duplicate UpdateProfiledAssertion request, with no domain bounds
					UpdateEntityDescriptions entityDescriptionsUpdate = new DefaultUpdateEntityDescriptions();
					entityDescriptionsUpdate.setEntityContents(staticDomainBroadcast.getEntityContents());
					
					ctxCoordinator.getContextUpdateManager().updateEntityDescriptions(receivedFromAgent, entityDescriptionsUpdate);
				}
				else {
					// Otherwise, this message needs to be forwarded
					msg = new ACLMessage(ACLMessage.REQUEST);
					msg.addReceiver(baseItem.getCoordinator());
					msg.setOntology(CMMAgent.cmmOntology.getName());
					msg.setLanguage(CMMAgent.cmmCodec.getName());
					
					String conversationId = "ForwardProfiledBroadcast" + "-" + ctxCoordinator.getName() + "-" + System.currentTimeMillis();
					msg.setConversationId(conversationId);
					
					try {
						Action forwardAction = new Action(baseItem.getCoordinator(), staticDomainBroadcast);
						ctxCoordinator.getContentManager().fillContent(msg, forwardAction);
			        }
			        catch (Exception e) {
			        	e.printStackTrace();
			        }
					
					forwardMsgVector.add(msg);
				}
			}
			
			return forwardMsgVector;
		}
		
		@Override
		protected void handleAllResponses(Vector responses) {
			// nothing to do for now, because we do not react to broadcast forwarding success
		}
	}
	
	
}
