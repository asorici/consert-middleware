package org.aimas.ami.cmm.agent.coordinator;

import jade.core.AID;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.config.CoordinatorSpecification;
import org.aimas.ami.cmm.api.ApplicationControlAdaptor;
import org.aimas.ami.cmm.exceptions.CMMConfigException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;


public class CtxCoord extends CMMAgent {
    private static final long serialVersionUID = 6296494181541059651L;
    
    /* Application unique identifier, agent specification */
    private String appIdentifier;
    private CoordinatorSpecification coordinatorSpecification;
    
    /* Coordinator state and internal structures */
    private SensorManager sensorManager;
    private QueryHandlerManager queryHandlerManager;
    private CommandManager commandManager;
    
    public ApplicationControlAdaptor getControlAdaptor() {
    	return commandManager;
    }
    
	@Override
	public AgentType getAgentType() {
		return AgentType.CTX_COORD;
	}
	
	public SensorManager getSensorManager() {
		return sensorManager;
	}

	public QueryHandlerManager getQueryHandlerManager() {
		return queryHandlerManager;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	// SETUP
	//////////////////////////////////////////////////////////////////////////
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
    	
    	System.out.println("Agent " + getName() + " starting: " + "spec: " + agentSpecURI + ", appId: " + appIdentifier + ", localOrgMgr: " + localOrgMgr);
    	
    	// register the CMMAgent-Lang ontology
    	registerCMMAgentLang();
    	
    	// ======== STEP 2a: configure the agent according to its specification	
    	try {
	    	// configure access to resource
    		doResourceAccessConfiguration();
    		
    		OntModel cmmConfigModel = configurationLoader.loadAppConfiguration();
    		Resource agentSpecRes = cmmConfigModel.getResource(agentSpecURI);
    		
    		if (agentSpecRes == null) {
    			throw new CMMConfigException("No CtxQueryHandler specification found in configuration model "
    					+ "for URI " + agentSpecURI);
    		}
    		
    		// retrieve specification and access the configured application adaptor service
    		agentSpecification = CoordinatorSpecification.fromConfigurationModel(cmmConfigModel, agentSpecRes);
    		coordinatorSpecification = (CoordinatorSpecification)agentSpecification;
    		
    		System.out.println("["+ getName() + "]: " + "Configuration has been parsed.");
    		
    		setupManagementStructures();
    		
    		System.out.println("["+ getName() + "]: " + "Management structures setup.");
    	}
    	catch(CMMConfigException e) {
    		// if we have a local OrgMgr we must signal our initialization failure
    		e.printStackTrace();
    		signalInitializationOutcome(false);
    		return;
    	}
    	
    	// STEP 2b: register CtxCoord service
    	registerCoordinatorService();
    	
    	// ======== STEP 3:	setup the CtxUser specific permanent behaviours
    	setupCoordinatorBehaviours();
    	
    	System.out.println("["+ getName() + "]: " + "Done agent setup. Signaling outcome.");
    	
    	// after this step initialization of the CtxSensor is complete, so we signal a successful init
		signalInitializationOutcome(true);
	}
	
	private void registerCoordinatorService() {
		registerAgentService(appIdentifier, null);
		System.out.println("["+ getName() + "]: " + "Coordinator service registered.");
    }

	private void setupManagementStructures() throws CMMConfigException {
        // setup the SensorManager
		sensorManager = new SensorManager(this);
		
		// setup the QueryHandler Manager
		queryHandlerManager = new QueryHandlerManager(this);
		
		// setup the CommandManager
		commandManager = new CommandManager(this, coordinatorSpecification);
    }
	
	private void setupCoordinatorBehaviours() {
        // Register sensor publish responder
		addBehaviour(new SensorPublishResponder(this));
		
		// Register the sensor update receiver
		addBehaviour(new SensorUpdateReceiver(this));
        
		// Register the QueryHandler registration behaviour
		addBehaviour(new RegisterQueryHandlerResponder(this));
		
		// Register the EnableAssertion responder
		addBehaviour(new EnableAssertionResponder(this));
		
		// Register the Provisioning Management Behaviour
		// TODO
    }
}
