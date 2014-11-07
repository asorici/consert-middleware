package org.aimas.ami.cmm.agent.queryhandler;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.Property;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.config.QueryHandlerSpecification;
import org.aimas.ami.cmm.api.CMMConfigException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;


public class CtxQueryHandler extends CMMAgent {
    private static final long serialVersionUID = 4721794711775677133L;
    
    /* Application unique identifier, agent specification */
    private String appIdentifier;
    private QueryHandlerSpecification queryHandlerSpecification;
    
    private boolean connectedToCoordinator = false;
    
    /* Query Manager */
    private QueryManager queryManager;
    
	@Override
	public AgentType getAgentType() {
		return AgentType.CTX_QUERY_HANDLER;
	}
	
	QueryManager getQueryManager() {
		return queryManager;
	}
	
	boolean connectedToCoordinator() {
		return connectedToCoordinator;
	}
	
	void setConnectedToCoordinator(boolean connected) {
		connectedToCoordinator = connected;
	}
	
	@Override
	public void setup() {
		// ======== STEP 1:	retrieve the initialization arguments and set CMM agent language
    	Object[] initArgs = getArguments();
    	
    	// first argument is always the URI of the AgentSpecification resource in the 
    	String agentSpecURI = (String)initArgs[0];
    	
    	// second one is always the application unique identifier
    	appIdentifier = (String)initArgs[1];
    	
    	// third one is optional and denotes the AID of a local OrgMgr
    	if (initArgs.length == 3) {
    		localOrgMgr = (AID)initArgs[2];
    	}
    	
    	// register the CMMAgent-Lang ontology
    	registerCMMAgentLang();
    	
    	// ======== STEP 2a: configure the agent according to its specification	
    	try {
	    	// configure access to resource
    		doResourceAccessConfiguration();
    		
    		OntModel cmmConfigModel = configurationLoader.loadAgentConfiguration();
    		Resource agentSpecRes = cmmConfigModel.getResource(agentSpecURI);
    		
    		if (agentSpecRes == null) {
    			throw new CMMConfigException("No CtxQueryHandler specification found in configuration model "
    					+ "for URI " + agentSpecURI);
    		}
    		
    		// retrieve specification and create sensed assertions manager
    		agentSpecification = QueryHandlerSpecification.fromConfigurationModel(cmmConfigModel, agentSpecRes);
    		queryHandlerSpecification = (QueryHandlerSpecification)agentSpecification;
    		queryManager = new QueryManager(this);
    	}
    	catch(CMMConfigException e) {
    		// if we have a local OrgMgr we must signal our initialization failure
    		signalInitializationOutcome(false);
    		return;
    	}
    	
    	// ======== STEP 2b: if there is a local OrgMgr register with it as part of the DF
    	// A CMM agent will specify two attributes in its service description: app-name
    	// and agent-type.
    	registerQueryHandlerService();
    	
    	// ======== STEP 3:	setup the CtxQueryHandler specific permanent behaviours
    	setupQueryHandlerBehaviours();
    	
    	// ======== STEP 4: connect to the CtxCoord by announcing presence. 
    	// This step may in fact be more complex (involving an entire protocol to setup the CONSERT Engine QueryAdaptor), 
    	// when we will operate in a decentralized hierarchical - multi-coordinator setting, 
    	// but for this first version we only need to connect to the CtxCoord specified directly in the specification.
    	doConnectToCoordinator();
    	
    	// after this step initialization of the CtxSensor is complete, so we signal a successful init
		signalInitializationOutcome(true);
	}
	
	
	/**
     * If there is a local OrgMgr (which acts as DF) register the query handling service with it
     */
	private void registerQueryHandlerService() {
		Property primaryHandler = new Property("is-primary", queryHandlerSpecification.isPrimary());
	    registerAgentService(appIdentifier, new Property[] {primaryHandler});
    }
	
	
	private void setupQueryHandlerBehaviours() {
	    // Register the QueryReceiverBehaviour
		addBehaviour(new QueryReceiver(this));
		
		// Register the UserRegistrationBehaviour
		addBehaviour(new UserRegistrationBehaviour(this));
    }
	
	
	private void doConnectToCoordinator() {
	    addBehaviour(new ConnectToCoordinatorBehaviour(this));
    }
}
