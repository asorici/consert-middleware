package org.aimas.ami.cmm.agent.user;

import jade.core.AID;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.util.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.UserQuery;
import org.aimas.ami.cmm.agent.onto.impl.DefaultUserQuery;
import org.aimas.ami.cmm.api.QueryNotificationHandler;
import org.aimas.ami.contextrep.engine.api.QueryException;
import org.aimas.ami.contextrep.engine.api.QueryResult;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;

public class UserQueryManager {
	private CtxUser userAgent;
	
	private Map<String, UserQueryWrapper> submittedQueries;
	private Map<String, UserSubscriptionWrapper> registeredSubscriptions;
	
	UserQueryManager(CtxUser userAgent) {
		this.userAgent = userAgent;
		
		submittedQueries = new ConcurrentHashMap<String, UserQueryWrapper>();
		registeredSubscriptions = new HashMap<String, UserSubscriptionWrapper>();
	}
	
	/**
	 * Submit a blocking query with the current CtxQueryHandler.
	 * @param query
	 * @param domainTargetType
	 * @param domainLowerBoundURI
	 * @param domainUpperBoundURI
	 * @return The Event object on which to wait for the result to the submitted query.
	 */
	public Event addBlockingQuery(Query query, String domainTargetType,
            String domainLowerBoundURI, String domainUpperBoundURI) {
		
		int queryType = query.isAskType() ? ACLMessage.QUERY_IF : ACLMessage.QUERY_REF;
		UserQueryWrapper wrapper = new UserQueryWrapper(query, queryType, null, domainTargetType, 
				domainLowerBoundURI, domainUpperBoundURI, 0);
		
		String identifier = wrapper.getIdentifier();
		submittedQueries.put(identifier, wrapper);
		
		return wrapper.submitBlocking();
    }
	
	/**
	 * Submit a non-blocking query to the current CtxQueryHandler.
	 * @param query
	 * @param notificationHandler
	 * @param domainTargetType
	 * @param domainLowerBoundURI
	 * @param domainUpperBoundURI
	 */
	public void addQuery(Query query, QueryNotificationHandler notificationHandler, String domainTargetType,
            String domainLowerBoundURI, String domainUpperBoundURI) {
		
		int queryType = query.isAskType() ? ACLMessage.QUERY_IF : ACLMessage.QUERY_REF;
		UserQueryWrapper wrapper = new UserQueryWrapper(query, queryType, notificationHandler, 
				domainTargetType, domainLowerBoundURI, domainUpperBoundURI, 0);
		
		String identifier = wrapper.getIdentifier();
		submittedQueries.put(identifier, wrapper);
		
		wrapper.submit();
    }
	
	/**
	 * Register a subscription with the current CtxQueryHandler.
	 * @param query
	 * @param notificationHandler
	 * @param repeatInterval
	 * @param targetType
	 * @param domainLowerBoundURI
	 * @param domainUpperBoundURI
	 * @return
	 */
	public String registerSubscription(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval, String targetType, String domainLowerBoundURI, String domainUpperBoundURI) {
	    
		int queryType = ACLMessage.SUBSCRIBE;
		UserSubscriptionWrapper wrapper = new UserSubscriptionWrapper(query, queryType, 
			notificationHandler, targetType, domainLowerBoundURI, domainUpperBoundURI, repeatInterval);
		
		String identifier = wrapper.getIdentifier();
		registeredSubscriptions.put(identifier, wrapper);
		wrapper.submit();
		
		return identifier;
    }
	
	/**
	 * Cancel subscription.
	 * @param subscriptionIdentifier The String identifier returned when the subscription was created
	 */
	public void cancelSubscription(String subscriptionIdentifier) {
	    UserSubscriptionWrapper wrapper = registeredSubscriptions.remove(subscriptionIdentifier);
	    wrapper.cancel();
    }
	
	/**
	 * Method called internally when this CtxUser agent is informed of a Domain or CtxQueryHandler 
	 * change by its OrgMgr. All pending queries and subscriptions have been terminated remotely and
	 * will be now resubmitted to the new CtxQueryHandler.  
	 */
	public void reset() {
		// reset pending queries
		for (UserQueryWrapper wrapper : submittedQueries.values()) {
			wrapper.reset();
		}
		
		// reset subscriptions
		for (UserSubscriptionWrapper wrapper : registeredSubscriptions.values()) {
			wrapper.reset();
		}
	}
	
	
	// AUXLIARY QUERY WRAPPER CLASS
	////////////////////////////////////////////////////////////////////////////
	private class UserQueryWrapper implements UserQueryNotifier {
		private static final int QUERY_RESULT_NOTIFICATION = 0x01; 
		
		protected AID ctxQueryAgent;
		
		protected Query query;
		protected int queryType;
		protected QueryNotificationHandler notificationHandler;
		private Event notificationEvent;
		
		protected String domainTargetType;
		protected String domainLowerBoundURI;
		protected String domainUpperBoundURI;
		
		protected int repeatInterval;
		
		protected ACLMessage queryMessage;
		private UserQueryBehaviour handlerBehaviour;
		
		protected UserQueryWrapper(Query query, int queryType,
                QueryNotificationHandler notificationHandler,
                String domainTargetType, String domainLowerBoundURI,
                String domainUpperBoundURI, int repeatInterval) {
	        this.query = query;
	        this.queryType = queryType;
	        this.notificationHandler = notificationHandler;
	        this.domainTargetType = domainTargetType;
	        this.domainLowerBoundURI = domainLowerBoundURI;
	        this.domainUpperBoundURI = domainUpperBoundURI;
	        this.repeatInterval = repeatInterval;
	        
	        createQueryMessage(null);
        }
		
		protected void createQueryMessage(String conversationId) {
	        // Set the ctxQueryAgent
			ctxQueryAgent = userAgent.getQueryAgent();
			
			// Create UserQuery query description
			UserQuery queryDesc = new DefaultUserQuery();
			queryDesc.setQueryTarget(domainTargetType);
			
			if (domainLowerBoundURI != null) {
				queryDesc.setDomain_lower_bound(domainLowerBoundURI);
			}
			else {
				queryDesc.setDomain_lower_bound(userAgent.getApplicationAdaptor().getDomainValue());
			}
			
			if (domainUpperBoundURI != null) {
				queryDesc.setDomain_upper_bound(domainUpperBoundURI);
			}
			else {
				queryDesc.setDomain_upper_bound(userAgent.getApplicationAdaptor().getDomainValue());
			}
			
			queryDesc.setRepeatInterval(repeatInterval);
			queryDesc.setQueryContent(query.serialize(Syntax.syntaxSPARQL_11));
			
			// Create ACLMessage
			queryMessage = new ACLMessage(queryType);
			queryMessage.setLanguage(CMMAgent.cmmCodec.getName());
			queryMessage.setOntology(CMMAgent.cmmOntology.getName());
			
			if (queryType == ACLMessage.SUBSCRIBE) {
				queryMessage.setProtocol(InteractionProtocol.FIPA_SUBSCRIBE);
			}
			else {
				queryMessage.setProtocol(InteractionProtocol.FIPA_QUERY);
			}
			
			if (conversationId == null) {
				conversationId = userAgent.getName() + "-query-" + System.currentTimeMillis() + "-" + getCnt();
			}
			
			queryMessage.setConversationId(conversationId);
			queryMessage.addReceiver(ctxQueryAgent);
			
			try {
	            userAgent.getContentManager().fillContent(queryMessage, queryDesc);
			}
            catch (Exception e) {
            	e.printStackTrace();
            }
        }
		
		protected String getIdentifier() {
			return queryMessage.getConversationId();
		}
		
		Event submitBlocking() {
			handlerBehaviour = new UserQueryBehaviour(userAgent, this, queryMessage, query);
			notificationEvent = new Event(QUERY_RESULT_NOTIFICATION, handlerBehaviour);
			
			userAgent.addBehaviour(handlerBehaviour);
			return notificationEvent;
		}
		
		protected void submit() {
			handlerBehaviour = new UserQueryBehaviour(userAgent, this, queryMessage, query);
			userAgent.addBehaviour(handlerBehaviour);
		}
		
		protected void cancel() {
			userAgent.removeBehaviour(handlerBehaviour);
			
			if (notificationEvent != null) {
				notificationEvent.notifyProcessed(null);
				notificationEvent = null;
			}
			
			notificationHandler = null;
		}
		
		protected void reset() {
			// recreate the query message keeping the original conversationId
			createQueryMessage(queryMessage.getConversationId());
			handlerBehaviour.reset(queryMessage);
		}
		
		// ================ QUERY AND SUBSCRIPTION RESULT NOTIFICATION ================
		@Override
	    public void notifyQueryResult(ACLMessage queryMessage, QueryResult result) {
			// first send notification
			if (queryMessage.getConversationId().equals(getIdentifier())) {
				if (notificationEvent != null) {
					// the blocking case
					notificationEvent.notifyProcessed(result);
				}
				else if (notificationHandler != null) { 
					if (result.isAsk()) {
						System.out.println("[INFO "+getClass().getName()+"] answer to ask query for id: " 
								+ getIdentifier() + " = " + result.getAskResult());
					}
					
					notificationHandler.handleResultNotification(query, result);
				}
			}
			
			// then remove from submitted queries list
			submittedQueries.remove(getIdentifier());
	    }

		@Override
	    public void notifyRefuse(ACLMessage queryMessage, ACLMessage refusal) {
			// first send notification
			if (queryMessage.getConversationId().equals(getIdentifier())) {
				if (notificationEvent != null) {
					// the blocking case: create a local QueryResult message with an exception
					notificationEvent.notifyProcessed(new QueryResult(query, 
							new QueryException("Query refused. Reason: " + refusal.getContent())));
				}
				else if (notificationHandler != null) {
					// TODO: see how we can add a more meaningful handling of refusal motive
					notificationHandler.handleRefuse(query);
				}
		    }
			
			// then remove from submitted queries list
			submittedQueries.remove(getIdentifier());
	    }
	}
	
	private class UserSubscriptionWrapper extends UserQueryWrapper {
		private UserSubscribeBehaviour handlerBehaviour;
		
		protected UserSubscriptionWrapper(Query query, int queryType,
                QueryNotificationHandler notificationHandler,
                String domainTargetType, String domainLowerBoundURI,
                String domainUpperBoundURI, int repeatInterval) {
	        super(query, queryType, notificationHandler, domainTargetType,
	                domainLowerBoundURI, domainUpperBoundURI, repeatInterval);
        }
		
		@Override
		protected void submit() {
			handlerBehaviour = new UserSubscribeBehaviour(userAgent, this, queryMessage, query);
			userAgent.addBehaviour(handlerBehaviour);
		}
		
		@Override
		protected void cancel() {
			handlerBehaviour.cancel(ctxQueryAgent, true);
			notificationHandler = null;
		}
	}
	
	
	private static long counter = 0;
	private static long getCnt() {
		return counter++;
	}
}
