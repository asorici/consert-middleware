package org.aimas.ami.cmm.agent.coordinator;

import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.RegisterCMMAgentInitiator;
import org.aimas.ami.cmm.agent.config.CoordinatorSpecification;
import org.aimas.ami.cmm.api.ApplicationControlAdaptor;
import org.aimas.ami.cmm.api.CMMConfigException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;


public class CtxCoord extends CMMAgent {
    private static final long serialVersionUID = 6296494181541059651L;
    
    /* agent specification */
    private CoordinatorSpecification coordinatorSpecification;
    
    /* Coordinator state and internal structures */
    private ContextUpdateManager contextUpdateManager;
    private QueryHandlerManager queryHandlerManager;
    private CommandManager commandManager;
    
    public ApplicationControlAdaptor getControlAdaptor() {
    	return commandManager;
    }
    
	@Override
	public AgentType getAgentType() {
		return AgentType.CTX_COORD;
	}
	
	public ContextUpdateManager getContextUpdateManager() {
		return contextUpdateManager;
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
	public void doAgentSpecificSetup(String agentSpecURI, String appIdentifier) {
    	// ======== STEP 1: configure the agent according to its specification	
    	try {
	    	OntModel cmmConfigModel = configurationLoader.loadAgentConfiguration();
    		Resource agentSpecRes = cmmConfigModel.getResource(agentSpecURI);
    		
    		if (agentSpecRes == null) {
    			throw new CMMConfigException("No CtxQueryHandler specification found in configuration model "
    					+ "for URI " + agentSpecURI);
    		}
    		
    		// retrieve specification and access the configured application adaptor service
    		agentSpecification = CoordinatorSpecification.fromConfigurationModel(cmmConfigModel, agentSpecRes);
    		coordinatorSpecification = (CoordinatorSpecification)agentSpecification;
    		assignedOrgMgr = coordinatorSpecification.getAssignedManagerAddress().getAID();
    		
    		setupManagementStructures();
    		
    		//System.out.println("["+ getName() + "]: " + "Management structures setup.");
    	}
    	catch(CMMConfigException e) {
    		// if we have a local OrgMgr we must signal our initialization failure
    		e.printStackTrace();
    		signalInitializationOutcome(false);
    		return;
    	}
    	
    	// STEP 2: register CtxCoord service
    	registerCoordinatorService();
    	
    	// ======== STEP 3:	setup the CtxUser specific permanent behaviours
    	setupCoordinatorBehaviours();
    	
    	System.out.println("["+ getName() + "]: " + "Done agent setup. Signaling outcome.");
    	
    	// after this step initialization of the CtxSensor is complete, so we signal a successful init
		signalInitializationOutcome(true);
	}
	
	private void registerCoordinatorService() {
		registerAgentService(appIdentifier, null);
    }
	
	
	@Override
    protected void registerWithAssignedManager() {
	    // all we have to do is register a behavior of type RegisterCMMAgentInitiator, if we have an `assigned' OrgMgr
		if (assignedOrgMgr != null) {
			addBehaviour(new RegisterCMMAgentInitiator(this));
		}
    }
	
	private void setupManagementStructures() throws CMMConfigException {
        // setup the SensorManager
		contextUpdateManager = new ContextUpdateManager(this);
		
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
		
		// Start the provisioning management service
		commandManager.setCommandRuleServiceActive(true);
    }

}
