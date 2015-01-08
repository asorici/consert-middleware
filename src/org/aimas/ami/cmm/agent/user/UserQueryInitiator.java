package org.aimas.ami.cmm.agent.user;

import jade.lang.acl.ACLMessage;
import jade.proto.SimpleAchieveREInitiator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aimas.ami.cmm.agent.onto.UserQueryResult;
import org.aimas.ami.contextrep.engine.api.ContextResultSet;
import org.aimas.ami.contextrep.engine.api.QueryException;
import org.aimas.ami.contextrep.engine.api.QueryResult;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;

public class UserQueryInitiator extends SimpleAchieveREInitiator {
    private static final long serialVersionUID = -559592994749114438L;
    
    private ACLMessage queryMessage;
    private Query query;
    private UserQueryNotifier resultNotifier;
    
	public UserQueryInitiator(CtxUser userAgent, UserQueryNotifier resultNotifier, 
			ACLMessage queryMessage, Query query) {
		super(userAgent, queryMessage);
		
		this.queryMessage = queryMessage;
		this.resultNotifier = resultNotifier;
		this.query = query;
	}
	
	@Override
	protected void handleNotUnderstood(ACLMessage msg) {
		// Something was wrong with the message. Build a QueryResult object with the error message
		// set to the one given by the msg
		QueryResult result = new QueryResult(query, new QueryException(msg.getContent()));
		resultNotifier.notifyQueryResult(queryMessage, result);
	}
	
	@Override
	protected void handleRefuse(ACLMessage msg) {
		resultNotifier.notifyRefuse(queryMessage, msg);
	}
	
	@Override
	protected void handleFailure(ACLMessage msg) {
		// The exception message for the failure is in the contentObject of the message
		QueryResult result = null;
		try {
	         UserQueryResult qr = (UserQueryResult)myAgent.getContentManager().extractContent(msg);
	         result = decodeResult(qr);
        }
        catch (Exception e) {
        	result = new QueryResult(query, new QueryException("Error decoding query result.", e));
        }
		
		resultNotifier.notifyQueryResult(queryMessage, result);
	}
	
	@Override
	protected void handleInform(ACLMessage msg) {
		QueryResult result = null;
		try {
			UserQueryResult qr = (UserQueryResult)myAgent.getContentManager().extractContent(msg);
	        result = decodeResult(qr);
        }
        catch (Exception e) {
        	result = new QueryResult(query, new QueryException("Error decoding query result.", e));
        }
		
		resultNotifier.notifyQueryResult(queryMessage, result);
	}
	
	
	// Auxiliary functions
	/////////////////////////////////////////////////////////////////////////
	private QueryResult decodeResult(UserQueryResult qr) {
	    boolean isAsk = qr.getIsAsk();
	    boolean askResult = qr.getAskResult();
	    
	    String errorMessage = qr.getErrorMessage();
	    String resultSetString = qr.getQueryResultSet();
	    
	    if (!errorMessage.isEmpty()) {
	    	return new QueryResult(query, new QueryException(errorMessage));
	    }
	    else {
	    	if (isAsk) {
	    		return new QueryResult(query, null, askResult);
	    	}
	    	else {
	    		ContextResultSet rs = parseResultSet(resultSetString);
	    		return new QueryResult(query, null, rs);
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
}
