package org.aimas.ami.cmm.agent.queryhandler;

import jade.core.behaviours.OneShotBehaviour;

import org.aimas.ami.contextrep.engine.api.QueryResult;
import org.aimas.ami.contextrep.engine.api.QueryResultNotifier;

import com.hp.hpl.jena.query.Query;

public class QueryExecBehaviour extends OneShotBehaviour implements QueryResultNotifier {
    private static final long serialVersionUID = -3447440277538850391L;
    
    private int queryType;
    private String queryIdentifier;
    private Query query;
	private UserQueryHandler userQueryHandler;
	
	
	public QueryExecBehaviour(int queryType, String queryIdentifier, Query query, 
			UserQueryHandler userQueryHandler) {
	    this.queryType = queryType;
	    this.query = query;
	    this.queryIdentifier = queryIdentifier;
	    this.userQueryHandler = userQueryHandler;
    }
	
	@Override
	public void action() {
		// Pose the query
		if (query.isAskType()) {
			//System.out.println("[INFO " + getClass().getSimpleName() + "] EXECUTING ASK QUERY " + queryIdentifier);
			userQueryHandler.getEngineQueryAdaptor().execAsk(query, null, this);
		}
		else if (query.isSelectType()) {
			userQueryHandler.getEngineQueryAdaptor().execQuery(query, null, this);
		}
	}
	
	
	@Override
    public void notifyAskResult(QueryResult queryResult) {
		if (!queryResult.hasError()) {
	    	userQueryHandler.notifyQueryResult(queryType, queryIdentifier, queryResult);
	    }
	    else {
	    	queryResult.getError().printStackTrace();
	    }
	    	
    }
	
	
	@Override
    public void notifyQueryResult(QueryResult queryResult) {
		if (!queryResult.hasError()) {
			/*
			if (queryIdentifier.contains("CtxUser__AirConditioning")) {
				System.out.println("[INFO " + getClass().getName() + "] RESULTS FOR QUERY WITH ID: " + queryIdentifier);
				while(queryResult.getResultSet().hasNext()) {
	    			System.out.println(queryResult.getResultSet().nextSolution());
	    		}
			}
			queryResult.getResultSet().reset();
			*/
			userQueryHandler.notifyQueryResult(queryType, queryIdentifier, queryResult);
		}
		else {
			queryResult.getError().printStackTrace();
		}
    }
	
}
