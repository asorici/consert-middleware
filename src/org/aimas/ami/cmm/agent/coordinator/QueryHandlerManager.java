package org.aimas.ami.cmm.agent.coordinator;

import jade.core.AID;

import java.util.HashMap;

public class QueryHandlerManager {
	private CtxCoord coordAgent;
	private HashMap<AID, QueryHandlerDescription> registeredQueryHandlers;
	
	public QueryHandlerManager(CtxCoord ctxCoordinator) {
		this.coordAgent = ctxCoordinator;
		registeredQueryHandlers = new HashMap<AID, QueryHandlerDescription>();
	}
	
	public void registerQueryHandler(AID queryAgent, boolean isPrimary) {
		registeredQueryHandlers.put(queryAgent, new QueryHandlerDescription(queryAgent, isPrimary));
	}
	
	public boolean isRegistered(AID queryAgent) {
	    return registeredQueryHandlers.containsKey(queryAgent);
    }
	
	private static class QueryHandlerDescription {
		private AID queryAgent;
		private boolean isPrimary;
		
		QueryHandlerDescription(AID queryAgent, boolean isPrimary) {
	        this.queryAgent = queryAgent;
	        this.isPrimary = isPrimary;
        }

		public AID getQueryAgent() {
			return queryAgent;
		}

		public boolean isPrimary() {
			return isPrimary;
		}
	}
}
