package org.aimas.ami.cmm;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.mtp.http.MessageTransportProtocol;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aimas.ami.cmm.agent.AgentManagementService;
import org.aimas.ami.cmm.agent.config.ApplicationSpecification;
import org.aimas.ami.cmm.agent.config.CMMAgentContainer;
import org.aimas.ami.cmm.agent.config.PlatformSpecification;
import org.aimas.ami.cmm.agent.osgi.JadeOSGIBridgeService;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.api.CMMInstanceStateOpCallback;
import org.aimas.ami.cmm.api.CMMOperationFuture;
import org.aimas.ami.cmm.api.CMMPlatformManagementService;
import org.aimas.ami.cmm.utils.AgentConfigLoader;
import org.aimas.ami.cmm.utils.PlatformConfigLoader;
import org.aimas.ami.contextrep.utils.BundleResourceManager;
import org.apache.felix.dm.DependencyManager;
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
	
	private static CMMPlatformManager instance;
	
	public static CMMPlatformManager getInstance() {
		return instance;
	}
	
	private BundleContext cmmBundleContext;
	private DependencyManager cmmDependencyManager;
	
	private CMMInstanceTracker cmmInstanceBundleTracker;
	private BundleResourceManager defaultInstanceResourceManager;
	
	private AgentContainer jadeAgentContainer;
	private PlatformSpecification platformSpecification;
	
	private Map<ContextDomainInfoWrapper, CMMInstanceStateWrapper> cmmInstanceStateMap = new HashMap<ContextDomainInfoWrapper, CMMInstanceStateWrapper>();
	private CMMInstanceStateOpHandler cmmInstanceStateOpHandler; 
	
	// Platform Start / Stop Management
	/////////////////////////////////////////////////////////////////////////
	@Override
    public void start(BundleContext context) throws Exception {
		// Save the OSGi related information
		instance = this;
		
		cmmBundleContext = context;
		cmmDependencyManager = new DependencyManager(context);
		
		cmmInstanceBundleTracker = new CMMInstanceTracker(context);
		cmmInstanceBundleTracker.open();
		
		// Register this class as a CMMPlatformManagementService instance
		context.registerService(CMMPlatformManagementService.class, this, null);
    }
	
	
	@Override
    public void stop(BundleContext context) throws Exception {
		// stop the CMM Platform
		stopCMMPlatform();
		
		// close the CMM Instance Bundle Tracker
		cmmInstanceBundleTracker.close();
		
		// lastly null out the OSGi context
		cmmDependencyManager.clear();
		cmmDependencyManager = null;
		cmmBundleContext = null;
    }
	
	@Override
	public void startCMMPlatform() throws Exception {
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
    	
    	// STEP 4: create and start the CMM Instance State Operations Handler
    	cmmInstanceStateOpHandler = new CMMInstanceStateOpHandler();
    	cmmInstanceStateOpHandler.start();
    	
    	// STEP 4: now there should normally be a default application configuration (agent-config file), so we start looking for one
    	// However, if we don't find one, there's no error, since the application still has the means to "call" for the instantiation of
    	// such an instance itself.
    	try {
    		AgentConfigLoader agentConfigLoader = new AgentConfigLoader(defaultInstanceResourceManager);
    		OntModel agentConfigModel = agentConfigLoader.loadAgentConfiguration();
    		
    		ApplicationSpecification defaultAppSpecification = ApplicationSpecification.fromConfigurationModel(agentConfigModel); 
    		String defaultApplicationId = defaultAppSpecification.getAppIdentifier();
    		String defaultContextDimension = defaultAppSpecification.getLocalContextDomain().hasDomainDimension() ? 
    				defaultAppSpecification.getLocalContextDomain().getDomainDimension().getURI() : null;
    		String defaultContextDomain = defaultAppSpecification.getLocalContextDomain().hasDomainValue() ? 
    				defaultAppSpecification.getLocalContextDomain().getDomainValue().getURI() : null;
    		
    		CMMOperationFuture<Void> installTask = installProvisioningGroup(defaultApplicationId, defaultContextDimension, defaultContextDomain);
    		installTask.awaitOperation();
    	}
    	catch(CMMConfigException ex) {
    		//ex.printStackTrace();
    		System.out.println("[" + getClass().getSimpleName() + "] There is no default CMM Agent Configuration - application must instantiate one");
    	}
	}
	
	@Override
	public void stopCMMPlatform() throws Exception {
		// TODO: stop all active Provisioning Group Instances -- see if this can be done more nicely
		for (CMMInstanceStateWrapper wrapper : cmmInstanceStateMap.values()) {
			if (wrapper.getState() != CMMInstanceState.NOT_INSTALLED) {
				CMMOperationFuture<Void> killTask = uninstallProvisioningGroup(wrapper.getApplicationId(), 
						wrapper.getContextDimensionURI(), wrapper.getContextDomainValueURI());
				
				killTask.awaitOperation(5, TimeUnit.SECONDS);
				
			}
		}
		
		// stop the Provisioning Group Instance State Operation Handler
		if (cmmInstanceStateOpHandler != null) {
			cmmInstanceStateOpHandler.close();
			cmmInstanceStateOpHandler = null;
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
		props.setProperty(Profile.GUI, Boolean.toString(true));
		
		// if it is not a main container, indicate the host:port address of the main container
		if (!isMainContainer) {
			CMMAgentContainer mainContainer = platformContainerSpec.getMainContainer();
			props.setProperty(Profile.MAIN_HOST, mainContainer.getContainerHost());
			props.setProperty(Profile.MAIN_PORT, Integer.toString(mainContainer.getContainerPort()));
		}
		
		// register a HTTP MTP with the given hostname and address configurations
		String mtpHost = platformContainerSpec.getMTPHost();
		int mtpPort = platformContainerSpec.getMTPPort();
		
		String mtpSpecifierString = "jade.mtp.http.MessageTransportProtocol(" + "http://" + mtpHost + ":" + mtpPort + "/jade" + ")";
		props.setProperty(Profile.MTPS, mtpSpecifierString);
		
		// register the JadeOsgiBridgeService JADE kernel service
		props.setProperty(Profile.SERVICES, JadeOSGIBridgeService.class.getName());
		
		profile = new ProfileImpl(props);
		Runtime.instance().setCloseVM(false);
		if(isMainContainer) {
			jadeAgentContainer = Runtime.instance().createMainContainer(profile);
		}
		else {
			jadeAgentContainer = Runtime.instance().createAgentContainer(profile);
		}
	}
	
	// OSGi Context
	/////////////////////////////////////////////////////////////////////////
	public BundleContext getBundleContext() {
		return cmmBundleContext;
	}
	
	public DependencyManager getDependencyManager() {
		return cmmDependencyManager;
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
	public CMMInstanceState getProvisioningGroupState(String applicationId, String contextDimensionURI, String contextDomainValueURI) {
		CMMInstanceStateWrapper stateWrapper = cmmInstanceStateMap.get(new ContextDomainInfoWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId)); 
		
		if (stateWrapper != null) {
			return stateWrapper.getState();
		}
		
		return CMMInstanceState.NOT_INSTALLED;
	}
	
	public CMMInstanceState getCMMInstanceState(ContextDomainInfoWrapper contextDomainInfo) {
		CMMInstanceStateWrapper stateWrapper = cmmInstanceStateMap.get(contextDomainInfo); 
		
		if (stateWrapper != null) {
			return stateWrapper.getState();
		}
		
		return CMMInstanceState.NOT_INSTALLED;
	}
	
	
	public void setCMMInstanceState(ContextDomainInfoWrapper contextDomainInfo, CMMInstanceState state) {
		CMMInstanceStateWrapper stateWrapper = cmmInstanceStateMap.get(contextDomainInfo);
		if (stateWrapper != null) {
			stateWrapper.setInstanceState(state);
		}
	}
	
	
	public CMMPlatformRequestExecutor getRequestExecutor(ContextDomainInfoWrapper contextDomainInfo) {
		CMMInstanceStateWrapper stateWrapper = cmmInstanceStateMap.get(contextDomainInfo);
		if (stateWrapper != null) {
			return stateWrapper.getPlatformRequestExecutor();
		}
		
		return null;
	}
	
	
	public void setRequestExecutor(ContextDomainInfoWrapper contextDomainInfo, CMMPlatformRequestExecutor executor) {
		CMMInstanceStateWrapper stateWrapper = cmmInstanceStateMap.get(contextDomainInfo);
		if (stateWrapper != null) {
			stateWrapper.setPlatformRequestExecutor(executor);
		}
	}
	
	@Override
    public void installProvisioningGroup(String applicationId, String contextDimensionURI, String contextDomainValueURI, 
    		CMMInstanceStateOpCallback operationCallback) {
		// Create domain info wrapper
		ContextDomainInfoWrapper contextDomainInfo = new ContextDomainInfoWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId);
		
		// Create entry in cmmInstanceStateMap if none exists
		CMMInstanceStateWrapper wrapper = cmmInstanceStateMap.get(contextDomainInfo);
		if (wrapper == null) {
			wrapper = new CMMInstanceStateWrapper(applicationId, contextDimensionURI, contextDomainValueURI);
			cmmInstanceStateMap.put(contextDomainInfo, wrapper);
		}
		
		cmmInstanceStateOpHandler.execOperation(new CMMInstallInstanceOp(contextDomainInfo, this), operationCallback);
    }
	
	@Override
    public void startProvisioningGroup(String applicationId, String contextDimensionURI, String contextDomainValueURI,
    		CMMInstanceStateOpCallback operationCallback) {
		ContextDomainInfoWrapper contextDomainInfo = new ContextDomainInfoWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId);
		
		cmmInstanceStateOpHandler.execOperation(new CMMStartInstanceOp(contextDomainInfo, this), operationCallback);
    }
	
	@Override
    public void stopProvisioningGroup(String applicationId, String contextDimensionURI, String contextDomainValueURI,
    		CMMInstanceStateOpCallback operationCallback) {
		ContextDomainInfoWrapper contextDomainInfo = new ContextDomainInfoWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId);
		
		cmmInstanceStateOpHandler.execOperation(new CMMStopInstanceOp(contextDomainInfo, this), operationCallback);
    }
	
	@Override
    public void uninstallProvisioningGroup(String applicationId, String contextDimensionURI, String contextDomainValueURI,
    		CMMInstanceStateOpCallback operationCallback) {
		ContextDomainInfoWrapper contextDomainInfo = new ContextDomainInfoWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId);
		
		cmmInstanceStateOpHandler.execOperation(new CMMKillInstanceOp(contextDomainInfo, this), operationCallback);
    }
	
	
	@Override
    public CMMOperationFuture<Void> installProvisioningGroup(String applicationId, 
    		String contextDimensionURI, String contextDomainValueURI) {
		ContextDomainInfoWrapper contextDomainInfo = new ContextDomainInfoWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId);
		
		// Create entry in cmmInstanceStateMap if none exists
		CMMInstanceStateWrapper wrapper = cmmInstanceStateMap.get(contextDomainInfo);
		if (wrapper == null) {
			wrapper = new CMMInstanceStateWrapper(applicationId, contextDimensionURI, contextDomainValueURI);
			cmmInstanceStateMap.put(contextDomainInfo, wrapper);
		}
		
		return cmmInstanceStateOpHandler.execOperation(new CMMInstallInstanceOp(contextDomainInfo, this));
    }
	
	@Override
    public CMMOperationFuture<Void> startProvisioningGroup(String applicationId, 
    		String contextDimensionURI, String contextDomainValueURI) {
		ContextDomainInfoWrapper contextDomainInfo = new ContextDomainInfoWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId);
		
		return cmmInstanceStateOpHandler.execOperation(new CMMStartInstanceOp(contextDomainInfo, this));
    }

	@Override
    public CMMOperationFuture<Void> stopProvisioningGroup(String applicationId, 
            String contextDimensionURI, String contextDomainValueURI) {
		ContextDomainInfoWrapper contextDomainInfo = new ContextDomainInfoWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId);
		
		return cmmInstanceStateOpHandler.execOperation(new CMMStopInstanceOp(contextDomainInfo, this));
    }

	@Override
    public CMMOperationFuture<Void> uninstallProvisioningGroup(String applicationId, 
    		String contextDimensionURI, String contextDomainValueURI) {
		ContextDomainInfoWrapper contextDomainInfo = new ContextDomainInfoWrapper(contextDimensionURI, 
				contextDomainValueURI, applicationId);
		
		return cmmInstanceStateOpHandler.execOperation(new CMMKillInstanceOp(contextDomainInfo, this));
    }
	
	
	@Override
    public boolean addPlatformMtpAddress(String host, int port) {
	    String mtpAddress = "http://" + host + ":" + port + "/jade";
	    
		try {
	        jadeAgentContainer.installMTP(mtpAddress, MessageTransportProtocol.class.getName());
        }
        catch (Exception e) {
	        e.printStackTrace();
	        return false;
        }
        
    
		return true;
	}
	
	
	@Override
    public void removePlatformMtpAddress(String host, int port) {
		String mtpAddress = "http://" + host + ":" + port + "/jade";
	    
		try {
	        jadeAgentContainer.uninstallMTP(mtpAddress);
        }
        catch (Exception e) {
	        e.printStackTrace();
        }
    }

	// CMM Agent Management Service
	/////////////////////////////////////////////////////////////////////////
	@Override
    public AgentController createNewAgent(String localName, String className, Object[] args) throws Exception {
		return jadeAgentContainer.createNewAgent(localName, className, args);
    }


	@Override
    public void removeAgent(AID agentAID) {
	    try {
	        jadeAgentContainer.getAgent(agentAID.getName(), true).kill();
        }
        catch (ControllerException e) {
	        e.printStackTrace();
        }
    }


	@Override
    public AgentController acceptNewAgent(String localName, Agent agent) throws Exception {
		return jadeAgentContainer.acceptNewAgent(localName, agent);
    }


	@Override
    public String getPlatformName() {
	    return jadeAgentContainer.getPlatformName();
    }
}
