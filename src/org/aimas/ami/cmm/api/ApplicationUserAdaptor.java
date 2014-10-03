package org.aimas.ami.cmm.api;

import org.aimas.ami.contextrep.engine.api.QueryResult;

import com.hp.hpl.jena.query.Query;

public interface ApplicationUserAdaptor {
	public static final String APP_IDENTIFIER_PROPERTY = "useradaptor.app-identifier";
	public static final String ADAPTOR_NAME = "useradaptor.name";
	
	public static enum QueryTarget {
		LOCAL("local"), DOMAIN("domain");
		
		private String name;
		
		QueryTarget(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	/* ==================== QUERY AND SUBSCRIPTION SUBMISSION ==================== */
	
	public QueryResult submitLocalQuery(Query query, long timeout) 
			throws DisconnectedQueryHandlerException;
	
	public QueryResult submitExactDomainQuery(Query query, String domainURI, long timeout)
			throws DisconnectedQueryHandlerException;
	
	public QueryResult submitDomainQuery(Query query, String domainLowerBoundURI, 
			String domainUpperBoundURI, long timeout) throws DisconnectedQueryHandlerException;
	
	public void submitLocalQuery(Query query, QueryNotificationHandler notificationHandler)
			throws DisconnectedQueryHandlerException;
	
	public void submitExactDomainQuery(Query query, QueryNotificationHandler notificationHandler, 
			String domainURI) throws DisconnectedQueryHandlerException;
	
	public void submitDomainQuery(Query query, QueryNotificationHandler notificationHandler, 
			String domainLowerBoundURI, String domainUpperBoundURI) throws DisconnectedQueryHandlerException;
	
	public String localSubscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval) throws DisconnectedQueryHandlerException;
	
	public String exactDomainSubscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval, String domainURI) throws DisconnectedQueryHandlerException;
	
	public String domainSubscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval, String domainLowerBoundURI, String domainUpperBoundURI) 
			throws DisconnectedQueryHandlerException;
	
	public void cancelSubscription(String subscriptionIdentifier);
	
	/* ======================== CONTEXT DOMAIN MANAGEMENT ======================== */
	/**
	 * Return the URI of the current Context Domain Value
	 * @return The URI of the current Context Domain Value, or null if no domain value is defined 
	 * (could be the case in the centralized-local deployment)
	 */
	public String getDomainValue();
}
