package org.aimas.ami.cmm.agent.orgmgr;

import jade.core.AID;
import jade.core.ServiceException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.df;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.Event;
import jade.wrapper.AgentController;

import java.util.Collection;

import org.aimas.ami.cmm.CMMPlatformRequestExecutor;
import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.config.AgentAddress;
import org.aimas.ami.cmm.agent.config.ApplicationSpecification;
import org.aimas.ami.cmm.agent.config.CoordinatorSpecification;
import org.aimas.ami.cmm.agent.config.ManagerSpecification;
import org.aimas.ami.cmm.agent.config.ManagerSpecification.ManagerType;
import org.aimas.ami.cmm.agent.config.QueryHandlerSpecification;
import org.aimas.ami.cmm.agent.config.SensorSpecification;
import org.aimas.ami.cmm.agent.config.UserSpecification;
import org.aimas.ami.cmm.agent.coordinator.CtxCoord;
import org.aimas.ami.cmm.agent.onto.RegisterManager;
import org.aimas.ami.cmm.agent.osgi.JadeOSGIBridgeHelper;
import org.aimas.ami.cmm.agent.queryhandler.CtxQueryHandler;
import org.aimas.ami.cmm.agent.sensor.CtxSensor;
import org.aimas.ami.cmm.agent.user.CtxUser;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.utils.AgentConfigLoader;
import org.aimas.ami.cmm.vocabulary.OrgConf;
import org.aimas.ami.contextrep.resources.CMMConstants;
import org.aimas.ami.contextrep.resources.TimeService;
import org.aimas.ami.contextrep.utils.BundleResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class OrgMgr extends df implements CMMPlatformRequestExecutor {
	private static final long serialVersionUID = 8067387356505727111L;
    
	public static final int INSTALL_REQUEST 	= 	0;
	public static final int START_REQUEST 		= 	1;
	public static final int STOP_REQUEST 		= 	2;
	public static final int UNINSTALL_REQUEST 	= 	3;
	
    public static final String NICKNAME = "OrgMgr";
    
    /* The helper that allows access to the OSGi framework that supports this CMM deployment. */
    JadeOSGIBridgeHelper helper;
    AgentConfigLoader configurationLoader;
    
    /* Application and Context Domain Information */
    ApplicationSpecification appSpecification;
    String cmmBundleLocation;
    
    /* The OrgMgr specification */
    ManagerSpecification mySpecification;
    ManagerType myType = ManagerType.Central;
    
    /* The index and control of managed agents */
    CMMAgentManager agentManager = new CMMAgentManager();
    DomainManagementHelper domainManager;
    
    @Override
    public void setup() {
    	
		// STEP 1: retrieve the initialization arguments
    	Object[] initArgs = getArguments();
    	cmmBundleLocation = (String)initArgs[0];
    	myType = (ManagerType)initArgs[1];
    	Event initializationReadyEvent = (Event)initArgs[2];
    	
		// STEP 2: do sub-DF registration and register as the implementation for the CMMPlatformInterface
    	registerO2AInterface(CMMPlatformRequestExecutor.class, this);
    	doSubDFRegistration();    	
    	
    	// STEP 3: do behaviour registration
    	getContentManager().registerLanguage(CMMAgent.cmmCodec);
    	getContentManager().registerOntology(CMMAgent.cmmOntology);
    	doBehaviourRegistration();
    	
    	// STEP 4: signal that the OrgMgr is set up
    	initializationReadyEvent.notifyProcessed(null);
	}
	
	
	private void doBehaviourRegistration() {
	    // 1) Domain Inform Responder
		addBehaviour(new DomainInformResponder(this));
		
		// 2) CMMAgent Registration Responder
		addBehaviour(new RegisterCMMAgentResponder(this));
		
		// 3) Related OrgMgr Registration Responder
		addBehaviour(new RegisterManagerResponder(this));
		
		// 4) Search Coordinator Responder
		addBehaviour(new SearchCoordinatorResponder(this));
		
		// 5) Related OrgMgr Registration Responder
		addBehaviour(new SearchQueryHandlerResponder(this));
		
		// 6) Resolve Query Base Responder
		addBehaviour(new ResolveQueryBaseResponder(this));
    }


	private void doSubDFRegistration() {
		try {
			AID parentName = getDefaultDF();
			
			// Execute the setup of jade.domain.df which includes all the
			// default behaviours of a df (i.e. register, unregister,modify, and search).
			super.setup();
			
			// Use this method to modify the current description of this df.
			setDescriptionOfThisDF(getOrgMgrDFDescription());
			
			// Show the default Gui of a df.
			//super.showGui();
			
			DFService.register(this, parentName, getOrgMgrDFDescription());
			addParent(parentName, getOrgMgrDFDescription());
			System.out.println("Agent: " + getName() + " federated with default df.");
			
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
    }

	private DFAgentDescription getOrgMgrDFDescription() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName() + "-sub-df");
		sd.setType("fipa-df");
		sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
		sd.addOntologies("fipa-agent-management");
		sd.setOwnership("JADE");
		dfd.addServices(sd);
		return dfd;
	}
	
	
	private void doCMMConfiguration(String cmmBundleLocation) throws CMMConfigException {
		try {
	        helper = (JadeOSGIBridgeHelper) getHelper(JadeOSGIBridgeHelper.SERVICE_NAME);
        }
        catch (ServiceException e) {
        	throw new CMMConfigException("Failed to configure CMM. Could not access OSGi bridge helper.", e);
        } 
		
		BundleContext context = helper.getBundleContext();
		Bundle cmmInstanceResourceBundle = context.getBundle(cmmBundleLocation);
		if (cmmInstanceResourceBundle == null) {
			throw new CMMConfigException("Could not find any CONSERT Middleware configuration bundle at OSGi plaform location: " + cmmBundleLocation);
		}
		
		// Now that we have found our resource bundle, setup the configuration loader 
		// and load the CMM configuration for located at the standard location: etc/cmm/cmm-config.ttl
		configurationLoader = new AgentConfigLoader(new BundleResourceManager(cmmInstanceResourceBundle));
		OntModel cmmConfigModel = configurationLoader.loadAgentConfiguration();
		
		// validate the configurationModel
		validateConfiguration(cmmConfigModel);
		
		// retrieve the application specification and my own specification (if one exists)
		appSpecification = ApplicationSpecification.fromConfigurationModel(cmmConfigModel);
		mySpecification = ManagerSpecification.fromConfigurationModel(cmmConfigModel);
		
		// set the provisioning group instance-specific TimeService, which must be defined in the default
		// provisioning group bundle
		String timeServiceFilter = "(" + CMMConstants.CONSERT_APPLICATION_ID_PROP + "=" + appSpecification.getAppIdentifier() + ")";
		try {
	        Collection<ServiceReference<TimeService>> timeServiceRefs = context.getServiceReferences(TimeService.class, timeServiceFilter);
	        if (!timeServiceRefs.isEmpty()) {
	        	// We know there will normally be only one TimeService instance that corresponds to our filter,
	        	// so we take the first one
	        	TimeService timeService = context.getService(timeServiceRefs.iterator().next());
	        	CMMAgent.setTimeService(timeService);
	        }
	        else {
	        	throw new CMMConfigException("No TimeService instance matches the configuration requirement for " 
	        			+ appSpecification.getAppIdentifier() + " context provisioning group.");
	        }
		}
        catch (InvalidSyntaxException e) {
	        e.printStackTrace();
	        throw new CMMConfigException("Failed to configure TimeService for agent of context provisioning group.", e);
        }
		
		// build agent configuration and start agents
		try {
	        doManagedAgentConfiguration(cmmConfigModel, context);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	throw new CMMConfigException("Failed to configure and initialize CMM Agents", e);
        }
		
		// instantiate the domain management helper and register behaviours that connect the OrgMgr
		// to other domain manager agents
		domainManager = new DomainManagementHelper(this, appSpecification.getLocalContextDomain());
		if (myType == ManagerType.Node && mySpecification.getParentManagerAddress() != null) {
			AID targetManager = mySpecification.getParentManagerAddress().getAID();
			addBehaviour(new RegisterManagerInitiator(this, RegisterManager.CHILD, targetManager));
		}
		else if (myType == ManagerType.Root && mySpecification.getKnownRootManagers() != null) {
			for (AgentAddress knownRootMgr : mySpecification.getKnownRootManagers()) {
				addBehaviour(new RegisterManagerInitiator(this, RegisterManager.ROOT, knownRootMgr.getAID()));
			}
		}
    }
	
	
	private void validateConfiguration(OntModel cmmConfigModel) throws CMMConfigException {
	    // TODO Implement validation
    }
	
	
	private void doManagedAgentConfiguration(OntModel cmmConfigModel, BundleContext context) throws Exception {
	    // STEP 1: configure managed agents
		// setup all CtxCoordinators - for starters we will only have one
		setupCoordinator(cmmConfigModel, context);
		
		// setup all CtxQueryHandlers
		setupQueryHandlers(cmmConfigModel, context);
		
		// setup all CtxSensors
		setupSensors(cmmConfigModel, context);
		
		// setup all CtxUsers
		setupUser(cmmConfigModel, context);
    }

	
	private void setupCoordinator(OntModel cmmConfigModel, BundleContext context) throws Exception {
		// Every agent receives 4 arguments:
		//		- 1: the OSGi Platform relative CMM Instance Resource Bundle location
		// 		- 2: the URI that represents their Specification resource in the configuration file
		//		- 3: the application unique identifier
		//		- 4: the AID of the local OrgMgr (the one that instantiates them)
		AID orgMgrAID = getAID();
		
		
		ResIterator coordinatorIt = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.CtxCoordSpec);
		if (coordinatorIt.hasNext()) {
			Resource coordSpecRes = coordinatorIt.next();
			CoordinatorSpecification coordSpec = CoordinatorSpecification.fromConfigurationModel(cmmConfigModel, coordSpecRes);
			String agentLocalName = coordSpec.getAgentLocalName();
			Resource contextDomainValue = appSpecification.getLocalContextDomain().getDomainValue();
			
			// Prepare the arguments
			Object[] args = null;
			if (contextDomainValue == null) {
				args = new Object[] {cmmBundleLocation, coordSpecRes.getURI(), appSpecification.getAppIdentifier(), orgMgrAID};
			}
			else {
				args = new Object[] {cmmBundleLocation, coordSpecRes.getURI(), appSpecification.getAppIdentifier(), orgMgrAID, 
						contextDomainValue.getURI()};
			}
			
			AgentController coordController = getContainerController().createNewAgent(agentLocalName, CtxCoord.class.getName(), args);
			agentManager.setManagedCoordinator(coordSpec, coordController);
		}
    }
	
	
	private void setupQueryHandlers(OntModel cmmConfigModel, BundleContext context) throws Exception {
		// Every agent receives 4 arguments:
		//		- 1: the OSGi Platform relative CMM Instance Resource Bundle location
		// 		- 2: the URI that represents their Specification resource in the configuration file
		//		- 3: the application unique identifier
		//		- 4: the AID of the local OrgMgr (the one that instantiates them)
		AID orgMgrAID = getAID();
		
		ResIterator queryHandlerIt = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.CtxQueryHandlerSpec);
		for (;queryHandlerIt.hasNext();) {
			Resource queryHandlerSpecRes = queryHandlerIt.next();
			QueryHandlerSpecification queryHandlerSpec = QueryHandlerSpecification.fromConfigurationModel(cmmConfigModel, queryHandlerSpecRes);
			String agentLocalName = queryHandlerSpec.getAgentLocalName();
			Resource contextDomainValue = appSpecification.getLocalContextDomain().getDomainValue();
			
			// Prepare the arguments
			Object[] args = null;
			if (contextDomainValue == null) {
				args = new Object[] {cmmBundleLocation, queryHandlerSpecRes.getURI(), appSpecification.getAppIdentifier(), orgMgrAID};
			}
			else {
				args = new Object[] {cmmBundleLocation, queryHandlerSpecRes.getURI(), appSpecification.getAppIdentifier(), orgMgrAID, 
						contextDomainValue.getURI()};
			}
			
			AgentController queryController = getContainerController().createNewAgent(agentLocalName, CtxQueryHandler.class.getName(), args);
			agentManager.addManagedQueryHandler(queryHandlerSpec, queryController);
		}
    }
	
	
	private void setupSensors(OntModel cmmConfigModel, BundleContext context) throws Exception {
		// Every agent receives 4 arguments:
		//		- 1: the OSGi Platform relative CMM Instance Resource Bundle location
		// 		- 2: the URI that represents their Specification resource in the configuration file
		//		- 3: the application unique identifier
		//		- 4: the AID of the local OrgMgr (the one that instantiates them)
		AID orgMgrAID = getAID();
		
		ResIterator sensorIt = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.CtxSensorSpec);
		for (;sensorIt.hasNext();) {
			Resource sensorSpecRes = sensorIt.next();
			SensorSpecification sensorSpec = SensorSpecification.fromConfigurationModel(cmmConfigModel, sensorSpecRes);
			String agentLocalName = sensorSpec.getAgentLocalName();
			
			// Prepare the arguments
			Object[] args = new Object[] {cmmBundleLocation, sensorSpecRes.getURI(), appSpecification.getAppIdentifier(), orgMgrAID};
			AgentController sensorController = getContainerController().createNewAgent(agentLocalName, CtxSensor.class.getName(), args);
			
			agentManager.addManagedSensor(appSpecification.getAppIdentifier(), sensorSpec, sensorController);
		}
    }
	
	
	private void setupUser(OntModel cmmConfigModel, BundleContext context) throws Exception {
		// Every agent receives 4 arguments:
		//		- 1: the OSGi Platform relative CMM Instance Resource Bundle location
		// 		- 2: the URI that represents their Specification resource in the configuration file
		//		- 3: the application unique identifier
		//		- 4: the AID of the local OrgMgr (the one that instantiates them)
		AID orgMgrAID = getAID();
		
		ResIterator userIt = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.CtxUserSpec);
		if (userIt.hasNext()) {
			Resource userSpecRes = userIt.next();
			UserSpecification userSpec = UserSpecification.fromConfigurationModel(cmmConfigModel, userSpecRes);
			String agentLocalName = userSpec.getAgentLocalName();
			
			// Prepare the arguments
			Object[] args = new Object[] {cmmBundleLocation, userSpecRes.getURI(), appSpecification.getAppIdentifier(), orgMgrAID};
			AgentController userController = getContainerController().createNewAgent(agentLocalName, CtxUser.class.getName(), args);
			
			agentManager.setManagedUser(appSpecification.getAppIdentifier(), userSpec, userController);
		}
    }
	
	
	private void doCMMInit(Event initCMMEvent) {
		// STEP 2: start managed agents in required order: CtxCoord, CtxQueryHandler, CtxSensor, CtxUser.
		// We do this by registering a CMM initialization SequentialBehavior: in each child behavior the 
		// agent must send a confirmation of successful initialization
		//System.out.println("Registering CMM INIT Behaviour");
		
		CMMInitBehaviour initBehaviour = new CMMInitBehaviour(this, initCMMEvent);
		addBehaviour(initBehaviour);
    }
	
	
	private void doCMMStart(Event startCMMEvent) {
	    // TODO Do an actual start
	    startCMMEvent.notifyProcessed(new CMMOpEventResult());
    }
	
	
	private void doCMMStop(Event stopCMMEvent) {
	    // TODO Do an actual stop
		stopCMMEvent.notifyProcessed(new CMMOpEventResult());
    }
	
	
	private void doCMMKill(Event killCMMEvent) {
	    // TODO Do an actual kill
		killCMMEvent.notifyProcessed(new CMMOpEventResult());
    }
	
	
	
	@Override
	protected void takeDown() {
	
	}

	// CMM Instance Request Management
	///////////////////////////////////////////////////////////////////////////////////////////
	@Override
    public Event createCMMInstance() {
    	Event initCMMEvent = new Event(INSTALL_REQUEST, this);
		try {
	        doCMMConfiguration(cmmBundleLocation);
        }
        catch (CMMConfigException e) {
        	initCMMEvent.notifyProcessed(new CMMOpEventResult(e));
        }
    	
    	doCMMInit(initCMMEvent);
    	
    	return initCMMEvent;
    }


	@Override
    public Event startCMMInstance() {
		Event startCMMEvent = new Event(START_REQUEST, this);
	    
		doCMMStart(startCMMEvent);
		return startCMMEvent;
    }
	

	@Override
    public Event stopCMMInstance() {
		Event stopCMMEvent = new Event(STOP_REQUEST, this);
	    
		doCMMStop(stopCMMEvent);
		return stopCMMEvent;
    }


	@Override
    public Event killCMMInstance() {
		Event killCMMEvent = new Event(UNINSTALL_REQUEST, this);
	    
		doCMMKill(killCMMEvent);
		return killCMMEvent;
    }
}
