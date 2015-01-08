package org.aimas.ami.cmm.agent.coordinator;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.aimas.ami.cmm.CMMPlatformManager;
import org.aimas.ami.cmm.agent.AgentType;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.RegisterCMMAgentInitiator;
import org.aimas.ami.cmm.agent.config.ApplicationSpecification;
import org.aimas.ami.cmm.agent.config.ContextDomainSpecification;
import org.aimas.ami.cmm.agent.config.ContextModelDefinition;
import org.aimas.ami.cmm.agent.config.CoordinatorSpecification;
import org.aimas.ami.cmm.api.ApplicationControlAdaptor;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.contextrep.engine.api.CommandHandler;
import org.aimas.ami.contextrep.engine.api.EngineFrontend;
import org.aimas.ami.contextrep.engine.api.InsertionHandler;
import org.aimas.ami.contextrep.engine.api.QueryHandler;
import org.aimas.ami.contextrep.engine.api.StatsHandler;
import org.aimas.ami.contextrep.resources.CMMConstants;
import org.aimas.ami.contextrep.resources.SystemTimeService;
import org.aimas.ami.contextrep.resources.TimeService;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

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
    			throw new CMMConfigException("No CtxQueryHandler specification found in configuration model " + "for URI " + agentSpecURI);
    		}
    		
    		// retrieve specification and access the configured application adaptor service
    		agentSpecification = CoordinatorSpecification.fromConfigurationModel(cmmConfigModel, agentSpecRes);
    		coordinatorSpecification = (CoordinatorSpecification)agentSpecification;
    		
    		if (coordinatorSpecification.hasAssignedManagerAddress()) {
    			assignedOrgMgr = coordinatorSpecification.getAssignedManagerAddress().getAID();
    		}
    		
    		ApplicationSpecification appSpecification = ApplicationSpecification.fromConfigurationModel(cmmConfigModel);
    		
    		// create the CONSERT Engine component
    		createCONSERTEngine(appSpecification);
    		
    		// setup CtxCoord internal management structures
    		setupManagementStructures(appSpecification);
    		
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
			addBehaviour(new RegisterCMMAgentInitiator(this, null));
		}
    }
	
	
	private void createCONSERTEngine(ApplicationSpecification appSpecification) throws CMMConfigException {
		BundleContext bundleContext = CMMPlatformManager.getInstance().getBundleContext();
	    DependencyManager depMgr = CMMPlatformManager.getInstance().getDependencyManager();
	    
	    // Create the consertEngineServiceProps which include the applicationId and the URIs for the different
		// file modules that compose the domain context model 
	    Dictionary<String, String> consertEngineServiceProps = buildConsertEngineServiceProperties(appSpecification);
	    
	    depMgr.add(depMgr.createComponent()
				.setInterface(new String[] {InsertionHandler.class.getName(), QueryHandler.class.getName(), 
						StatsHandler.class.getName(), CommandHandler.class.getName()}, consertEngineServiceProps)
				.setImplementation(EngineFrontend.class)
				.setCallbacks("initEngine", "startEngine", "stopEngine", "closeEngine")
				.add(depMgr.createServiceDependency()
						.setService(TimeService.class, "(" 	+ CMMConstants.CONSERT_APPLICATION_ID_PROP + "=" 
															+ appSpecification.getAppIdentifier() + ")")
						.setRequired(true)
						.setAutoConfig("timeService")
				)
				/*
				.add(createServiceDependency()
						.setService(LogService.class)
						.setRequired(true)
						.setAutoConfig("logService")
				).add(createServiceDependency()
						.setService(LogReaderService.class)
						.setRequired(true)
						.setAutoConfig("logReaderService")
				)
				*/
			);
	    
	    // Now that we have created the CONSERT Engine, we want to wait for its initialization, so
	    // what we do is build a service tracker that just waits for the insertion service to be ready.
	    String engineServiceFilterStr 	= "(&" 	+ "(" + Constants.OBJECTCLASS + "=" + InsertionHandler.class.getName() + ")"
	    									+ "(" + CMMConstants.CONSERT_APPLICATION_ID_PROP + "=" + appSpecification.getAppIdentifier() + ")" + ")";
	    try {
	        Filter engineServiceFilter = bundleContext.createFilter(engineServiceFilterStr);
	        ServiceTracker<InsertionHandler, InsertionHandler> engineServiceTracker 
		    	= new ServiceTracker<InsertionHandler, InsertionHandler>(bundleContext, engineServiceFilter, null);
		    engineServiceTracker.open();
	    
	        InsertionHandler handler = engineServiceTracker.waitForService(12000);
	        if (handler == null) {
	        	throw new CMMConfigException("Could not instantiate CONSERT Engine component");
	        }
	    }
        catch (InvalidSyntaxException e) {
	        e.printStackTrace();
	        throw new CMMConfigException("Could not instantiate CONSERT Engine component. Bad Service filter syntax.", e);
        }
	    catch (InterruptedException e) {
	        throw new CMMConfigException("Could not instantiate CONSERT Engine component!", e);
        }
    }
	
	
	private Dictionary<String, String> buildConsertEngineServiceProperties(ApplicationSpecification appSpecification) 
			throws CMMConfigException {
		
		ContextDomainSpecification contextDomainSpec = appSpecification.getLocalContextDomain();
	    if (contextDomainSpec == null) {
	    	throw new CMMConfigException("Specification for application with appIdentifier " 
	    			+ appSpecification.getAppIdentifier() + " contains no context domain information!");
	    }
	    
	    ContextModelDefinition contextModelDef = contextDomainSpec.getDomainContextModelDefinition();
	    if (contextModelDef == null) {
	    	throw new CMMConfigException("Specification for application with appIdentifier " 
	    			+ appSpecification.getAppIdentifier() + " contains no context model information!");
	    }
		
	    // put the application-id property
		Dictionary<String, String> engineServiceProps = new Hashtable<String, String>();
	    engineServiceProps.put(CMMConstants.CONSERT_APPLICATION_ID_PROP, appSpecification.getAppIdentifier());
		
	    // put the provisioning-instance-bundle-id property
	    String contextDimensionURI = contextDomainSpec.hasDomainDimension() ? contextDomainSpec.getDomainDimension().getURI() : null;
	    String contextDomainValueURI = contextDomainSpec.hasDomainValue() ? contextDomainSpec.getDomainValue().getURI() : null;
	    
	    Bundle provisioningInstanceBundle = CMMPlatformManager.getInstance().getCMMInstanceResourceBundle(
	    		appSpecification.getAppIdentifier(), contextDimensionURI, contextDomainValueURI);
	    engineServiceProps.put(CMMConstants.CONSERT_INSTANCE_BUNDLE_ID, Long.toString(provisioningInstanceBundle.getBundleId()));
	    
	    // add all the context model file dictionary properties
	    Dictionary<String, String> contextModelFileProps = contextModelDef.getContextModelFileDictionary();
	    for (Enumeration<String> keyEnum = contextModelFileProps.keys(); keyEnum.hasMoreElements();) {
	    	String moduleKey = keyEnum.nextElement();
	    	engineServiceProps.put(moduleKey, contextModelFileProps.get(moduleKey));
	    }
	    
	    return engineServiceProps;
    }

	private void setupManagementStructures(ApplicationSpecification appSpecification) throws CMMConfigException {
        // First step is to retrieve all required interfaces to the CONSERT Engine
		BundleContext context = getOSGiBridge().getBundleContext();
		
		// Get insertion handler, command handler and stats handler
		ServiceReference<InsertionHandler> insertHandlerRef = null;
		ServiceReference<StatsHandler> statsHandlerRef = null;
		ServiceReference<CommandHandler> commandHandlerRef = null;
		
        try {
	        insertHandlerRef = context.getServiceReferences(InsertionHandler.class, 
	        		"(" + CMMConstants.CONSERT_APPLICATION_ID_PROP + "=" + appSpecification.getAppIdentifier() + ")").iterator().next();
	        if (insertHandlerRef == null) {
				throw new CMMConfigException("No reference found for CONSERT Engine service: " + InsertionHandler.class.getName());
			}
	        
	        statsHandlerRef = context.getServiceReferences(StatsHandler.class, 
	        		"(" + CMMConstants.CONSERT_APPLICATION_ID_PROP + "=" + appSpecification.getAppIdentifier() + ")").iterator().next();
	        if (statsHandlerRef == null) {
				throw new CMMConfigException("No reference found for CONSERT Engine service: " + StatsHandler.class.getName());
			}
	        
	        commandHandlerRef = context.getServiceReferences(CommandHandler.class, 
	        		"(" + CMMConstants.CONSERT_APPLICATION_ID_PROP + "=" + appSpecification.getAppIdentifier() + ")").iterator().next();
	        if (commandHandlerRef == null) {
				throw new CMMConfigException("No reference found for CONSERT Engine service: " + CommandHandler.class.getName());
			}
        }
        catch (InvalidSyntaxException e) {
	        e.printStackTrace();
	        throw new CMMConfigException(e);
        }
		
        InsertionHandler insertionHandler = context.getService(insertHandlerRef);
        StatsHandler statsHandler = context.getService(statsHandlerRef);
        CommandHandler commandHandler = context.getService(commandHandlerRef);
		
		// setup the SensorManager
		contextUpdateManager = new ContextUpdateManager(this, insertionHandler);
		
		// setup the QueryHandler Manager
		queryHandlerManager = new QueryHandlerManager(this);
		
		// setup the CommandManager
		commandManager = new CommandManager(this, coordinatorSpecification, commandHandler, statsHandler);
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
