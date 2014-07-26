package org.aimas.ami.cmm.agent.orgmgr;

import jade.core.AID;
import jade.core.ServiceException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.df;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.osgi.OSGIBridgeHelper;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.wrapper.AgentController;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.CtxCoord;
import org.aimas.ami.cmm.agent.CtxQueryHandler;
import org.aimas.ami.cmm.agent.CtxUser;
import org.aimas.ami.cmm.agent.config.ApplicationSpecification;
import org.aimas.ami.cmm.agent.config.CoordinatorSpecification;
import org.aimas.ami.cmm.agent.config.ManagerSpecification;
import org.aimas.ami.cmm.agent.config.QueryHandlerSpecification;
import org.aimas.ami.cmm.agent.config.SensorSpecification;
import org.aimas.ami.cmm.agent.config.UserSpecification;
import org.aimas.ami.cmm.agent.sensor.CtxSensor;
import org.aimas.ami.cmm.exceptions.CMMConfigException;
import org.aimas.ami.cmm.utils.AgentConfigLoader;
import org.aimas.ami.cmm.vocabulary.OrgConf;
import org.aimas.ami.contextrep.utils.BundleResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class OrgMgr extends df implements CMMInitListener {
    private static final long serialVersionUID = 8067387356505727111L;
    
    public static final String NICKNAME = "OrgMgr";
    
    
    /**
     * The helper that allows access to the OSGi framework that supports this CMM deployment.
     */
    OSGIBridgeHelper helper;
    Bundle resourceBundle;
    AgentConfigLoader configurationLoader;
    
    /** The Application specification */
    ApplicationSpecification appSpecification;
    
    /** The OrgMgr specification */
    ManagerSpecification mySpecification;
    
    /** The index and control of managed agents */
    CMMAgentManager agentManager = new CMMAgentManager();
    
    public void setup() {
    	try {
	    	// STEP 1: do sub-DF registration
	    	doSubDFRegistration();
	    	
	    	// STEP 2: retrieve the CMM configuration file and read it
	    	/* TODO: this step will most likely be reconsidered in the future. This is because we said that
	    	 * we want the OrgMgr TO BE ABLE TO MANAGE MULTIPLE APPLICATIONS AT A TIME. As such, he will not search
	    	 * only for a specific bundle where configuration files are found, but MOST LIKELY WILL LOOKUP AN 
	    	 * CMMAplicationManagementService WHICH WILL PROVIDE THE LIST OF APPLICATION CONFIGURATION BUNDLES INSTALLED
	    	 * IN THE CURRENT OSGi Runtime that he needs to inspect.
	    	 * 
	    	 * FOR THE INITIAL VERSION WE ARE DOING IT LIKE THIS, SIMPLY BECAUSE IT'S FASTER.
	    	 */
	    	doCMMConfiguration();
	    	
	    	// STEP 3: do behaviour registration
	    	doBehaviourRegistration();
	    	
	    	// STEP 4: start all the configured CMM agents
	    	doCMMInitialization();
    	}
    	catch(CMMConfigException e) {
    		System.out.println("Failed to initialize OrgMgr agent. Reason: " + e);
    	}
	}
	

	private void doCMMInitialization() {
		// STEP 2: start managed agents in required order: CtxCoord, CtxQueryHandler, CtxSensor, CtxUser.
		// We do this by registering a CMM initialization SequentialBehavior: in each child behavior the 
		// agent must send a confirmation of successful initialization
		CMMInitBehaviour initBehaviour = new CMMInitBehaviour(this, this);
		addBehaviour(initBehaviour);
    }
	
	@Override
    public void notifyInitResult(CMMInitResult initResult) {
	    if (!initResult.hasError()) {
	    	System.out.println("CMM INIT SUCCESSFUL!");
	    }
	    else {
	    	if (initResult.hasFaultyAgent()) {
	    		System.out.println("CMM INIT FAILED. No initialization confirmation received from " 
	    			+ initResult.getFaultyAgent().getAgentSpecification().getAgentName());
	    	}
	    	else {
	    		System.out.println("CMM INIT FAILED. Exception during initialization: " 
	    			+ initResult.getError());
	    	}
	    }
    }
	
	
	private void doBehaviourRegistration() {
	    // TODO Auto-generated method stub
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
			super.showGui();
			
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
	
	
	private void doCMMConfiguration() throws CMMConfigException {
		try {
	        helper = (OSGIBridgeHelper) getHelper(OSGIBridgeHelper.SERVICE_NAME);
        }
        catch (ServiceException e) {
        	throw new CMMConfigException("Failed to configure CMM. Could not access OSGi bridge helper.", e);
        } 
		
		BundleContext context = helper.getBundleContext();
		
		// Iterate through the installed bundles to find our "cmm-resources"
		for (Bundle candidate : context.getBundles()) {
			if (candidate.getSymbolicName().equals(CMMAgent.RESOURCE_BUNDLE_NAME)) {
				resourceBundle = candidate;
				break;
			}
		}
		
		// Now that we have found our resource bundle, setup the configuration loader 
		// and load the CMM configuration for located at the standard location: etc/cmm/cmm-config.ttl
		configurationLoader = new AgentConfigLoader(new BundleResourceManager(resourceBundle));
		OntModel cmmConfigModel = configurationLoader.loadAppConfiguration();
		
		// validate the configurationModel
		validateConfiguration(cmmConfigModel);
		
		// retrieve app specification - TODO: there will be more than one in the future
		appSpecification = ApplicationSpecification.fromConfigurationModel(cmmConfigModel);
		
		// retrieve own specification
		mySpecification = ManagerSpecification.fromConfigurationModel(cmmConfigModel);
		
		// build agent configuration and start agents
		try {
	        doManagedAgentConfiguration(cmmConfigModel, context);
        }
        catch (Exception e) {
        	throw new CMMConfigException("Failed to configure and initialize CMM Agents", e);
        }
    }
	
	
	private void doManagedAgentConfiguration(OntModel cmmConfigModel, BundleContext context) throws Exception {
	    // STEP 1: configure managed agents
		
		// access the Jade Runtime Service
		String jrsName = JadeRuntimeService.class.getName(); 
		ServiceReference jadeRef = context.getServiceReference(jrsName); 
		JadeRuntimeService jrs = (JadeRuntimeService) context.getService(jadeRef);
		
		// setup all CtxCoordinators - for starters we will only have one
		setupCoordinators(cmmConfigModel, context, jrs);
		
		// setup all CtxQueryHandlers
		setupQueryHandlers(cmmConfigModel, context, jrs);
		
		// setup all CtxSensors
		setupSensors(cmmConfigModel, context, jrs);
		
		// setup all CtxUsers
		setupUsers(cmmConfigModel, context, jrs);
    }

	
	private void setupCoordinators(OntModel cmmConfigModel, BundleContext context, JadeRuntimeService jrs) 
			throws Exception {
		// Every agent receives 2 mandatory argument and 1 optional one at initialization:
		// 		- 1: the URI that represents their Specification resource in the configuration file
		//		- 2: the application unique identifier
		//		- 3: (optional) the AID of the local OrgMgr
		AID orgMgrAID = mySpecification.getAgentAddress().getAID();
		String appIdentifier = appSpecification.getAppIdentifier();
		
		ResIterator coordinatorIt = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.CtxCoordSpec);
		for (;coordinatorIt.hasNext();) {
			Resource coordSpecRes = coordinatorIt.next();
			CoordinatorSpecification coordSpec = CoordinatorSpecification.fromConfigurationModel(cmmConfigModel, coordSpecRes);
			
			// Prepare the arguments
			Object[] args = new Object[] {coordSpecRes.getURI(), appIdentifier, orgMgrAID};
			
			AgentController coordController = 
					jrs.createNewAgent(coordSpec.getAgentName(), CtxCoord.class.getName(), args);
			
			agentManager.addManagedCoordinator(coordSpec.getAgentName(), coordSpec, coordController);
		}
    }
	
	
	private void setupQueryHandlers(OntModel cmmConfigModel, BundleContext context, JadeRuntimeService jrs) 
			throws Exception {
		// Every agent receives 2 mandatory argument and 1 optional one at initialization:
		// 		- 1: the URI that represents their Specification resource in the configuration file
		//		- 2: the application unique identifier
		//		- 3: (optional) the AID of the local OrgMgr
		AID orgMgrAID = mySpecification.getAgentAddress().getAID();
		String appIdentifier = appSpecification.getAppIdentifier();
		
		ResIterator queryHandlerIt = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.CtxQueryHandlerSpec);
		for (;queryHandlerIt.hasNext();) {
			Resource queryHandlerSpecRes = queryHandlerIt.next();
			QueryHandlerSpecification queryHandlerSpec = QueryHandlerSpecification.fromConfigurationModel(cmmConfigModel, queryHandlerSpecRes);
			
			// Prepare the arguments
			Object[] args = new Object[] {queryHandlerSpecRes.getURI(), appIdentifier, orgMgrAID};
			AgentController queryController = jrs.createNewAgent(queryHandlerSpec.getAgentName(), CtxQueryHandler.class.getName(), args);
			
			agentManager.addManagedQueryHandler(queryHandlerSpec.getAgentName(), queryHandlerSpec, queryController);
		}
    }
	
	
	private void setupSensors(OntModel cmmConfigModel, BundleContext context, JadeRuntimeService jrs) 
			throws Exception {
		// Every agent receives 2 mandatory argument and 1 optional one at initialization:
		// 		- 1: the URI that represents their Specification resource in the configuration file
		//		- 2: the application unique identifier
		//		- 3: (optional) the AID of the local OrgMgr
		AID orgMgrAID = mySpecification.getAgentAddress().getAID();
		String appIdentifier = appSpecification.getAppIdentifier();
		
		ResIterator sensorIt = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.CtxSensorSpec);
		for (;sensorIt.hasNext();) {
			Resource sensorSpecRes = sensorIt.next();
			SensorSpecification sensorSpec = SensorSpecification.fromConfigurationModel(cmmConfigModel, sensorSpecRes);
			
			// Prepare the arguments
			Object[] args = new Object[] {sensorSpecRes.getURI(), appIdentifier, orgMgrAID};
			AgentController sensorController = jrs.createNewAgent(sensorSpec.getAgentName(), CtxSensor.class.getName(), args);
			
			agentManager.addManagedSensor(sensorSpec.getAgentName(), sensorSpec, sensorController);
		}
    }
	
	
	private void setupUsers(OntModel cmmConfigModel, BundleContext context, JadeRuntimeService jrs) 
			throws Exception {
		// Every agent receives 2 mandatory argument and 1 optional one at initialization:
		// 		- 1: the URI that represents their Specification resource in the configuration file
		//		- 2: the application unique identifier
		//		- 3: (optional) the AID of the local OrgMgr
		AID orgMgrAID = mySpecification.getAgentAddress().getAID();
		String appIdentifier = appSpecification.getAppIdentifier();
		
		ResIterator userIt = cmmConfigModel.listResourcesWithProperty(RDF.type, OrgConf.CtxUserSpec);
		for (;userIt.hasNext();) {
			Resource userSpecRes = userIt.next();
			UserSpecification userSpec = UserSpecification.fromConfigurationModel(cmmConfigModel, userSpecRes);
			
			// Prepare the arguments
			Object[] args = new Object[] {userSpecRes.getURI(), appIdentifier, orgMgrAID};
			AgentController userController = jrs.createNewAgent(userSpec.getAgentName(), CtxUser.class.getName(), args);
			
			agentManager.addManagedUser(userSpec.getAgentName(), userSpec, userController);
		}
    }
	
	
	private void validateConfiguration(OntModel cmmConfigModel) throws CMMConfigException {
	    // TODO Implement validation
    }
	
	
	@Override
	protected void takeDown() {
	
	}
}
