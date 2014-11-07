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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class QueryManager implements AssertionUpdateListener {
	private CtxQueryHandler ctxQueryAgent;
	private QueryHandler engineQueryAdaptor;
	private StatsHandler engineStatsAdaptor;
	
	private Map<AID, UserQueryHandler> managedQueries;
	
	public QueryManager(CtxQueryHandler ctxQueryAgent) throws CMMConfigException {
	    this.ctxQueryAgent = ctxQueryAgent;
	    setup();
	}
	
	/**
	 * Find the CONSERT Engine query and stats adaptor
	 * @throws CMMConfigException
	 */
	private void setup() throws CMMConfigException {
		BundleContext context = ctxQueryAgent.getOSGiBridge().getBundleContext();
		managedQueries = new HashMap<AID, UserQueryHandler>();
		
		ServiceReference<QueryHandler> queryAdaptorRef = context.getServiceReference(QueryHandler.class);
		if (queryAdaptorRef == null) {
			throw new CMMConfigException("CtxQueryHandler " + ctxQueryAgent.getName() + 
				" could not find reference for CONSERT Engine service: " + QueryHandler.class.getName());
		}
		engineQueryAdaptor = context.getService(queryAdaptorRef);
		engineQueryAdaptor.registerAssertionUpdateListener(this);
		
		ServiceReference<StatsHandler> statsAdaptorRef = context.getServiceReference(StatsHandler.class);
		if (statsAdaptorRef == null) {
			throw new CMMConfigException("CtxQueryHandler " + ctxQueryAgent.getName() + 
				" could not find reference for CONSERT Engine service: " + StatsHandler.class.getName());
		}
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
