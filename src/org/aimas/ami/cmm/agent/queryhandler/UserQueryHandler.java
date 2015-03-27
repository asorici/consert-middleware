package org.aimas.ami.cmm.agent.queryhandler;

import jade.content.ContentElement;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.SenderBehaviour;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aimas.ami.cmm.agent.CMMAgent;
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
import org.aimas.ami.contextrep.engine.api.QueryException;
import org.aimas.ami.contextrep.engine.api.QueryHandler;
import org.aimas.ami.contextrep.engine.api.QueryResult;
import org.aimas.ami.contextrep.model.ContextAssertion;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;

public class UserQueryHandler {
	
	AID user;
	QueryManager manager;
	
	Map<String, UserQueryWrapper> registeredSubscriptions;
	Map<String, DomainSubscriptionBehaviour> registeredDomainSubscriptions;
	Map<String, UserQueryWrapper> pendingQueries;
	
	/* A mapping from a ContextAssertion to the list of subscription identifiers in which 
	 * the ContextAssertion is present */
	Map<ContextAssertion, List<String>> subscriptionBodyMap;
	
	public UserQueryHandler(QueryManager manager, AID user) {
		this.manager = manager;
		this.user = user;
	    
	    registeredSubscriptions = new HashMap<String, UserQueryWrapper>();
	    registeredDomainSubscriptions = new HashMap<String, DomainSubscriptionBehaviour>();
	    pendingQueries = new ConcurrentHashMap<String, UserQueryWrapper>();
	    
	    subscriptionBodyMap = new HashMap<ContextAssertion, List<String>>();
    }
	
	QueryHandler getEngineQueryAdaptor() {
		return manager.getEngineQueryAdaptor();
	}
	
	Set<ContextAssertion> analyzeQuery(Query query) {
		return manager.getEngineQueryAdaptor().analyzeQuery(query, null);
	}
	
	Set<ContextAssertion> analyzeInactiveAssertions(Set<ContextAssertion> referencedAssertions) {
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
	            UserQueryResult qr = encodeResultForMessage(result);
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
    		// the new result differs from the cached one.
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
    		else {
    			// If it is a time-based subscription or a simple query, we just send the result, i.e. we do no caching
    			sendInform = true;
    		}
    		
    		if (sendInform) {
	    		ACLMessage response = prepareQueryResponseMsg(queryWrapper.getInitialMessage(), ACLMessage.INFORM);
	    		try {
	    			UserQueryResult qr = encodeResultForMessage(result);
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

    
    UserQueryResult encodeResultForMessage(QueryResult result) {
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
    
    QueryResult decodeResultFromMessage(Query initialQuery, UserQueryResult qr) {
	    boolean isAsk = qr.getIsAsk();
	    boolean askResult = qr.getAskResult();
	    
	    String errorMessage = qr.getErrorMessage();
	    String resultSetString = qr.getQueryResultSet();
	    
	    if (!errorMessage.isEmpty()) {
	    	return new QueryResult(initialQuery, new QueryException(errorMessage));
	    }
	    else {
	    	if (isAsk) {
	    		return new QueryResult(initialQuery, null, askResult);
	    	}
	    	else {
	    		ContextResultSet rs = parseResultSet(resultSetString);
	    		return new QueryResult(initialQuery, null, rs);
	    	}
	    }
    }
	
	private ContextResultSet parseResultSet(String resultSetString) {
		ResultSet results = ResultSetFactory.fromXML(resultSetString);
		List<String> resultVars = results.getResultVars();
		final List<Binding> bindings = new ArrayList<Binding>();
		
		while (results.hasNext()) {
			Binding binding = results.nextBinding();
			bindings.add(detachBinding(binding));
		}
		
		return new ContextResultSet(resultVars, bindings);
    }
	
	private Binding detachBinding(Binding binding) {
		Iterator<Var> varsIt = binding.vars();
		Binding initial = BindingFactory.binding();
		
		while (varsIt.hasNext()) {
			Var var = varsIt.next();
			initial = BindingFactory.binding(initial, var, binding.get(var));
		}
		
		return initial;
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
    		// First, determine if we this query is addressed to us (local or our domain) or if it
    		// needs to be forwarded to another CtxQueryHandler
    		boolean executeLocally = true;
    		
    		if (!queryWrapper.isLocalQuery()) {
	    		if (queryWrapper.isExactDomainQuery()) {
	    			String queryDomainValueURI = queryWrapper.getExactDomainValue();
	    			String ourDomainValueURI = manager.getCtxQueryAgent().getContextDomainValueURI();
	    			
	    			if (ourDomainValueURI != null && !ourDomainValueURI.equals(queryDomainValueURI)) {
	    				// If we have an exact domain query request and the domain does not match
	    				// ours, we will have to collaborate with the OrgMgr to see where to
	    				// route the query
	    				executeLocally = false;
	    			}
	    		}
	    		else if (queryWrapper.isUpperBroadcastQuery()) {
	    			// If we are dealing with an upperBroadcast, we will have to work with the OrgMgr to see:
	    			// (i) to whom we have to route the query, (ii) if we have to execute it as well 
	    			executeLocally = false;
	    		}
	    		else {
	    			// TODO: implement the other cases as well: full domain-range query -- but this has
	    			// to be re-thought in terms of domainRangeEntity limits instead of domainRangeValue limits.  
	    			return;
	    		}
    		}
    		
    		if (executeLocally) {
	    		// If we have to execute the query ourselves, analyze it and see if we need to enable anything first
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
    		else {
    			// If we have to forward the query to other CtxQueryHandlers (possibly ourself too, in the
    			// case we have an upper broadcast in which our assigned OrgMgr determines that we have to
    			// participate with answers) start a DomainQueryBehaviour
    			manager.getCtxQueryAgent().addBehaviour(new DomainQueryBehaviour(manager.getCtxQueryAgent(), 
    					this, queryWrapper));
    		}
    	}
    }

	
	public void registerSubscription(ACLMessage userQueryMsg) {
		UserQueryWrapper queryWrapper = buildWrapper(userQueryMsg);
    	if (queryWrapper != null) {
    		// First, determine if we this query is addressed to us (local or our domain) or if it
    		// needs to be forwarded to another CtxQueryHandler
    		boolean executeLocally = true;
    		
    		if (!queryWrapper.isLocalQuery()) {
    			if (queryWrapper.isExactDomainQuery()) {
	    			String queryDomainValueURI = queryWrapper.getExactDomainValue();
	    			String ourDomainValueURI = manager.getCtxQueryAgent().getContextDomainValueURI();
	    			
	    			if (ourDomainValueURI != null && !ourDomainValueURI.equals(queryDomainValueURI)) {
	    				// If we have an exact domain query request and the domain does not match
	    				// ours, we will have to collaborate with the OrgMgr to see where to
	    				// route the query
	    				executeLocally = false;
	    			}
	    		}
	    		else if (queryWrapper.isUpperBroadcastQuery()) {
	    			// If we are dealing with an upperBroadcast, we will have to work with the OrgMgr to see:
	    			// (i) to whom we have to route the query, (ii) if we have to execute it as well 
	    			executeLocally = false;
	    		}
	    		else {
	    			// TODO: implement the other cases as well: full domain-range query -- but this has
	    			// to be re-thought in terms of domainRangeEntity limits instead of domainRangeValue limits.  
	    			return;
	    		}
    		}
    		
    		if (executeLocally) {
	    		// If we have to execute the query ourselves, analyze it and see if we need to enable anything first
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
	    		
	    		// Lastly, use the CONSERT Engine QueryHandler to mark the registration of this query
	    		// as a subscription
	    		manager.getEngineQueryAdaptor().registerSubscription(queryWrapper.getQueryContent());
    		}
    		else {
    			// If we have to forward the subscription to other CtxQueryHandlers (possibly ourself too, in the
    			// case we have an upper broadcast in which our assigned OrgMgr determines that we have to
    			// participate with answers) start a DomainSubscriptionBehaviour
    			
    			// We will register the subscription in the registeredDomainSubscriptions map, such
    			// that when a cancellation request comes, we now how to resolve it.
    			DomainSubscriptionBehaviour domainSubscriptionBehaviour = new DomainSubscriptionBehaviour(
    					manager.getCtxQueryAgent(), this, queryWrapper);
    			registeredDomainSubscriptions.put(userQueryMsg.getConversationId(), domainSubscriptionBehaviour);
    			manager.getCtxQueryAgent().addBehaviour(domainSubscriptionBehaviour);
    		}
    	}
    }

	
	public void cancelSubscription(ACLMessage userQueryMsg) {
	    String subscriptionIdentifier = userQueryMsg.getConversationId();
	    
	    // When canceling, we first check to see if the subscription is for a domain query.
	    // The local and domain subscriptions are disjoint: if the domain subscription involved a local
	    // query as well, it will be deleted by the corresponding domainSubscriptionBehaviour
	    if (registeredDomainSubscriptions.containsKey(subscriptionIdentifier)) {
	    	DomainSubscriptionBehaviour domainSubscriptionBehaviour = 
	    			registeredDomainSubscriptions.remove(subscriptionIdentifier);
	    	domainSubscriptionBehaviour.cancel();
	    	// NOTE that this is a decoupled cancellation, i.e. the user requesting the cancellation
	    	// receives confirmation of this fact, before all up- or down-stream cancellations have
	    	// been processed.
	    }
	    else {
	    	// For the local subscriptions, remove any pending queries, remove the subscription
	    	// and unregister it from the CONSERT Engine QueryHandler
	    	pendingQueries.remove(subscriptionIdentifier);
		    UserQueryWrapper queryWrapper = registeredSubscriptions.remove(subscriptionIdentifier);
		    manager.getEngineQueryAdaptor().unregisterSubscription(queryWrapper.getQueryContent());
		    
		    for (List<String> subsIdentifiers : subscriptionBodyMap.values()) {
		    	subsIdentifiers.remove(subscriptionIdentifier);
		    }
	    }
	    
	    
	    ACLMessage cancelConfirm = prepareQueryResponseMsg(userQueryMsg, ACLMessage.INFORM);
	    manager.getCtxQueryAgent().addBehaviour(new SenderBehaviour(manager.getCtxQueryAgent(), cancelConfirm));
    }
	
	// ========================== AUXILIARY WRAPPERS AND BEHAVIOURS ========================== //
	/////////////////////////////////////////////////////////////////////////////////////////////
	class UserQueryWrapper {
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
		
		AID getSender() {
	        return initQueryMessage.getSender();
        }
		
		UserQuery getQueryDescription() {
			return queryDescription;
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
		
		boolean isLocalQuery() {
			return queryDescription.getQueryTarget().equals(UserQuery.LOCAL_QUERY);
		}
		
		boolean isExactDomainQuery() {
			String upperBound = queryDescription.getDomain_upper_bound();
			String lowerBound = queryDescription.getDomain_lower_bound();
			
			return !upperBound.isEmpty() && !lowerBound.isEmpty() && upperBound.equals(lowerBound); 
		}
		
		String getExactDomainValue() {
			if (isExactDomainQuery()) {
				return queryDescription.getDomain_upper_bound();
			}
			
			return null;
		}
		
		String getUpperDomainValue() {
			return queryDescription.getDomain_upper_bound();
		}
		
		String getLowerDomainValue() {
			return queryDescription.getDomain_lower_bound();
		}
		
		boolean isUpperBroadcastQuery() {
			String upperBound = queryDescription.getDomain_upper_bound();
			String lowerBound = queryDescription.getDomain_lower_bound();
			
			return !upperBound.isEmpty() && lowerBound.isEmpty();
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
			
			QueryExecBehaviour b = new QueryExecBehaviour(queryType, queryIdentifier, queryContent, UserQueryHandler.this);
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
			
			CtxQueryHandler ctxQueryAgent = manager.getCtxQueryAgent();
			String conversationId = ctxQueryAgent.getName() + "-EnableAssertions-" + System.currentTimeMillis(); 
			
			AID coordinatorAgent = ctxQueryAgent.getConnectedCoordinator();
			
			enableAssertionsMsg.addReceiver(coordinatorAgent);
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
				Action enableAssertionsAction = new Action(coordinatorAgent, msgContent);
	            manager.getCtxQueryAgent().getContentManager().fillContent(enableAssertionsMsg, enableAssertionsAction);
            }
            catch (Exception e) {
	            e.printStackTrace();
            }
			
			return enableAssertionsMsg;
		}
		
		@Override
		protected void handleInform(ACLMessage msg) {
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
