package org.aimas.ami.cmm.agent.user;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREInitiator;

import org.aimas.ami.contextrep.engine.api.QueryException;
import org.aimas.ami.contextrep.engine.api.QueryResult;

import com.hp.hpl.jena.query.Query;

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
	         result = (QueryResult)msg.getContentObject();
        }
        catch (UnreadableException e) {
        	result = new QueryResult(query, new QueryException("Error decoding query result.", e));
        }
		
		resultNotifier.notifyQueryResult(queryMessage, result);
	}
	
	@Override
	protected void handleInform(ACLMessage msg) {
		QueryResult result = null;
		try {
	         result = (QueryResult)msg.getContentObject();
        }
        catch (UnreadableException e) {
        	result = new QueryResult(query, new QueryException("Error decoding query result.", e));
        }
		
		resultNotifier.notifyQueryResult(queryMessage, result);
	}
}
