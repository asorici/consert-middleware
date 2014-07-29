package org.aimas.ami.cmm.agent.user;

import jade.util.Event;

import org.aimas.ami.cmm.api.DisconnectedQueryHandlerException;
import org.aimas.ami.cmm.api.QueryNotificationHandler;
import org.aimas.ami.cmm.resources.ApplicationUserAdaptor;
import org.aimas.ami.contextrep.engine.api.QueryResult;

import com.hp.hpl.jena.query.Query;

public class CtxUserAdaptor implements ApplicationUserAdaptor {
	private CtxUser userAgent;
	private UserQueryManager userQueryManager;
	private String domainValueURI;
	
	
	public CtxUserAdaptor(CtxUser userAgent) {
		this.userAgent = userAgent;
		this.userQueryManager = new UserQueryManager(userAgent);
	}
	
	// QUERY AND SUBSCRIPTION SUBMISSION IMPLEMENTATION
	////////////////////////////////////////////////////////////////////////////
	private QueryResult submitQuery(Query query, QueryTarget target, 
			String domainLowerBoundURI, String domainUpperBoundURI, long timeout) 
			throws DisconnectedQueryHandlerException {
		
		if (userAgent.hasQueryHandler()) {
			Event resultEvent = userQueryManager.addBlockingQuery(query, target.getName(),
					domainLowerBoundURI, domainUpperBoundURI);
			
			try {
		        QueryResult result = (QueryResult)resultEvent.waitUntilProcessed(timeout);
		        return result;
			}
	        catch (InterruptedException e) {
		        return null;
	        }
		}
		else {
			throw new DisconnectedQueryHandlerException();
		}
		
	}
	
	private void submitQuery(Query query, QueryNotificationHandler notificationHandler, 
			QueryTarget target, String domainLowerBoundURI, String domainUpperBoundURI) 
			throws DisconnectedQueryHandlerException{
		
		if (userAgent.hasQueryHandler()) {
			userQueryManager.addQuery(query, notificationHandler, target.getName(),
				domainLowerBoundURI, domainUpperBoundURI);
		}
		else {
			throw new DisconnectedQueryHandlerException();
		}
	}
	
	private String subscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval, QueryTarget target, String domainLowerBoundURI, 
			String domainUpperBoundURI) throws DisconnectedQueryHandlerException {
		
		if (userAgent.hasQueryHandler()) {
			return userQueryManager.registerSubscription(query, notificationHandler, repeatInterval, 
				target.getName(), domainLowerBoundURI, domainUpperBoundURI);
		}
		else {
			throw new DisconnectedQueryHandlerException();
		}
	}
	
	// QUERY AND SUBSCRIPTION INTERFACE
	////////////////////////////////////////////////////////////////////////////
	@Override
	public QueryResult submitLocalQuery(Query query, long timeout) 
			throws DisconnectedQueryHandlerException {
		return submitQuery(query, QueryTarget.LOCAL, null, null, timeout);
	}
	
	@Override
	public QueryResult submitExactDomainQuery(Query query, String domainURI, long timeout) 
			throws DisconnectedQueryHandlerException {
		return submitQuery(query, QueryTarget.DOMAIN, domainURI, domainURI, timeout);
	}
	
	@Override
	public QueryResult submitDomainQuery(Query query, String domainLowerBoundURI, 
			String domainUpperBoundURI, long timeout)throws DisconnectedQueryHandlerException {
		return submitQuery(query, QueryTarget.DOMAIN, domainLowerBoundURI, domainUpperBoundURI, timeout);
	}
	
	@Override
	public void submitLocalQuery(Query query, QueryNotificationHandler notificationHandler)
			throws DisconnectedQueryHandlerException {
		submitQuery(query, notificationHandler, QueryTarget.LOCAL, null, null);
	}
	
	@Override
	public void submitExactDomainQuery(Query query, QueryNotificationHandler notificationHandler, 
			String domainURI) throws DisconnectedQueryHandlerException {
		submitQuery(query, notificationHandler, QueryTarget.DOMAIN, domainURI, domainURI);
	}
	
	@Override
	public void submitDomainQuery(Query query, QueryNotificationHandler notificationHandler, 
			String domainLowerBoundURI, String domainUpperBoundURI) throws DisconnectedQueryHandlerException {
		submitQuery(query, notificationHandler, QueryTarget.DOMAIN, domainLowerBoundURI, domainUpperBoundURI);
	}
	
	@Override
	public String localSubscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval) throws DisconnectedQueryHandlerException {
		return subscribe(query, notificationHandler, repeatInterval, QueryTarget.LOCAL, null, null);
	}
	
	@Override
	public String exactDomainSubscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval, String domainURI) throws DisconnectedQueryHandlerException {
		return subscribe(query, notificationHandler, repeatInterval, QueryTarget.DOMAIN, domainURI, domainURI);
	}
	
	@Override
	public String domainSubscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval, String domainLowerBoundURI, String domainUpperBoundURI)
			throws DisconnectedQueryHandlerException {
		return subscribe(query, notificationHandler, repeatInterval, QueryTarget.DOMAIN, 
				domainLowerBoundURI, domainUpperBoundURI);
	}
	
	@Override
	public void cancelSubscription(String subscriptionIdentifier) {
		userQueryManager.cancelSubscription(subscriptionIdentifier);
	}
	
	
	// CONTEXT DOMAIN MANAGEMENT
	////////////////////////////////////////////////////////////////////////////
	@Override
    public String getDomainValue() {
	    return domainValueURI;
    }
	
	public void setDomainValue(String domainValueURI) {
		this.domainValueURI = domainValueURI;
	}
}
