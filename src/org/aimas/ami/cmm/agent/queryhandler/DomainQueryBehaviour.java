package org.aimas.ami.cmm.agent.queryhandler;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SenderBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.SimpleAchieveREInitiator;
import jade.util.Event;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.AssertionCapability;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.EnableAssertions;
import org.aimas.ami.cmm.agent.onto.QueryBase;
import org.aimas.ami.cmm.agent.onto.QueryBaseItem;
import org.aimas.ami.cmm.agent.onto.ResolveQueryBase;
import org.aimas.ami.cmm.agent.onto.UserQuery;
import org.aimas.ami.cmm.agent.onto.UserQueryResult;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionCapability;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionDescription;
import org.aimas.ami.cmm.agent.onto.impl.DefaultEnableAssertions;
import org.aimas.ami.cmm.agent.onto.impl.DefaultResolveQueryBase;
import org.aimas.ami.cmm.agent.queryhandler.UserQueryHandler.UserQueryWrapper;
import org.aimas.ami.contextrep.engine.api.QueryException;
import org.aimas.ami.contextrep.engine.api.QueryResult;
import org.aimas.ami.contextrep.engine.api.QueryResultNotifier;
import org.aimas.ami.contextrep.model.ContextAssertion;

import com.hp.hpl.jena.query.Query;

public class DomainQueryBehaviour extends SequentialBehaviour {
	
    private static final long serialVersionUID = 8435924492567381305L;
    
    private static final String QUERY_BASE_ITEMS_KEY = "query-base-items";
    private static final String QUERY_RESULT_SET_KEY = "query-result-set";
    
    private static final int DOMAIN_QUERY_FAILURE = -1;
    private static final int DOMAIN_QUERY_SUCCESS =  0;
    
    private CtxQueryHandler ctxQueryAgent;
    private UserQueryHandler userQueryHandler;
    private UserQueryWrapper domainQuery;
	
	public DomainQueryBehaviour(CtxQueryHandler queryAgent, UserQueryHandler userQueryHandler,  UserQueryWrapper domainQuery) {
		super(queryAgent);
		
		this.ctxQueryAgent = queryAgent;
		this.userQueryHandler = userQueryHandler;
		this.domainQuery = domainQuery;
		
		addSubBehaviour(new GetQueryBaseInitiator(queryAgent, queryAgent.getAssignedOrgManager()));
		addSubBehaviour(new ResolveQueryBehaviour(queryAgent));
	}
	
	@Override
    protected boolean checkTermination(boolean currentDone, int currentResult) {
    	boolean shouldFinish = super.checkTermination(currentDone, currentResult);
    	
    	if (!shouldFinish) {
    		if (currentDone && currentResult == DOMAIN_QUERY_FAILURE) {
    			// we can stop in advance since one of our agents has failed to confirm initialization
    			shouldFinish = true;
    		}
    	}
    	
    	return shouldFinish;
    }
	
	@Override
    public int onEnd() {
		List<QueryResult> domainQueryResults = (List<QueryResult>)getDataStore().get(QUERY_RESULT_SET_KEY); 
		
		if (domainQueryResults != null) {
			// Here we have to combine the results
			QueryResult cumulatedQueryResult = null;
			boolean allErrors = false;
			boolean isAsk = false;
			
			for (QueryResult qr : domainQueryResults) {
				if (!qr.hasError()) {
					allErrors = false;
					
					if (cumulatedQueryResult == null) {
						if (qr.isAsk()) {
							isAsk = true;
							cumulatedQueryResult = new QueryResult(domainQuery.getQueryContent(), null, qr.getAskResult());
						}
						else {
							cumulatedQueryResult = new QueryResult(domainQuery.getQueryContent(), null, qr.getResultSet());
						}
					}
					else {
						if (isAsk) {
							cumulatedQueryResult.cumulateAsk(qr.getAskResult());
						}
						else {
							cumulatedQueryResult.cumulateResultSet(qr.getResultSet());
						}
					}
				}
			}
			
			int responsePerformative = ACLMessage.INFORM;
			if (allErrors) {
				responsePerformative = ACLMessage.FAILURE;
			}
			
			ACLMessage response = userQueryHandler.prepareQueryResponseMsg(domainQuery.getInitialMessage(), responsePerformative); 
			try {
	            UserQueryResult uqr = userQueryHandler.encodeResultForMessage(cumulatedQueryResult);
	            ctxQueryAgent.getContentManager().fillContent(response, uqr);
            }
            catch (Exception e) {
	            e.printStackTrace();
            }
			
			ctxQueryAgent.addBehaviour(new SenderBehaviour(ctxQueryAgent, response));
		}
		else {
			// We don't have any query results stored.
			// If we could not execute the query, we are going to return a FAILURE message performative
    		ACLMessage response = userQueryHandler.prepareQueryResponseMsg(domainQuery.getInitialMessage(), ACLMessage.FAILURE);
    		ctxQueryAgent.addBehaviour(new SenderBehaviour(ctxQueryAgent, response));
		}
		
		return 0;
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
				return DOMAIN_QUERY_SUCCESS;
			
			return DOMAIN_QUERY_FAILURE;
		}
	}
	
	
	// ======================================================================================
	private class ResolveQueryBehaviour extends AchieveREInitiator implements QueryResultNotifier {
        
		private static final long serialVersionUID = 1L;
		private Event queryEventResult;
		
		public ResolveQueryBehaviour(Agent a) {
	        super(a, null);
        }
		
		@Override
		protected Vector<ACLMessage> prepareRequests(ACLMessage request) {
			Vector<ACLMessage> outgoingMessages = new Vector<ACLMessage>();
			
			// First, retrieve the QueryBaseItems
			QueryBase queryBase = (QueryBase)getParent().getDataStore().get(QUERY_BASE_ITEMS_KEY);
			for (int i = 0; i < queryBase.getBaseItems().size(); i++) {
				QueryBaseItem queryBaseItem = (QueryBaseItem)queryBase.getBaseItems().get(i);
				AID forwardQueryHandler = queryBaseItem.getQueryHandler();
				
				if (forwardQueryHandler.equals(ctxQueryAgent.getAID())) {
					// If we have to answer to the query as well, we create a queryEventResult that
					// we use to synchronize on our own query result
					queryEventResult = new Event(0, null);
					
					Set<ContextAssertion> referencedAssertions = userQueryHandler.analyzeQuery(domainQuery.getQueryContent());
					Set<ContextAssertion> inactiveAssertions = userQueryHandler.analyzeInactiveAssertions(referencedAssertions);
					
					if (inactiveAssertions.isEmpty()) {
						Query query = domainQuery.getQueryContent();
						
						if (query.isAskType()) {
							userQueryHandler.getEngineQueryAdaptor().execAsk(query, null, this);
						}
						else if (query.isSelectType()) {
							userQueryHandler.getEngineQueryAdaptor().execQuery(query, null, this);
						}
					}
					else {				
						ctxQueryAgent.addBehaviour(new EnableAndExecuteBehaviour(domainQuery.getQueryContent(), 
								inactiveAssertions, queryEventResult));
					}
				}
				else if (!forwardQueryHandler.equals(domainQuery.getSender())) {
					// If we have to forward it to another queryHandler then just create the appropriate message
					String forwardUpperDomainURI = queryBaseItem.getQueryUpperBound();
					String forwardLowerDomainURI = queryBaseItem.getQueryLowerBound();
					
					ACLMessage forwardedMsg = copyRequestStrucutre(domainQuery.getInitialMessage());
					forwardedMsg.addReceiver(forwardQueryHandler);
					
					UserQuery forwardedQueryDesc = domainQuery.getQueryDescription();
					forwardedQueryDesc.setDomain_upper_bound(forwardUpperDomainURI);
					forwardedQueryDesc.setDomain_lower_bound(forwardLowerDomainURI);
					
					try {
			            ctxQueryAgent.getContentManager().fillContent(forwardedMsg, forwardedQueryDesc);
			            outgoingMessages.add(forwardedMsg);
					}
		            catch (Exception e) {
		            	e.printStackTrace();
		            }
				}
			}
			
			return outgoingMessages;
		}
		
		@Override
		protected void handleAllResponses(Vector responses) {
			// Handle the responses from the forward query handlers. We retrieve the UserQueryResult from
			// each one and store it in the datastore.
			List<QueryResult> queryResults = new LinkedList<QueryResult>();
			
			for (int i = 0; i < responses.size(); i++) {
				ACLMessage response = (ACLMessage)responses.get(i);
				
				try {
					UserQueryResult uqr = (UserQueryResult)ctxQueryAgent.getContentManager().extractContent(response);
					QueryResult qr = userQueryHandler.decodeResultFromMessage(domainQuery.getQueryContent(), uqr);
					queryResults.add(qr);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			getParent().getDataStore().put(QUERY_RESULT_SET_KEY, queryResults);
		}
		
		@Override
		public int onEnd() {
			// Here we synchronize on the queryEventResult, if we had to do any local processing
			if (queryEventResult != null) {
				try {
	                QueryResult qr = (QueryResult)queryEventResult.waitUntilProcessed();
	                if (qr != null) {
	                	List<QueryResult> queryResults = (List<QueryResult>)getParent().getDataStore().get(QUERY_RESULT_SET_KEY);
	                	if (queryResults == null) {
	                		queryResults = new LinkedList<QueryResult>();
	                		getParent().getDataStore().put(QUERY_RESULT_SET_KEY, queryResults);
	                	}
	                	
	                	queryResults.add(qr);
	                }
                }
                catch (InterruptedException e) {
	                e.printStackTrace();
                }
			}
			
			return super.onEnd();
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
        public void notifyQueryResult(QueryResult result) {
	        if (queryEventResult != null) {
	        	queryEventResult.notifyProcessed(result);
	        }
        }

		@Override
        public void notifyAskResult(QueryResult result) {
			if (queryEventResult != null) {
	        	queryEventResult.notifyProcessed(result);
	        }
        }
	}
	
	
	private class EnableAndExecuteBehaviour extends SimpleAchieveREInitiator implements QueryResultNotifier {
        private static final long serialVersionUID = 1L;
        
        private Query query;
        private Set<ContextAssertion> inactiveAssertions;
        private Event queryEventResult;
        
		public EnableAndExecuteBehaviour(Query query, Set<ContextAssertion> inactiveAssertions, Event queryEventResult) {
			super(ctxQueryAgent, null);
			
			this.query = query;
			this.inactiveAssertions = inactiveAssertions;
			this.queryEventResult = queryEventResult;
		}
		
		@Override
		protected ACLMessage prepareRequest(ACLMessage msg){
			ACLMessage enableAssertionsMsg = new ACLMessage(ACLMessage.REQUEST);
			
			String conversationId = ctxQueryAgent.getName() + "-EnableAssertions-" + System.currentTimeMillis(); 
			
			enableAssertionsMsg.addReceiver(ctxQueryAgent.getConnectedCoordinator());
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
				Action enableAssertionsAction = new Action(ctxQueryAgent.getConnectedCoordinator(), msgContent);
				ctxQueryAgent.getContentManager().fillContent(enableAssertionsMsg, enableAssertionsAction);
            }
            catch (Exception e) {
	            e.printStackTrace();
            }
			
			return enableAssertionsMsg;
		}
		
		@Override
		protected void handleInform(ACLMessage msg){
			// The inform message is just a confirmation that the assertions have been activated.
			// We can now execute the query.
			if (query.isAskType()) {
				userQueryHandler.getEngineQueryAdaptor().execAsk(query, null, this);
			}
			else if (query.isSelectType()) {
				userQueryHandler.getEngineQueryAdaptor().execQuery(query, null, this);
			}
		}
		
		@Override
		protected void handleNotUnderstood(ACLMessage msg){
			// For now we don't do anything, we don't expect to encounter this
	    }
		
		@Override
		protected void handleRefuse(ACLMessage msg) {
			if (queryEventResult != null) {
	        	queryEventResult.notifyProcessed(new QueryResult(query, new QueryException("Enabling of updates "
	        			+ "for required ContextAssertions was refused by the coordiantor agent")));
	        }
		}
		
		@Override
		protected void handleFailure(ACLMessage msg) {
			if (queryEventResult != null) {
	        	queryEventResult.notifyProcessed(new QueryResult(query, new QueryException("Enabling of updates "
	        			+ "for required ContextAssertions was refused by the coordiantor agent")));
	        }
		}
		
		
		@Override
        public void notifyQueryResult(QueryResult result) {
			if (queryEventResult != null) {
	        	queryEventResult.notifyProcessed(result);
	        }
        }

		@Override
        public void notifyAskResult(QueryResult result) {
			if (queryEventResult != null) {
	        	queryEventResult.notifyProcessed(result);
	        }
        }
	}
}
