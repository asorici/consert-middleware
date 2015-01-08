package org.aimas.ami.cmm.agent.queryhandler;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;

import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.contextrep.engine.api.AssertionUpdateListener;
import org.aimas.ami.contextrep.engine.api.QueryHandler;
import org.aimas.ami.contextrep.engine.api.StatsHandler;
import org.aimas.ami.contextrep.model.ContextAssertion;
import org.aimas.ami.contextrep.resources.CMMConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class QueryManager implements AssertionUpdateListener {
	private CtxQueryHandler ctxQueryAgent;
	private QueryHandler engineQueryAdaptor;
	private StatsHandler engineStatsAdaptor;
	
	private Map<AID, UserQueryHandler> managedQueries;
	
	public QueryManager(CtxQueryHandler ctxQueryAgent, String appIdentifier) throws CMMConfigException {
	    this.ctxQueryAgent = ctxQueryAgent;
	    setup(appIdentifier);
	}
	
	/**
	 * Find the CONSERT Engine query and stats adaptor
	 * @param appIdentifier 
	 * @throws CMMConfigException
	 */
	private void setup(String appIdentifier) throws CMMConfigException {
		BundleContext context = ctxQueryAgent.getOSGiBridge().getBundleContext();
		managedQueries = new HashMap<AID, UserQueryHandler>();
		
		ServiceReference<StatsHandler> statsAdaptorRef = null;
		ServiceReference<QueryHandler> queryAdaptorRef = null;
		
		try {
	        statsAdaptorRef = context.getServiceReferences(StatsHandler.class, 
	        		"(" + CMMConstants.CONSERT_APPLICATION_ID_PROP + "=" + appIdentifier + ")").iterator().next();
	        if (statsAdaptorRef == null) {
				throw new CMMConfigException("CtxQueryHandler " + ctxQueryAgent.getName() + 
						" could not find reference for CONSERT Engine service: " + StatsHandler.class.getName());
			}
	        
	        queryAdaptorRef = context.getServiceReferences(QueryHandler.class, 
	        		"(" + CMMConstants.CONSERT_APPLICATION_ID_PROP + "=" + appIdentifier + ")").iterator().next();
	        if (queryAdaptorRef == null) {
				throw new CMMConfigException("CtxQueryHandler " + ctxQueryAgent.getName() + 
						" could not find reference for CONSERT Engine service: " + QueryHandler.class.getName());
			}
        }
        catch (InvalidSyntaxException e) {
	        e.printStackTrace();
	        throw new CMMConfigException(e);
        }
		
		engineQueryAdaptor = context.getService(queryAdaptorRef);
		engineQueryAdaptor.registerAssertionUpdateListener(this);
		
		engineStatsAdaptor = context.getService(statsAdaptorRef);
	}
	
	QueryHandler getEngineQueryAdaptor() {
		return engineQueryAdaptor;
	}
	
	StatsHandler getEngineStatsAdaptor() {
		return engineStatsAdaptor;
	}
	
	CtxQueryHandler getCtxQueryAgent() {
		return ctxQueryAgent;
	}
	
	
	// ================ QUERY REGISTRATION AND CANCELLATION ================ //
	///////////////////////////////////////////////////////////////////////////
	public void registerUser(AID ctxUser) {
		UserQueryHandler handler = managedQueries.get(ctxUser);
		if (handler == null) {
			handler = new UserQueryHandler(this, ctxUser);
			managedQueries.put(ctxUser, handler);
		}
    }
	
	void executeQuery(AID ctxUser, ACLMessage userQueryMsg) {
		UserQueryHandler handler = managedQueries.get(ctxUser);
		if (handler == null) {
			handler = new UserQueryHandler(this, ctxUser);
			managedQueries.put(ctxUser, handler);
		}
		
		handler.executeQuery(userQueryMsg);
	}
	
	
	void registerSubscription(AID ctxUser, ACLMessage userQueryMsg) {
		UserQueryHandler handler = managedQueries.get(ctxUser);
		if (handler == null) {
			handler = new UserQueryHandler(this, ctxUser);
			managedQueries.put(ctxUser, handler);
		}
		
		handler.registerSubscription(userQueryMsg);
	}
	
	
	void cancelSubscription(AID ctxUser, ACLMessage userQueryMsg) {
		UserQueryHandler handler = managedQueries.get(ctxUser);
		if (handler != null) {
			handler.cancelSubscription(userQueryMsg);
		}
	}
	
	
	// ================= CONTEXT ASSERTION UPDATE LISTENER ================= //
	///////////////////////////////////////////////////////////////////////////
	@Override
    public void notifyAssertionUpdated(ContextAssertion contextAssertion) {
		for (UserQueryHandler handler : managedQueries.values()) {
			handler.notifyAssertionUpdated(contextAssertion);
		}
	}
}
