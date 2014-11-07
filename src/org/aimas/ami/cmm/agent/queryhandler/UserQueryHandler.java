package org.aimas.ami.cmm.agent.queryhandler;

import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.SenderBehaviour;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.config.QueryHandlerSpecification;
import org.aimas.ami.cmm.agent.onto.AssertionCapability;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.EnableAssertions;
import org.aimas.ami.cmm.agent.onto.UserQuery;
import org.aimas.ami.cmm.agent.onto.UserQueryResult;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionCapability;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionDescription;
import org.aimas.ami.cmm.agent.onto.impl.DefaultEnableAssertions;
import org.aimas.ami.cmm.agent.onto.impl.DefaultUserQueryResult;
import org.aimas.ami.contextrep.engine.api.ContextResultSet;
import org.aimas.ami.contextrep.engine.api.QueryHandler;
import org.aimas.ami.contextrep.engine.api.QueryResult;
import org.aimas.ami.contextrep.model.ContextAssertion;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class UserQueryHandler {
	
	private QueryManager manager;
	private AID user;
	
	private Map<String, UserQueryWrapper> registeredSubscriptions;
	private Map<String, UserQueryWrapper> pendingQueries;
	
	/* A mapping from a ContextAssertion to the list of subscription identifiers in which 
	 * the ContextAssertion is present */
	private Map<ContextAssertion, List<String>> subscriptionBodyMap;
	
	public UserQueryHandler(QueryManager manager, AID user) {
		this.manager = manager;
		this.user = user;
	    
	    registeredSubscriptions = new HashMap<String, UserQueryHandler.UserQueryWrapper>();
	    pendingQueries = new ConcurrentHashMap<String, UserQueryHandler.UserQueryWrapper>();
	    
	    subscriptionBodyMap = new HashMap<ContextAssertion, List<String>>();
    }
	
	QueryHandler getEngineQueryAdaptor() {
		return manager.getEngineQueryAdaptor();
	}
	
	private Set<ContextAssertion> analyzeQuery(Query query) {
		return manager.getEngineQueryAdaptor().analyzeQuery(query, null);
	}
	
	private Set<ContextAssertion> analyzeInactiveAssertions(Set<ContextAssertion> referencedAssertions) {
	    Set<ContextAssertion> inactiveAssertions = new HashSet<ContextAssertion>();
	    
	    for (ContextAssertion contextAssertion : referencedAssertions) {
	    	if (!manager.getEngineStatsAdaptor().getAssertionEnableStatus(contextAssertion.getOntologyResource()).updatesEnabled()) {
	    		inactiveAssertions.add(contextAssertion);
	    	}
	    }
	    
	    return inactiveAssertions;
    }
	
	// ====================== HANDLING ASSERTION UPDATE NOTIFICATIONS ======================= //
	////////////////////////////////////////////////////////////////////////////////////////////
	public void notifyAssertionUpdated(ContextAssertion contextAssertion) {
		List<String> subsIdentifiers = subscriptionBodyMap.get(contextAssertion);
		
		if (subsIdentifiers != null) {
			for (String identifier : subsIdentifiers) {
				if (!pendingQueries.containsKey(identifier)) {
					UserQueryWrapper queryWrapper = registeredSubscriptions.get(identifier);
					
					pendingQueries.put(identifier, queryWrapper);
					queryWrapper.executeQuery();
				}
			}
		}
	}
	
	
	// ======================== HANDLING QUERY RESULT NOTIFICATIONS ========================= //
	////////////////////////////////////////////////////////////////////////////////////////////
    public void notifyQueryResult(int queryType, String queryIdentifier, QueryResult result) {
	    // Retrieve the queryWrapper for the query we executed
    	UserQueryWrapper queryWrapper = pendingQueries.remove(queryIdentifier);
    	if (queryWrapper == null) {
    		return;
    	}
    	
    	
    	if (result.hasError()) {
    		// If we could not execute the query, we are going to return a FAILURE message performative
    		ACLMessage response = prepareQueryResponseMsg(queryWrapper.getInitialMessage(), ACLMessage.FAILURE);
    		try {
	            UserQueryResult qr = encodeResult(result);
	            manager.getCtxQueryAgent().getContentManager().fillContent(response, qr);
            }
            catch (Exception e) {
	            e.printStackTrace();
            }
    		
    		manager.getCtxQueryAgent().addBehaviour(new SenderBehaviour(manager.getCtxQueryAgent(), response));
    	}
    	else {
    		// If we treated a subscription query check to see if we have a 
    		// time-based or change-based subscription. For a change-based subscription, see if
    		// the new result differs than the cached one.
    		boolean sendInform = false;
    		if (queryWrapper.getPerformative() == ACLMessage.SUBSCRIBE && !queryWrapper.isTimeBasedUpdate()) {
    			if (result.isAsk() && queryWrapper.resultChanged(result.getAskResult())) {
    				queryWrapper.cacheAskResult(result.getAskResult());
    				sendInform = true;
    			}
    			else if (result.isSelect() && queryWrapper.resultChanged(result.getResultSet())) {
    				queryWrapper.cacheSelectResult(result.getResultSet());
    				sendInform = true;
    			}
    		}
    		
    		if (sendInform) {
	    		ACLMessage response = prepareQueryResponseMsg(queryWrapper.getInitialMessage(), ACLMessage.INFORM);
	    		try {
	    			UserQueryResult qr = encodeResult(result);
	    			manager.getCtxQueryAgent().getContentManager().fillContent(response, qr);
	    			
	    			//System.out.println(response);
	            }
	            catch (Exception e) {
		            e.printStackTrace();
	            }
	    		
	    		manager.getCtxQueryAgent().addBehaviour(new SenderBehaviour(manager.getCtxQueryAgent(), response));
    		}
    	}
    }

    
    private UserQueryResult encodeResult(QueryResult result) {
	    UserQueryResult qr = new DefaultUserQueryResult();
	    
	    // set isAsk
	    qr.setIsAsk(result.isAsk());
	    
	    // set error message
	    if (result.hasError()) {
	    	qr.setErrorMessage(result.getError().getMessage());
	    }
	    else {
	    	qr.setErrorMessage("");
	    }
	    
	    // set ask value
	    qr.setAskResult(result.getAskResult());
	    
	    // set xml-serialized result set
	    if (result.isSelect() && result.getResultSet() != null) {
	    	result.getResultSet().reset();
	    	qr.setQueryResultSet(ResultSetFormatter.asXMLString(result.getResultSet()));
	    }
	    else {
	    	qr.setQueryResultSet("");
	    }
	    
	    return qr;
    }

	ACLMessage prepareQueryResponseMsg(ACLMessage initialMessage, int performative) {
    	ACLMessage reply = new ACLMessage(performative);
    	
    	// set the receiver
    	reply.addReceiver(user);
    	
    	// Set the conversationId
    	reply.setConversationId(initialMessage.getConversationId());
    	
    	// Set the inReplyTo
    	reply.setInReplyTo(initialMessage.getReplyWith());
    	
    	// Set the Protocol.
    	reply.setProtocol(initialMessage.getProtocol());
    	
    	// Set the Language codec
    	reply.setLanguage(initialMessage.getLanguage());
    	
    	// Set the ontology
    	reply.setOntology(initialMessage.getOntology());
    	
    	// Set ReplyWith - don't think it is necessary for a QUERY RESULT MSG - the requesting agent won't
    	// send anything in response
    	//reply.setReplyWith(ctxQueryAgent.getName() + "-" +  + "-" + java.lang.System.currentTimeMillis());
    	
    	return reply;
    }
    
    
	// ================ HANDLING REGISTRATION AND CANCELATION OF USER QUERIES ================ //
    /////////////////////////////////////////////////////////////////////////////////////////////
	private UserQueryWrapper buildWrapper(ACLMessage userQueryMsg) {
		try {
	        UserQueryWrapper queryWrapper = new UserQueryWrapper(userQueryMsg);
	        return queryWrapper;
	    }
        catch (NotUnderstoodException e) {
	        ACLMessage response = prepareQueryResponseMsg(userQueryMsg, ACLMessage.NOT_UNDERSTOOD);
	        response.setContent(e.getMessage());
	        
	        manager.getCtxQueryAgent().addBehaviour(new SenderBehaviour(manager.getCtxQueryAgent(), response));
	        return null;
        }
	}
    
    
    public void executeQuery(ACLMessage userQueryMsg) {
    	UserQueryWrapper queryWrapper = buildWrapper(userQueryMsg);
    	if (queryWrapper != null) {
    		// First, analyze query and see if we need to enable anything
			Set<ContextAssertion> referencedAssertions = analyzeQuery(queryWrapper.getQueryContent());
			Set<ContextAssertion> inactiveAssertions = analyzeInactiveAssertions(referencedAssertions);
			
			if (inactiveAssertions.isEmpty()) {
				pendingQueries.put(userQueryMsg.getConversationId(), queryWrapper);
				queryWrapper.executeQuery();
			}
			else {				
				manager.getCtxQueryAgent().addBehaviour(new EnableAndExecuteBehaviour(queryWrapper, inactiveAssertions));
			}
    	}
    }


	
	public void registerSubscription(ACLMessage userQueryMsg) {
		UserQueryWrapper queryWrapper = buildWrapper(userQueryMsg);
    	if (queryWrapper != null) {
    		// First, analyze query and see if we need to enable anything
    		Set<ContextAssertion> referencedAssertions = analyzeQuery(queryWrapper.getQueryContent());
    		Set<ContextAssertion> inactiveAssertions = analyzeInactiveAssertions(referencedAssertions);
    		
    		for (ContextAssertion ca : referencedAssertions) {
    			List<String> subsIdentifiers = subscriptionBodyMap.get(ca);
    			if (subsIdentifiers == null) {
    				subsIdentifiers = new LinkedList<String>();
    				subsIdentifiers.add(userQueryMsg.getConversationId());
    				subscriptionBodyMap.put(ca, subsIdentifiers);
    			}
    			else {
    				subsIdentifiers.add(userQueryMsg.getConversationId());
    			}
    		}
    		
    		if (inactiveAssertions.isEmpty()) {
    			registeredSubscriptions.put(userQueryMsg.getConversationId(), queryWrapper);
				pendingQueries.put(userQueryMsg.getConversationId(), queryWrapper);
				queryWrapper.executeQuery();
			}
			else {				
				manager.getCtxQueryAgent().addBehaviour(new EnableAndExecuteBehaviour(queryWrapper, inactiveAssertions));
			}
    	}
    }

	
	public void cancelSubscription(ACLMessage userQueryMsg) {
	    String subscriptionIdentifier = userQueryMsg.getConversationId();
	    
	    pendingQueries.remove(subscriptionIdentifier);
	    registeredSubscriptions.remove(subscriptionIdentifier);
	    
	    for (List<String> subsIdentifiers : subscriptionBodyMap.values()) {
	    	subsIdentifiers.remove(subscriptionIdentifier);
	    }
	    
	    ACLMessage cancelConfirm = prepareQueryResponseMsg(userQueryMsg, ACLMessage.INFORM);
	    manager.getCtxQueryAgent().addBehaviour(new SenderBehaviour(manager.getCtxQueryAgent(), cancelConfirm));
    }
	
	// ========================== AUXILIARY WRAPPERS AND BEHAVIOURS ========================== //
	/////////////////////////////////////////////////////////////////////////////////////////////
	private class UserQueryWrapper {
		private ACLMessage initQueryMessage;
		private UserQuery queryDescription;
		private Query queryContent;
		
		private ContextResultSet cachedSelectResult = null;
		private boolean cachedAskResult = false;
		
		public UserQueryWrapper(ACLMessage queryMessage) throws NotUnderstoodException {
	        this.initQueryMessage = queryMessage;
	        extractUserQuery();
        }

		private void extractUserQuery() throws NotUnderstoodException {
			try {
				ContentElement ce = manager.getCtxQueryAgent().getContentManager().extractContent(initQueryMessage);
	            if (ce instanceof UserQuery) {
	            	queryDescription = (UserQuery)ce;
	            	
	            	String queryString = queryDescription.getQueryContent();
	            	queryContent = QueryFactory.create(queryString);
	            }
	            else {
	            	throw new NotUnderstoodException("Requested query not of type " + UserQuery.class.getName());
	            }
			}
            catch (Exception e) {
            	e.printStackTrace();
	            throw new NotUnderstoodException("Requested query not understood. Reason: " + e.getMessage());
            }
        }
		
		ACLMessage getInitialMessage() {
			return initQueryMessage; 
		}
		
		Query getQueryContent() {
			return queryContent;
		}
		
		int getPerformative() {
			return initQueryMessage.getPerformative();
		}
		
		String getIdentifier() {
			return initQueryMessage.getConversationId();
		}
		
		boolean isTimeBasedUpdate() {
			return queryDescription.getRepeatInterval() != 0;
		}
		
		boolean resultChanged(ContextResultSet newSelectResult) {
			if (cachedSelectResult == null) {
				return true;
			}
			
			if (newSelectResult.size() != cachedSelectResult.size()) {
				return true;
			}
			
			cachedSelectResult.reset();
			newSelectResult.reset();
			
			for (;cachedSelectResult.hasNext();) {
				Binding cachedBinding = cachedSelectResult.nextBinding();
				Binding newBinding = newSelectResult.nextBinding();
				
				if (!cachedBinding.equals(newBinding)) {
					// reset the new result set and return
					newSelectResult.reset();
					cachedSelectResult.reset();
					return true;
				}
			}
			
			// reset the new result set and return
			newSelectResult.reset();
			cachedSelectResult.reset();
			return false;
		}
		
		boolean resultChanged(boolean newAskResult) {
			return newAskResult != cachedAskResult;
		}
		
		void cacheSelectResult(ContextResultSet newResultSet) {
			cachedSelectResult = newResultSet;
		}
		
		void cacheAskResult(boolean newAskResult) {
			cachedAskResult = newAskResult;
		}
		
		/* Create a new QueryHandlerBehaviour to execute a query via the CONSERT Engine Query Adaptor */
		void executeQuery() {
			int queryType = initQueryMessage.getPerformative();
			String queryIdentifier = initQueryMessage.getConversationId();
			
			QueryExecBehaviour b = new QueryExecBehaviour(queryType, queryIdentifier, queryContent, 
					UserQueryHandler.this);
			manager.getCtxQueryAgent().addBehaviour(b);
		}
	}
	
	
	private class EnableAndExecuteBehaviour extends SimpleAchieveREInitiator {
        private static final long serialVersionUID = 1L;
        
        private UserQueryWrapper queryWrapper;
        private Set<ContextAssertion> inactiveAssertions;
        
		public EnableAndExecuteBehaviour(UserQueryWrapper queryWrapper, Set<ContextAssertion> inactiveAssertions) {
			super(manager.getCtxQueryAgent(), null);
			
			this.queryWrapper = queryWrapper;
			this.inactiveAssertions = inactiveAssertions;
		}
		
		@Override
		protected ACLMessage prepareRequest(ACLMessage msg){
			ACLMessage enableAssertionsMsg = new ACLMessage(ACLMessage.REQUEST);
			
			QueryHandlerSpecification spec = (QueryHandlerSpecification)manager.getCtxQueryAgent().getAgentSpecification();
			String conversationId = manager.getCtxQueryAgent().getName() + "-EnableAssertions-" + System.currentTimeMillis(); 
			
			enableAssertionsMsg.addReceiver(spec.getAssignedCoordinatorAddress().getAID());
			enableAssertionsMsg.setConversationId(conversationId);
			enableAssertionsMsg.setLanguage(CMMAgent.cmmCodec.getName());
			enableAssertionsMsg.setOntology(CMMAgent.cmmOntology.getName());
			
			EnableAssertions msgContent = new DefaultEnableAssertions();
			for (ContextAssertion ca : inactiveAssertions) {
				String assertionResURI = ca.getOntologyResource().getURI();
				
				AssertionDescription assertionDesc = new DefaultAssertionDescription();
				assertionDesc.setAssertionType(assertionResURI);
				
				AssertionCapability capability = new DefaultAssertionCapability();
				capability.setAssertion(assertionDesc);
				
				msgContent.addEnabledCapability(capability);
			}
			
			try {
				Action enableAssertionsAction = new Action(spec.getAssignedCoordinatorAddress().getAID(), msgContent);
	            manager.getCtxQueryAgent().getContentManager().fillContent(enableAssertionsMsg, enableAssertionsAction);
            }
            catch (Exception e) {
	            e.printStackTrace();
            }
			
			return enableAssertionsMsg;
		}
		
		@Override
		protected void handleInform(ACLMessage msg){
			// The inform message is just a confirmation that the assertions have been activated.
			// If the initial message is a SUBSCRIBE request, we can now register it and then execute
			// the query.
			if (queryWrapper.getInitialMessage().getPerformative() == ACLMessage.SUBSCRIBE ) {
				registeredSubscriptions.put(queryWrapper.getInitialMessage().getConversationId(), queryWrapper);
			}
			
			pendingQueries.put(queryWrapper.getInitialMessage().getConversationId(), queryWrapper);
			queryWrapper.executeQuery();
		}
		
		@Override
		protected void handleNotUnderstood(ACLMessage msg){
			// For now we don't do anything, we don't expect to encounter this
	    }
		
		@Override
		protected void handleRefuse(ACLMessage msg) {
			/* This is actually a case for future development. 
			 * Normally, if we receive a refuse message, it means that the CtxCoord agent himself
			 * refused to activate a derived ContextAssertion we need.
			 * For now we just forward the failure to the CtxUser that posed the query.
			 */
			ACLMessage userResponse = prepareQueryResponseMsg(queryWrapper.getInitialMessage(), ACLMessage.FAILURE);
			userResponse.setContent(msg.getContent());
			myAgent.send(userResponse);
		}
		
		@Override
		protected void handleFailure(ACLMessage msg) {
			/* This is actually a case for future development. 
			 * Normally, if we receive a failure message, it means that a CtxSensor/CtxUser agent
			 * has refused to activate sending updates for the ContextAssertions we need.
			 * For now we just forward the failure to the CtxUser that posed the query.
			 */
			ACLMessage userResponse = prepareQueryResponseMsg(queryWrapper.getInitialMessage(), ACLMessage.FAILURE);
			userResponse.setContent(msg.getContent());
			myAgent.send(userResponse);
		}
	}
	
}
