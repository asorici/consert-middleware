package org.aimas.ami.cmm.agent.user;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.util.Event;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.onto.AssertionCapability;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.PublishAssertions;
import org.aimas.ami.cmm.agent.onto.UpdateEntityDescriptions;
import org.aimas.ami.cmm.agent.onto.UpdateProfiledAssertion;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionCapability;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionDescription;
import org.aimas.ami.cmm.agent.onto.impl.DefaultPublishAssertions;
import org.aimas.ami.cmm.agent.onto.impl.DefaultUpdateEntityDescriptions;
import org.aimas.ami.cmm.agent.onto.impl.DefaultUpdateProfiledAssertion;
import org.aimas.ami.cmm.agent.sensor.AssertionManager;
import org.aimas.ami.cmm.agent.sensor.PublishAssertionsBehaviour;
import org.aimas.ami.cmm.agent.sensor.SensingManager;
import org.aimas.ami.cmm.api.ApplicationUserAdaptor;
import org.aimas.ami.cmm.api.DisconnectedCoordinatorException;
import org.aimas.ami.cmm.api.DisconnectedQueryHandlerException;
import org.aimas.ami.cmm.api.QueryNotificationHandler;
import org.aimas.ami.cmm.sensing.ContextAssertionDescription;
import org.aimas.ami.contextrep.engine.api.QueryResult;
import org.aimas.ami.contextrep.vocabulary.ConsertCore;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataDelete;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateRequest;

public class CtxUserAdaptor implements ApplicationUserAdaptor {
	private CtxUser userAgent;
	
	private UserQueryManager userQueryManager;
	private SensingManager userSensingManager;
	
	private String contextDomainValueURI;
	private String contextDimensionURI;
	
	public CtxUserAdaptor(CtxUser userAgent) {
		this(userAgent, null, null);
	}
	
	public CtxUserAdaptor(CtxUser userAgent, String contextDimensionURI, String contextDomainValueURI) {
		this.userAgent = userAgent;
		this.userQueryManager = new UserQueryManager(userAgent);
		this.userSensingManager = new SensingManager(userAgent, null);
		
		this.contextDimensionURI = contextDimensionURI;
		this.contextDomainValueURI = contextDomainValueURI;
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
			throws DisconnectedQueryHandlerException {
		
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
    public String getContextDomainValue() {
	    return contextDomainValueURI;
    }
	
	public void setDomainValue(String domainValueURI) {
		this.contextDomainValueURI = domainValueURI;
	}
	
	@Override
    public String getContextDimension() {
	    return contextDimensionURI;
    }
	
	public void setContextDimension(String contextDimensionURI) {
		this.contextDimensionURI = contextDimensionURI;
	}
	
	
	// USER-GENERATED CONTEXT ASSERTION MANAGEMENT
	////////////////////////////////////////////////////////////////////////////
	public void setUserUpdateDestination(AID coordinatorAID) {
		userSensingManager.setAssertionUpdateDestination(coordinatorAID);
	}
	
	@Override
	public void registerUserSuppliedAssertion(String assertionResourceURI, String adaptorClassName, 
			String updateMode, int updateRate) throws DisconnectedCoordinatorException {
		
		if (userSensingManager.hasUpdateDestination()) {
			// Add the managed assertion to the userSensingManager
			userSensingManager.addManagedContextAssertion(assertionResourceURI, adaptorClassName, updateMode, updateRate);
		
			// Create a PublishAssertionsBehaviour that announces the new 
			PublishAssertions publishContent = new DefaultPublishAssertions();
			
			AssertionManager assertionManager = userSensingManager.getAssertionManager(assertionResourceURI);
			AssertionDescription assertionDesc = assertionManager.getAssertionDescription();
			
			if (assertionDesc != null) {
				AssertionCapability capability = new DefaultAssertionCapability();
				capability.setAssertion(assertionDesc);
				capability.setAvailableUpdateMode(updateMode);
				capability.setAvailableUpdateRate(updateRate);
				
				publishContent.addCapability(capability);
				
				ACLMessage publishMsg = new ACLMessage(ACLMessage.PROPOSE);
				publishMsg.addReceiver(userSensingManager.getAssertionUpdatesDestination());
				publishMsg.setLanguage(CMMAgent.cmmCodec.getName());
				publishMsg.setOntology(CMMAgent.cmmOntology.getName());
				
				try {
					Action publishAssertionsAction = new Action(userAgent.getAID(), publishContent);
					
			        userAgent.getContentManager().fillContent(publishMsg, publishAssertionsAction);
			        userAgent.addBehaviour(new PublishAssertionsBehaviour(userAgent, userSensingManager, publishMsg));
		        }
		        catch (CodecException e) {
			        e.printStackTrace();
		        }
		        catch (OntologyException e) {
			        e.printStackTrace();
		        }
			}
		}
		else {
			throw new DisconnectedCoordinatorException();
		}
	}
	
	@Override
	public void unregisterUserSuppliedAssertion(String assertionResourceURI) {
		// TODO: SEND UNPUBLISH message before removing from userSensingManager
		userSensingManager.removeManagedContextAssertion(assertionResourceURI);
	}
	
	/* ==================== PROFILED ASSERTIONS + ENTITY DESCRIPTION MANAGEMENT ==================== */
	@Override
	public void sendEntityDescriptions(Model addedDescriptionsModel, Model deletedDescriptionsModel) 
			throws DisconnectedCoordinatorException {
		
		if (userSensingManager.hasUpdateDestination()) {
			// Create the UpdateRequest from the statements in the entityDescriptions model
			UpdateRequest updateRequest = new UpdateRequest();
			if (deletedDescriptionsModel != null) {
				QuadDataAcc quads = new QuadDataAcc();
				Node entityStore = Node.createURI(ConsertCore.ENTITY_STORE_URI);
				
				for (StmtIterator it = deletedDescriptionsModel.listStatements(); it.hasNext();) {
					quads.addQuad(Quad.create(entityStore, it.next().asTriple()));
				}
				
				Update deleteUpdate = new UpdateDataDelete(quads);
				updateRequest.add(deleteUpdate);
			}
			
			if (addedDescriptionsModel != null) {
				QuadDataAcc quads = new QuadDataAcc();
				Node entityStore = Node.createURI(ConsertCore.ENTITY_STORE_URI);
				
				for (StmtIterator it = addedDescriptionsModel.listStatements(); it.hasNext();) {
					quads.addQuad(Quad.create(entityStore, it.next().asTriple()));
				}
				
				Update addedUpdate = new UpdateDataDelete(quads);
				updateRequest.add(addedUpdate);
			}
			
			if (!updateRequest.getOperations().isEmpty()) {
				UpdateEntityDescriptions entitiesUpdateContent = new DefaultUpdateEntityDescriptions();
				entitiesUpdateContent.setEntityContents(updateRequest.toString());
				
				UserUpdateStaticInitiator updateStaticBehaviour = new UserUpdateStaticInitiator(userAgent, entitiesUpdateContent);
				userAgent.addBehaviour(updateStaticBehaviour);
			}
		}
		else {
			throw new DisconnectedCoordinatorException();
		}
	}

	@Override
    public void sendProfiledAssertion(ContextAssertionDescription assertionDescription, 
    		UpdateRequest profiledAssertionUpdate) throws DisconnectedCoordinatorException {
		
		if (userSensingManager.hasUpdateDestination()) {
			// Create the UpdateProfiledAssertion message
	    	UpdateProfiledAssertion updateProfiledContent = new DefaultUpdateProfiledAssertion();
	    	updateProfiledContent.setAssertion(fromAdaptor(assertionDescription));
	    	updateProfiledContent.setAssertionContent(profiledAssertionUpdate.toString());
	    	
	    	UserUpdateProfiledInitiator updateProfiledBehaviour = new UserUpdateProfiledInitiator(userAgent, updateProfiledContent);
			userAgent.addBehaviour(updateProfiledBehaviour);
		}
		else {
			throw new DisconnectedCoordinatorException();
		}
    }
	
	private AssertionDescription fromAdaptor(ContextAssertionDescription desc) {
    	DefaultAssertionDescription info = new DefaultAssertionDescription();
    	info.setAssertionType(desc.getContextAssertionURI());
    	
    	for (String annotationURI : desc.getSupportedAnnotationURIs()) {
    		info.addAnnotationType(annotationURI);
    	}
    	
    	return info;
    }
	
	//* ==================== PROFILED ASSERTIONS + ENTITY DESCRIPTION BROADCAST ==================== */
	@Override
    public void broadcastProfiledAssertion(ContextAssertionDescription assertionDescription,
            UpdateRequest profiledAssertionUpdate, BroadcastTarget broadcastTarget, 
            String contextDomainLimit) throws DisconnectedCoordinatorException {
	    // TODO: future implementation
		throw new UnsupportedOperationException("Not implemented, yet.");
    }

	@Override
    public void broadcastEntityDescriptions(Model entityDescriptionsModel,
            BroadcastTarget broadcastTarget, String contextDomainLimit) throws DisconnectedCoordinatorException {
	    // TODO: future implementation
		throw new UnsupportedOperationException("Not implemented, yet.");
    }
}
