package org.aimas.ami.cmm.agent.queryhandler;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ACLMessage;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.RegisterCMMAgentInitiator;
import org.aimas.ami.cmm.agent.RegisterCMMAgentInitiator.RegisterCMMAgentNotifier;
import org.aimas.ami.cmm.agent.SearchCoordinatorInitiator;
import org.aimas.ami.cmm.agent.SearchCoordinatorInitiator.SearchCoordinatorListener;
import org.aimas.ami.cmm.agent.config.QueryHandlerSpecification;
import org.aimas.ami.cmm.api.CMMConfigException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;


public class CtxQueryHandler extends CMMAgent {
    private static final long serialVersionUID = 4721794711775677133L;
    
    /* Agent specification */
    private QueryHandlerSpecification queryHandlerSpecification;
    
    private boolean connectedToCoordinator = false;
    private AID ctxCoordinator;
    
    /* Query Manager */
    private QueryManager queryManager;
    
	@Override
	public AgentType getAgentType() {
		return AgentType.CTX_QUERY_HANDLER;
	}
	
	QueryManager getQueryManager() {
		return queryManager;
	}
	
	AID getConnectedCoordinator() {
		return ctxCoordinator;
	}
	
	boolean connectedToCoordinator() {
		if (ctxCoordinator == null)
			return false;
		
		return connectedToCoordinator;
	}
	
	void setConnectedToCoordinator(boolean connected) {
		connectedToCoordinator = connected;
	}
	
	@Override
	public void doAgentSpecificSetup(String agentSpecURI, String appIdentifier) {
		// ======== STEP 1: configure the agent according to its specification	
    	try {
    		OntModel cmmConfigModel = configurationLoader.loadAgentConfiguration();
    		Resource agentSpecRes = cmmConfigModel.getResource(agentSpecURI);
    		
    		if (agentSpecRes == null) {
    			throw new CMMConfigException("No CtxQueryHandler specification found in configuration model for URI " + agentSpecURI);
    		}
    		
    		// retrieve specification and create sensed assertions manager
    		agentSpecification = QueryHandlerSpecification.fromConfigurationModel(cmmConfigModel, agentSpecRes);
    		queryHandlerSpecification = (QueryHandlerSpecification)agentSpecification;
    		
    		if (queryHandlerSpecification.hasAssignedManagerAddress()) {
    			assignedOrgMgr = queryHandlerSpecification.getAssignedManagerAddress().getAID();
    		}
    		
    		queryManager = new QueryManager(this, appIdentifier);
    	}
    	catch(CMMConfigException e) {
    		// if we have a local OrgMgr we must signal our initialization failure
    		signalInitializationOutcome(false);
    		return;
    	}
    	
    	// ======== STEP 2: if there is a local OrgMgr register with it as part of the DF
    	// A CMM agent will specify two attributes in its service description: app-name
    	// and agent-type.
    	registerQueryHandlerService();
    	
    	// ======== STEP 3:	setup the CtxQueryHandler specific permanent behaviours
    	setupQueryHandlerBehaviours();
    	
    	// after this step initialization of the CtxSensor is complete, so we signal a successful init
		signalInitializationOutcome(true);
	}
	
	
	/** If there is a local OrgMgr (which acts as DF) register the query handling service with it */
	private void registerQueryHandlerService() {
		Property primaryHandler = new Property("is-primary", queryHandlerSpecification.isPrimary());
	    registerAgentService(appIdentifier, new Property[] {primaryHandler});
    }
	
	@Override
    protected void registerWithAssignedManager() {
		if (assignedOrgMgr != null) {
			// If there is an assigned OrgMgr we will find his associated coordinator agent
			// after we register with him as a CtxQueryHandler.
			addBehaviour(new RegisterCMMAgentInitiator(this, new RegisterCMMAgentNotifier() {
				@Override
				public void cmmAgentRegistered() {
					findCoordinator();
				}
			}));
		}
		else {
	    	// Find the CtxCoord with which this agent has to work
	    	findCoordinator();
		}
    }
	
	private void setupQueryHandlerBehaviours() {
	    // Register the QueryReceiverBehaviour
		addBehaviour(new QueryReceiver(this));
		
		// Register the UserRegistrationBehaviour
		addBehaviour(new RequesterRegistrationResponder(this));
    }
	
	
	private void findCoordinator() {
	    SearchCoordinatorListener listener = new SearchCoordinatorListener() {
			@Override
			public void coordinatorAgentNotFound(ACLMessage msg) {
				System.out.println(msg);
			}
			
			@Override
			public void coordinatorAgentFound(AID agentAID) {
				ctxCoordinator = agentAID;
				doConnectToCoordinator(ctxCoordinator);
			}
		};
		
		addBehaviour(new SearchCoordinatorInitiator(this, listener));
    }
	
	
	private void doConnectToCoordinator(AID coordinator) {
	    addBehaviour(new ConnectToCoordinatorBehaviour(this, coordinator));
    }
}
