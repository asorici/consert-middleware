package org.aimas.ami.cmm.agent.user;

import org.aimas.ami.contextrep.engine.api.QueryException;
import org.aimas.ami.contextrep.engine.api.QueryResult;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SubscriptionInitiator;

import com.hp.hpl.jena.query.Query;

public class UserSubscribeBehaviour extends SubscriptionInitiator {
    private static final long serialVersionUID = 1482335988271649414L;
    
    private ACLMessage subscribeMessage;
    private Query query;
    private UserQueryNotifier resultNotifier;
    
	public UserSubscribeBehaviour(CtxUser userAgent, UserQueryNotifier resultNotifier, 
			ACLMessage subscribeMessage, Query query) {
	    super(userAgent, subscribeMessage);
	    
	    this.subscribeMessage = subscribeMessage;
		this.resultNotifier = resultNotifier;
		this.query = query;
    }
	
	@Override
	protected void handleNotUnderstood(ACLMessage msg) {
		// Something was wrong with the message. Build a QueryResult object with the error message
		// set to the one given by the msg
		QueryResult result = new QueryResult(query, new QueryException(msg.getContent()));
		resultNotifier.notifyQueryResult(subscribeMessage, result);
	}
	
	@Override
	protected void handleRefuse(ACLMessage msg) {
		resultNotifier.notifyRefuse(subscribeMessage, msg);
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
		
		resultNotifier.notifyQueryResult(subscribeMessage, result);
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
		
		resultNotifier.notifyQueryResult(subscribeMessage, result);
	}
}
