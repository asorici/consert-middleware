package org.aimas.ami.cmm.agent.queryhandler;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SenderBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SubscriptionInitiator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aimas.ami.cmm.agent.onto.QueryBase;
import org.aimas.ami.cmm.agent.onto.QueryBaseItem;
import org.aimas.ami.cmm.agent.onto.ResolveQueryBase;
import org.aimas.ami.cmm.agent.onto.UserQuery;
import org.aimas.ami.cmm.agent.onto.impl.DefaultResolveQueryBase;
import org.aimas.ami.cmm.agent.queryhandler.UserQueryHandler.UserQueryWrapper;

public class DomainSubscriptionBehaviour extends SequentialBehaviour {
	
    private static final long serialVersionUID = -8741454835506283809L;
    
    private static final String QUERY_BASE_ITEMS_KEY = "query-base-items";
    
    private static final int DOMAIN_SUBSCRIPTION_FAILURE = -1;
    private static final int DOMAIN_SUBSCRIPTION_SUCCESS =  0;
    
    private CtxQueryHandler ctxQueryAgent;
    private UserQueryHandler userQueryHandler;
    private UserQueryWrapper domainQuery;
    
    private MakeSubscriptionsBehaviour subscriptionsReceiverBehaviour;
    
	public DomainSubscriptionBehaviour(CtxQueryHandler queryAgent, UserQueryHandler userQueryHandler,  UserQueryWrapper domainQuery) {
		this.ctxQueryAgent = queryAgent;
		this.userQueryHandler = userQueryHandler;
		this.domainQuery = domainQuery;
		
		addSubBehaviour(new GetQueryBaseInitiator(queryAgent, queryAgent.getAssignedOrgManager()));
		
		subscriptionsReceiverBehaviour = new MakeSubscriptionsBehaviour(queryAgent); 
		addSubBehaviour(subscriptionsReceiverBehaviour);
	}
	
	@Override
	protected boolean checkTermination(boolean currentDone, int currentResult) {
		boolean shouldFinish = super.checkTermination(currentDone, currentResult);
		
		if (!shouldFinish) {
			if (currentDone && currentResult == DOMAIN_SUBSCRIPTION_FAILURE) {
				// we can stop in advance since one of our agents has failed to confirm initialization
				shouldFinish = true;
			}
		}
		
		return shouldFinish;
	}
	
	public void cancel() {
		subscriptionsReceiverBehaviour.cancelSubscriptions();
	}

	// Behaviors in the sequence
	/////////////////////////////////////////////////////////////////////////////////////////
	private class GetQueryBaseInitiator extends SimpleAchieveREInitiator {
        private static final long serialVersionUID = 1L;
        
		private AID orgMgr;
		private boolean success = true;
		
		public GetQueryBaseInitiator(Agent a, AID orgMgr) {
	        super(a, null);
	        this.orgMgr = orgMgr;
        }
		
		@Override
		protected ACLMessage prepareRequest(ACLMessage msg) {
			msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(orgMgr);
			
			String conversationId = "ResolveQueryBase" + "-" + ctxQueryAgent.getName() + "-" + System.currentTimeMillis();
			msg.setConversationId(conversationId);
			
			ResolveQueryBase requestContent = new DefaultResolveQueryBase();
			requestContent.setQuery(domainQuery.getQueryDescription());
			requestContent.setReceivedFromAgent(domainQuery.getSender());
			
			Action requestAction = new Action(orgMgr, requestContent);
			
			try {
		        ctxQueryAgent.getContentManager().fillContent(msg, requestAction);
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
		        QueryBase queryBase = (QueryBase)ctxQueryAgent.getContentManager().extractContent(msg);
		        getParent().getDataStore().put(QUERY_BASE_ITEMS_KEY, queryBase);
			}
	        catch (Exception e) {
		        e.printStackTrace();
		        success = false;
	        }
		}
		
		
		protected void handleFailure(ACLMessage msg) {
			// It means the OrgMgr could not help us find the query base, so we report a failure
			success = false;
		}
		
		
		@Override
		public int onEnd() {
			if (success) 
				return DOMAIN_SUBSCRIPTION_SUCCESS;
			
			return DOMAIN_SUBSCRIPTION_FAILURE;
		}
	}
	
	
	// ======================================================================================
	private class MakeSubscriptionsBehaviour extends SimpleBehaviour {
        private static final long serialVersionUID = 1L;
        
        private static final int SEND_SUBSCRIBE_QUERIES 	= 0;
        private static final int SUBSCRIBE_QUERIES_SENT 	= 1;
        private static final int CANCEL_SUBCSCRIBE_QUERIES	= 2;
        
        private int state = SEND_SUBSCRIBE_QUERIES;
        private boolean finished = false;
        
    	private Map<AID, DomainSubscribeWrapper> domainSubscriptionsMap;
    	
        public MakeSubscriptionsBehaviour(Agent a) {
        	super(a);
        	this.domainSubscriptionsMap = new HashMap<AID, DomainSubscribeWrapper>();
        }
        
        public void cancelSubscriptions() {
        	// change the state
        	state = CANCEL_SUBCSCRIBE_QUERIES;
        	
        	// awake the behaviour if it is blocked
        	if (!this.isRunnable()) {
        		restart();
        	}
        }
        
		@Override
        public void action() {
			switch(state) {
				case SEND_SUBSCRIBE_QUERIES: {
					QueryBase queryBase = (QueryBase)getParent().getDataStore().get(QUERY_BASE_ITEMS_KEY);
					for (int i = 0; i < queryBase.getBaseItems().size(); i++) {
						QueryBaseItem queryBaseItem = (QueryBaseItem)queryBase.getBaseItems().get(i);
						AID forwardQueryHandler = queryBaseItem.getQueryHandler();
					
						if (forwardQueryHandler.equals(ctxQueryAgent.getAID())) {
							DomainSubscribeWrapper subscribeWrapper = new DomainSubscribeWrapper(true, 
									forwardQueryHandler, domainQuery.getIdentifier(), null);
							domainSubscriptionsMap.put(ctxQueryAgent.getAID(), subscribeWrapper);
							
							// Register the subscription with the local UserQueryHandler and mark it also in
							// the CONSERT Engine
							userQueryHandler.registeredSubscriptions.put(domainQuery.getIdentifier(), domainQuery);
							userQueryHandler.pendingQueries.put(domainQuery.getIdentifier(), domainQuery);
							userQueryHandler.manager.getEngineQueryAdaptor().registerSubscription(domainQuery.getQueryContent());
							
							domainQuery.executeQuery();
						}
						else if (!forwardQueryHandler.equals(domainQuery.getSender())) {
							// If we have to forward it to another queryHandler then just create the appropriate message
							String forwardUpperDomainURI = queryBaseItem.getQueryUpperBound();
							String forwardLowerDomainURI = queryBaseItem.getQueryLowerBound();
							
							ACLMessage forwardedSubs = copyRequestStrucutre(domainQuery.getInitialMessage());
							forwardedSubs.addReceiver(forwardQueryHandler);
							
							UserQuery forwardedQueryDesc = domainQuery.getQueryDescription();
							forwardedQueryDesc.setDomain_upper_bound(forwardUpperDomainURI);
							forwardedQueryDesc.setDomain_lower_bound(forwardLowerDomainURI);
							
							try {
					            ctxQueryAgent.getContentManager().fillContent(forwardedSubs, forwardedQueryDesc);
							}
				            catch (Exception e) {
				            	e.printStackTrace();
				            }
							
							// Add the record of this subscription
							DomainSubscribeInitiator forwardSubsInitiator = new DomainSubscribeInitiator(ctxQueryAgent, forwardedSubs); 
							DomainSubscribeWrapper subscribeWrapper = new DomainSubscribeWrapper(false, 
									forwardQueryHandler, domainQuery.getIdentifier(), forwardSubsInitiator);
							domainSubscriptionsMap.put(ctxQueryAgent.getAID(), subscribeWrapper);
							
							// Start the behaviour
							ctxQueryAgent.addBehaviour(forwardSubsInitiator);
						}
					}
					
					// After sending the subscription forward messages we block, waiting for the cancellation of
					// this subscription
					state = SUBSCRIBE_QUERIES_SENT;
					block();
					
					break;
				}
				case SUBSCRIBE_QUERIES_SENT: {
					// After having sent the subscription forward messages we block, waiting for the cancellation of
					// this subscription
					block();
					break;
				}
				case CANCEL_SUBCSCRIBE_QUERIES: {
					for (DomainSubscribeWrapper wrapper : domainSubscriptionsMap.values()) {
						wrapper.cancel();
					}
					
					finished = true;
					break;
				}
			}
        }
		
		private ACLMessage copyRequestStrucutre(ACLMessage initialMessage) {
	        ACLMessage msg = new ACLMessage(initialMessage.getPerformative());
	        msg.setProtocol(initialMessage.getProtocol());
	        
	        msg.setConversationId(initialMessage.getConversationId());
	        msg.setReplyWith(initialMessage.getReplyWith());
	        
	        msg.setLanguage(initialMessage.getLanguage());
	    	msg.setOntology(initialMessage.getOntology());
	    	
	    	// TODO: CONSIDER ADDING A TIMEOUT FOR THE FORWARDED QUERIES - this timeout
	    	// should depend on an estimate of the number of hops until the "destination".
	    	// The destination is not necessarily the next CtxQueryHandler, but the final
	    	// domain that the query needs to reach in order to get an answer.
	    	
	    	return msg;
        }
		
		@Override
        public boolean done() {
	        return finished;
        }
	}
	
	// ======================================================================================
	private class DomainSubscribeInitiator extends SubscriptionInitiator {
        private static final long serialVersionUID = 1L;

		public DomainSubscribeInitiator(Agent a, ACLMessage msg) {
	        super(a, msg);
        }
		
		@Override
		protected void handleInform(ACLMessage msg) {
			// Here we just forward the message
			ctxQueryAgent.addBehaviour(new SenderBehaviour(ctxQueryAgent, msg));
		}
		
		@Override
		protected void handleFailure(ACLMessage msg) {
			// Here we just forward the message
			ctxQueryAgent.addBehaviour(new SenderBehaviour(ctxQueryAgent, msg));
		}
		
		// TODO: well this is not right. Forwarded subscriptions must be aggregated in some way, because
		// like this I can not properly send errors, or I will mix errors with informs (especially in 
		// multi-domain subscriptions). 
	}
	
	// ======================================================================================
	private class DomainSubscribeWrapper {
		
		private boolean local = false;
		private String subscriptionIdentifier;
		private AID responder;
		private DomainSubscribeInitiator forwardSubscribeInitiator;
		
		
		public DomainSubscribeWrapper(boolean local, AID responder, String subscriptionIdentifier, 
				DomainSubscribeInitiator forwardSubscriptionBehaviour) {
	        
			this.local = local;
			this.responder = responder;
			this.subscriptionIdentifier = subscriptionIdentifier;
			this.forwardSubscribeInitiator = forwardSubscriptionBehaviour;
	        
        }
		
		public void cancel() {
			if (local) {
				userQueryHandler.pendingQueries.remove(subscriptionIdentifier);
				userQueryHandler.registeredSubscriptions.remove(subscriptionIdentifier);
				userQueryHandler.manager.getEngineQueryAdaptor().unregisterSubscription(domainQuery.getQueryContent());
				
			    for (List<String> subsIdentifiers : userQueryHandler.subscriptionBodyMap.values()) {
			    	subsIdentifiers.remove(subscriptionIdentifier);
			    }
			}
			else {
				if (forwardSubscribeInitiator != null) {
					forwardSubscribeInitiator.cancel(responder, false);
				}
			}
		}
	}
}
