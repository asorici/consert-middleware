package org.aimas.ami.cmm.api;

import org.aimas.ami.cmm.sensing.ContextAssertionDescription;
import org.aimas.ami.contextrep.engine.api.QueryResult;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateRequest;

public interface ApplicationUserAdaptor {
	public static final String APP_IDENTIFIER_PROPERTY 	= "useradaptor.app-identifier";
	public static final String ADAPTOR_NAME 			= "useradaptor.name";
	
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
	
	public static enum BroadcastTarget {
		ALL, SIBLINGS, UP, DOWN
	}
	
	/* ==================== QUERY AND SUBSCRIPTION SUBMISSION ==================== */
	public QueryResult submitLocalQuery(Query query, long timeout) 
			throws DisconnectedQueryHandlerException;
	
	public void submitLocalQuery(Query query, QueryNotificationHandler notificationHandler)
			throws DisconnectedQueryHandlerException;
	
	public QueryResult submitExactDomainQuery(Query query, String domainURI, long timeout)
			throws DisconnectedQueryHandlerException;
	
	public void submitExactDomainQuery(Query query, QueryNotificationHandler notificationHandler, 
			String domainURI) throws DisconnectedQueryHandlerException;
	
	public QueryResult submitDomainRangeQuery(Query query, String domainTypeLowerBoundURI, 
			String domainTypeUpperBoundURI, long timeout) throws DisconnectedQueryHandlerException;
	
	public void submitDomainRangeQuery(Query query, QueryNotificationHandler notificationHandler, 
			String domainTypeLowerBoundURI, String domainTypeUpperBoundURI) throws DisconnectedQueryHandlerException;
	
	public String localSubscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval) throws DisconnectedQueryHandlerException;
	
	public String exactDomainSubscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval, String domainURI) throws DisconnectedQueryHandlerException;
	
	public String domainRangeSubscribe(Query query, QueryNotificationHandler notificationHandler, 
			int repeatInterval, String domainTypeLowerBoundURI, String domainTypeUpperBoundURI) 
			throws DisconnectedQueryHandlerException;
	
	public void cancelSubscription(String subscriptionIdentifier);
	
	/* ======================== CONTEXT DOMAIN MANAGEMENT ======================== */
	/**
	 * Return the URI of the current ContextDomain Value. The ContextDomain Value is for which this adaptor is
	 * responsible is either defined in the CMM Agent-configuration of this CONSERT CMM instance, or it is
	 * determined dynamically in case of a mobile node. 
	 * @return The URI of the current Context Domain Value, or null if no domain value is defined 
	 * (could be the case in the centralized-local deployment)
	 */
	public String getContextDomainValue();
	
	/**
	 * Return the URI of the ContextDimension for which this adaptor is used. 
	 * @return The URI of the current ContextDimension, or null if no domain value is defined 
	 * (could be the case in the centralized-local deployment)
	 */
	public String getContextDimension();
	
	/* ==================== USER-GENERATED CONTEXT ASSERTION MANAGEMENT ==================== */
	/**
	 * Register a ContextAssertionAdaptor (specified by <code>assertionAdaptorClass</code>) which can provide updates 
	 * for the <i>sensed</i> ContextAssertion identified by <code>assertionResourceURI</code>. 
	 * 
	 * Note that the assertion adaptor instance must have been deployed as an OSGi service prior to making this call.
	 *  
	 * The updates of the assertion adaptor will be directed to the CtxCoord agent in charge of the 
	 * context provisioning for the current ContextDimensions + ContextDomain. 
	 * 
	 * @param assertionResourceURI	The URI of the supplied ContextAssertion 
	 * @param assertionAdaptorClass	The implementation class of the ContextAssertionAdaptor instance which will
	 * 	be supplying the updates for the specified ContextAssertion
	 * @param updateMode	The default update mode (change-based or time-based) for the supplied ContextAssertion
	 * @param updateRate	The default update rate for the supplied ContextAssertion (0 if change-based)
	 */
	public void registerUserSuppliedAssertion(String assertionResourceURI, String assertionAdaptorClass, 
			String updateMode, int updateRate) throws DisconnectedCoordinatorException;
	
	/**
	 * Stop supplying updates of the ContextAssertion identified by the <code>assertionResourceURI</code>.
	 * @param assertionResourceURI
	 */
	public void unregisterUserSuppliedAssertion(String assertionResourceURI);
	
	
	/* ==================== PROFILED ASSERTIONS + ENTITY DESCRIPTION MANAGEMENT ==================== */
	/**
	 * Send a set of static EntityDescription information to the CtxCoord agent in charge of the 
	 * context provisioning for the current ContextDimensions + ContextDomain. The information to be sent is
	 * contained within two Jena RDF Models: a set of statements to be added and one to be deleted.
	 * @param addedDescriptionsModel The JENA RDF Model containing the EntityDescription statements to be added. 
	 * 		May be null.
	 * @param deletedDescriptionsModel The JENA RDF Model containing the EntityDescription statements to be deleted. 
	 * 		May be null. 
	 */
	public void sendEntityDescriptions(Model addedDescriptionsModel, Model deletedDescriptionsModel) throws DisconnectedCoordinatorException;
	
	/**
	 * Send a profiled ContextAssertion update to the CtxCoord agent in charge of the 
	 * context provisioning for the current ContextDimensions + ContextDomain.
	 * @param assertionDescription	The description (assertion type + annotation types) of the updated ContextAssertion.
	 * @param profiledAssertionUpdate	The named graph based SPARQL encoded assertion update contents. 
	 */
	public void sendProfiledAssertion(ContextAssertionDescription assertionDescription, 
			UpdateRequest profiledAssertionUpdate) throws DisconnectedCoordinatorException;
	
	//* ==================== PROFILED ASSERTIONS + ENTITY DESCRIPTION BROADCAST ==================== */
	/**
	 * Broadcast a profiled ContextAssertion update along the ContextDomain hierarchy, to all CtxCoord agents that 
	 * conform to the ContextDomain <i>type</i> limits set by <code>domainTypeLowerBound</code> and <code>domainTypeUpperBound</code>.
	 * @param assertionDescription	The description (assertion type + annotation types) of the updated ContextAssertion.
	 * @param profiledAssertionUpdate	The named graph based SPARQL encoded assertion update contents.
	 * @param domainTypeLowerBoundURI 	The URI of the ContextDomain <i>type</i> representing the lower bound on the dissemination depth in the domain hierarchy, 
	 * 		or null if no suh limit is set. 
	 * @param domainTypeUpperBoundURI	The URI of the ContextDomain <i>type</i> representing the upper bound on the dissemination height in the domain hierarchy, 
	 * 		or null if no such limit is set.  
	 */
	public void broadcastProfiledAssertion(ContextAssertionDescription assertionDescription, 
			UpdateRequest profiledAssertionUpdate, String domainTypeLowerBoundURI, String domainTypeUpperBoundURI) throws DisconnectedCoordinatorException;
	
	/**
	 * Broadcast EntityDescription information along the ContextDomain hierarchy, to all CtxCoord agents that 
	 * conform to the ContextDomain <i>type</i> limits set by <code>domainTypeLowerBound</code> and <code>domainTypeUpperBound</code>.
	 * @param addedDescriptionsModel The JENA RDF Model containing the EntityDescription statements to be added. 
	 * 		May be null.
	 * @param deletedDescriptionsModel The JENA RDF Model containing the EntityDescription statements to be deleted. 
	 * 		May be null. 
	 * @param domainTypeLowerBoundURI 	The URI of the ContextDomain <i>type</i> representing the lower bound on the dissemination depth in the domain hierarchy, 
	 * 		or null if no suh limit is set. 
	 * @param domainTypeUpperBoundURI	The URI of the ContextDomain <i>type</i> representing the upper bound on the dissemination height in the domain hierarchy, 
	 * 		or null if no such limit is set.
	 */
	public void broadcastEntityDescriptions(Model addedDescriptionsModel, Model deletedDescriptionsModel,
			String domainTypeLowerBoundURI, String domainTypeUpperBoundURI) throws DisconnectedCoordinatorException;
}
