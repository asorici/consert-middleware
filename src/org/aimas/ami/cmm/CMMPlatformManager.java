package org.aimas.ami.cmm;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.osgi.OSGIBridgeHelper;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.HashMap;
import java.util.Map;

import org.aimas.ami.cmm.agent.AgentManagementService;
import org.aimas.ami.cmm.agent.config.CMMAgentContainer;
import org.aimas.ami.cmm.agent.config.PlatformSpecification;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.api.CMMInstanceStateOpCallback;
import org.aimas.ami.cmm.api.CMMOperationFuture;
import org.aimas.ami.cmm.api.CMMPlatformManagementService;
import org.aimas.ami.cmm.utils.AgentConfigLoader;
import org.aimas.ami.cmm.utils.PlatformConfigLoader;
import org.aimas.ami.contextrep.utils.BundleResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.hp.hpl.jena.ontology.OntModel;

/*
 * The class plays the role of both bundle activator (starting the JADE platform on which
 * different CMM instances will be started) as well as the implementation of the DeployManagementService
 */
public class CMMPlatformManager implements BundleActivator, CMMPlatformManagementService, AgentManagementService {
	public static final String 	DEFAULT_JADE_MTP_HOST 	= 	"localhost";
	public static final int		DEFAULT_JADE_MTP_PORT 	= 	7778;
	
	private CMMInstanceTracker cmmInstanceBundleTracker;
	private BundleResourceManager defaultInstanceResourceManager;
	
	private AgentContainer jadeAgentContainer;
	private PlatformSpecification platformSpecification;
	
	private Map<ContextDomainWrapper, CMMInstanceStateWrapper> cmmInstanceStateMap = new HashMap<ContextDomainWrapper, CMMInstanceStateWrapper>();
	
	// Platform Start / Stop Management
	/////////////////////////////////////////////////////////////////////////
	@Override
    public void start(BundleContext context) throws Exception {
		cmmInstanceBundleTracker = new CMMInstanceTracker(context);
		cmmInstanceBundleTracker.open();
		
		// STEP 1: retrieve the default-instance bundle - this must exist as per design requirement
		Bundle defaultInstanceBundle = cmmInstanceBundleTracker.getDefaultInstanceBundle();
		if (defaultInstanceBundle == null) {
			throw new CMMConfigException("The default CONSERT middleware instance bundle was not found.");
		}
		
		// STEP 2: load the platform configuration
		defaultInstanceResourceManager = new BundleResourceManager(defaultInstanceBundle); 
		PlatformConfigLoader platformConfigLoader = new PlatformConfigLoader(defaultInstanceResourceManager);
    	OntModel platformConfigModel = platformConfigLoader.loadAppConfiguration();
    	
    	// validate the platform configuration model
    	validatePlatformSpecification(platformConfigModel);
    	
    	// STEP 3: process the platform configuration and create the JADE (Main) Container instance
    	platformSpecification = PlatformSpecification.fromConfigurationModel(platformConfigModel);
    	CMMAgentContainer platformContainerSpec = platformSpecification.getPlatformAgentContainer();
    	createJadePlatform(platformContainerSpec);
    	
    	// STEP 4: now there should normally be a default CMM instance configuration (cmm-config file), so we start looking for one
    	// However, if we don't find one, there's no error, since the application still has the means to "call" for the instantiation of
    	// such an instance itself.
    	try {
    		AgentConfigLoader agentConfigLoader = new AgentConfigLoader(defaultInstanceResourceManager);
    		OntModel agentConfigModel = agentConfigLoader.loadAgentConfiguration();
    		
    		
    	}
    	catch(CMMConfigException ex) {
    		System.out.println("There is no default CMM Agent Configuration - application must instantiate one");
    	}
    	
    }
	

	private void validatePlatformSpecification(OntModel platformConfigModel) throws CMMConfigException {
	    
    }
	
	
	private void createJadePlatform(CMMAgentContainer platformContainerSpec) {
	    // STEP 1: setup the platform properties
		ProfileImpl profile = null;
		Properties props = new ExtendedProperties();
		
		boolean isMainContainer = platformContainerSpec.isMainContainer();
		
		// set properties for the platform and local container
		props.setProperty(Profile.PLATFORM_ID, platformContainerSpec.getPlatformName());
		props.setProperty(Profile.MAIN, Boolean.toString(isMainContainer));
		props.setProperty(Profile.LOCAL_HOST, platformContainerSpec.getContainerHost());
		props.setProperty(Profile.LOCAL_PORT, Integer.toString(platformContainerSpec.getContainerPort()));
		
		// if it is not a main container, indicate the host:port address of the main container
		if (isMainContainer) {
			CMMAgentContainer mainContainer = platformContainerSpec.getMainContainer();
			props.setProperty(Profile.MAIN_HOST, mainContainer.getContainerHost());
			props.setProperty(Profile.MAIN_PORT, Integer.toString(mainContainer.getContainerPort()));
		}
		
		// register a HTTP MTP with the given hostname and address configurations
		String mtpHost = platformContainerSpec.getMTPHost();
		int mtpPort = platformContainerSpec.getMTPPort();
		
		String mtpSpecifierString = "jade.mtp.http.MessageTransportProtocol(" + "http://" + mtpHost + ":" + mtpPort + "/jade" + ")";
		props.setProperty(Profile.MTPS, mtpSpecifierString);
		
		// register the OsgiBridgeHelper JADE kernel service
		props.setProperty(Profile.SERVICES, OSGIBridgeHelper.class.getName());
		
		profile = new ProfileImpl(props);
		Runtime.instance().setCloseVM(false);
		if(isMainContainer) {
			jadeAgentContainer = Runtime.instance().createMainContainer(profile);
		}
		else {
			jadeAgentContainer = Runtime.instance().createAgentContainer(profile);
		}
	}


	@Override
    public void stop(BundleContext context) throws Exception {
		cmmInstanceBundleTracker.close();
    }
	
	// Application Info
	/////////////////////////////////////////////////////////////////////////
	public PlatformSpecification getPlatformSpecification() {
		return platformSpecification;
	}
	
	// CMM Instance Bundle Management
	/////////////////////////////////////////////////////////////////////////
	public Bundle getCMMInstanceResourceBundle(String applicationId, String contextDimensionURI, String contextDomainValueURI) {
		return cmmInstanceBundleTracker.getCMMInstanceBundle(applicationId, contextDimensionURI, contextDomainValueURI);
	}
	
	// CMM Instance State Management
	/////////////////////////////////////////////////////////////////////////
	public CMMInstanceState getCMMInstanceState(String applicationId, String contextDimensionURI, String contextDomainValueURI) {
		CMMInstanceStateWrapper stateWrapper = cmmInstanceStateMap.get(new ContextDomainWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId)); 
		
		if (stateWrapper != null) {
			return stateWrapper.getState();
		}
		
		return CMMInstanceState.NOT_INSTALLED;
	}
	
	public CMMInstanceState getCMMInstanceState(ContextDomainWrapper contextDomainInfo) {
		CMMInstanceStateWrapper stateWrapper = cmmInstanceStateMap.get(contextDomainInfo); 
		
		if (stateWrapper != null) {
			return stateWrapper.getState();
		}
		
		return CMMInstanceState.NOT_INSTALLED;
	}
	
	@Override
    public void installCMMInstance(String applicationId, String contextDimensionURI, String contextDomainValueURI, 
    		CMMInstanceStateOpCallback operationCallback) {
	    // TODO Auto-generated method stub
	    
    }
	
	@Override
    public void startCMMInstance(String applicationId, String contextDimensionURI, String contextDomainValueURI,
    		CMMInstanceStateOpCallback operationCallback) {
	    // TODO Auto-generated method stub
	    
    }
	
	@Override
    public void stopCMMInstance(String applicationId, String contextDimensionURI, String contextDomainValueURI,
    		CMMInstanceStateOpCallback operationCallback) {
	    // TODO Auto-generated method stub
	    
    }
	
	@Override
    public void uninstallCMMInstance(String applicationId, String contextDimensionURI, String contextDomainValueURI,
    		CMMInstanceStateOpCallback operationCallback) {
	    // TODO Auto-generated method stub
	    
    }
	
	
	@Override
    public CMMOperationFuture<Void> installCMMInstance(String applicationId, 
    		String contextDimensionURI, String contextDomainValueURI) {
	    return null;
    }
	
	@Override
    public CMMOperationFuture<Void> startCMMInstance(String applicationId, 
    		String contextDimensionURI, String contextDomainValueURI) {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public CMMOperationFuture<Void> stopCMMInstance(String applicationId, 
            String contextDimensionURI, String contextDomainValueURI) {
	    return null;
    }

	@Override
    public CMMOperationFuture<Void> uninstallCMMInstance(String applicationId, 
    		String contextDimensionURI, String contextDomainValueURI) {
	    return null;
    }
	
	@Override
    public void addPlatformMtpAddress(String host, int port) {
	    // TODO Auto-generated method stub
	    
    }
	
	
	@Override
    public void removePlatformMtpAddress(String host, int port) {
	    // TODO Auto-generated method stub
	    
    }

	// CMM Agent Management Service
	/////////////////////////////////////////////////////////////////////////
	@Override
    public AgentController createNewAgent(String localName, String className, Object[] args) throws Exception {
	    // TODO Auto-generated method stub
		return jadeAgentContainer.createNewAgent(localName, className, args);
    }


	@Override
    public void removeAgent(AID agentAID) {
	    // TODO Auto-generated method stub
	    
    }


	@Override
    public AgentController acceptNewAgent(String localName, Agent agent) throws Exception {
	    // TODO Auto-generated method stub
	    return null;
    }


	@Override
    public String getContainerName() {
	    // TODO Auto-generated method stub
	    return null;
    }


	@Override
    public String getPlatformName() {
	    // TODO Auto-generated method stub
	    return null;
    }
}
