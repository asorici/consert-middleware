package org.aimas.ami.cmm.agent;

import jade.osgi.service.agentFactory.AgentFactoryService;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.wrapper.AgentController;

import org.aimas.ami.cmm.agent.config.ApplicationSpecification;
import org.aimas.ami.cmm.agent.config.CMMAgentContainer;
import org.aimas.ami.cmm.agent.config.ManagerSpecification;
import org.aimas.ami.cmm.agent.orgmgr.OrgMgr;
import org.aimas.ami.cmm.exceptions.CMMConfigException;
import org.aimas.ami.cmm.utils.AgentConfigLoader;
import org.aimas.ami.contextrep.resources.SystemTimeService;
import org.aimas.ami.contextrep.resources.TimeService;
import org.aimas.ami.contextrep.utils.BundleResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import com.hp.hpl.jena.ontology.OntModel;

public class AgentActivator implements BundleActivator {
	public static final String RESOURCE_BUNDLE_SYMBOLIC_NAME = "consert-engine.resources";
	public static final String JADE_BUNDLE_SYMBOLIC_NAME = "jade.jadeOsgi";
	public static final String AGENT_BUNDLE_SYMBOLIC_NAME = "consert-middleware.agent-bundle";
	
	private AgentFactoryService agentFactory;
	
	
	@Override
    public void start(BundleContext context) throws Exception {
		agentFactory = new AgentFactoryService(); 
		agentFactory.init(context.getBundle());
		
		/* The jadeOSGi bundle will not be started by default - we manage it from here.
		 * Basically, the CMM-agent bundle is the one that controls CMM deploy.
		 * 
		 * The first step is to create an AgentConfigLoader and load the ApplicationSpecification; it is
		 * here that we find the parameters for the JADE agent container and platform name. We need to set
		 * these before starting the JADE bundle.
		 */
		
		// ======== STEP 1: Iterate through the installed bundles to find our "cmm-resources" ========
		
		// TODO: This will likely change in the future, because if we want to handle several context-aware
		// applications using the same CMM instance, we need an additional mechanism to manage all the
		// installed apps, i.e. there will be several "cmm-resources" bundles.
		Bundle resourceBundle = null;
		
		for (Bundle candidate : context.getBundles()) {
			if (candidate.getSymbolicName().equals(RESOURCE_BUNDLE_SYMBOLIC_NAME)) {
				resourceBundle = candidate;
				break;
			}
		}
		
		if (resourceBundle == null) 
			throw new CMMConfigException("The " + RESOURCE_BUNDLE_SYMBOLIC_NAME + " bundle could not be found");
		
		// Additionally, get the TimeService service registered by the cmm-resource bundle
		ServiceReference<TimeService> timeServiceRef = context.getServiceReference(TimeService.class);
		if (timeServiceRef == null) {
			CMMAgent.setTimeService(new SystemTimeService());
		}
		else {
			CMMAgent.setTimeService(context.getService(timeServiceRef));
		}
		
		AgentConfigLoader configLoader = new AgentConfigLoader(new BundleResourceManager(resourceBundle));
		OntModel cmmConfigModel = configLoader.loadAppConfiguration();
		
		// validate the application specification in the CMM configuration model
		validateApplicationConfiguration(cmmConfigModel);
		
		// retrieve the ApplicationSpecification and extract the needed parameters
		ApplicationSpecification appSpec = ApplicationSpecification.fromConfigurationModel(cmmConfigModel);
		CMMAgentContainer containerSpec = appSpec.getAppAgentContainer();
		
		// ======== STEP 2: start JADE framework ========
		startJADEFramework(containerSpec, context);
		
		// ======== STEP 3a: read the configuration for the OrgMgr agent (if it exists) that will 
		// create all the other agents specified in the configuration for this CMM instance ========
		boolean managerStarted = startOrgMgr(cmmConfigModel, context);
		if (!managerStarted) {
			// STEP 3b: no local OrgMgr is defined so we have to instantiate the configured agents here
			doAgentInstatiation(cmmConfigModel, context);
		}
    }


	@Override
    public void stop(BundleContext context) throws Exception {
	    agentFactory.clean();
    }
	
	
	private void validateApplicationConfiguration(OntModel cmmConfigModel) {
	    // TODO do the actual validation of the ApplicationSpecification part
    }
	
	
	private void startJADEFramework(CMMAgentContainer containerSpec, BundleContext context) throws BundleException {
		// ======== STEP 1: setup JADE Framework properties ========
		if (!containerSpec.isMainContainer()) {
			// if we're not a main container
			System.setProperty("jade.main", "false");
			
			// there must be a Main Container specification, so access that one
			CMMAgentContainer mainContainerSpec = containerSpec.getMainContainer();
			
			// set the main container host and port parameters
			System.setProperty("jade.host", mainContainerSpec.getContainerHost());
			System.setProperty("jade.port", "" + mainContainerSpec.getContainerPort());
		}
		
		// set the local container parameters
		System.setProperty("jade.local-host", containerSpec.getContainerHost());
		System.setProperty("jade.local-port", "" + containerSpec.getContainerPort());
		System.setProperty("jade.platform-id", "" + containerSpec.getPlatformName());
		
		// ======== STEP 2: start the JADE OSGi bundle ========
		Bundle jadeBundle = null;
		
		for (Bundle candidate : context.getBundles()) {
			if (candidate.getSymbolicName().equals(JADE_BUNDLE_SYMBOLIC_NAME)) {
				jadeBundle = candidate;
				break;
			}
		}
		
		jadeBundle.start();
    }

	
	private boolean startOrgMgr(OntModel cmmConfigModel, BundleContext context) throws Exception {
	    // Get the OrgMgr specification
		ManagerSpecification orgMgrSpec = ManagerSpecification.fromConfigurationModel(cmmConfigModel);
	    
		// If there is no OrgMgr, it means we are only deploying agents that are configured to 
		// connect to a remote OrgMgr. We return false, such that the AgentActivator can take over to
		// instantiate all configured agents
		if (orgMgrSpec == null)
			return false;
		
	    // get the Jade Runtime Service
		String jrsName = JadeRuntimeService.class.getName(); 
		ServiceReference jadeRef = context.getServiceReference(jrsName); 
		JadeRuntimeService jrs = (JadeRuntimeService) context.getService(jadeRef);
		
		String orgMgrName = orgMgrSpec.getAgentAddress().getLocalName();
		AgentController orgMgrController = jrs.createNewAgent(orgMgrName, OrgMgr.class.getName(), null, AGENT_BUNDLE_SYMBOLIC_NAME);
		orgMgrController.start();
		
		return true;
    }
	
	
	private void doAgentInstatiation(OntModel cmmConfigModel, BundleContext context) {
	    // TODO: Implement this as copy of code in OrgMgr for now
    }
}
