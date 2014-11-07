package org.aimas.ami.cmm.agent;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.osgi.OSGIBridgeHelper;

import java.util.Calendar;

import org.aimas.ami.cmm.CMMPlatformManager;
import org.aimas.ami.cmm.agent.config.AgentSpecification;
import org.aimas.ami.cmm.agent.onto.CMMAgentLangOntology;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.utils.AgentConfigLoader;
import org.aimas.ami.contextrep.resources.SystemTimeService;
import org.aimas.ami.contextrep.resources.TimeService;
import org.aimas.ami.contextrep.utils.BundleResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public abstract class CMMAgent extends Agent {
	private static final long serialVersionUID = 6980230538089503193L;
	
	/* The codec and ontology settings for a CMM agent */
	public static final Codec cmmCodec = new SLCodec(true);
	public static final Ontology cmmOntology = CMMAgentLangOntology.getInstance();
	
	/* Administrative helper stuff */
	protected OSGIBridgeHelper osgiHelper;
	protected Bundle resourceBundle;
	protected AgentConfigLoader configurationLoader;
	
	private static TimeService timeService;
	
	public static void setTimeService(TimeService timeService) {
		CMMAgent.timeService = timeService;
	}
	
	public static long currentTimeMillis() {
		return timeService.getCurrentTimeMillis();
	}
	
	public static Calendar now() {
		return timeService.getCalendarInstance();
	}
	
	/* Agent Specification */
	protected AgentSpecification agentSpecification;
	
	/* The (optional) local OrgMgr instance */
	protected AID localOrgMgr;
	
	public OSGIBridgeHelper getOSGiBridge() {
		return osgiHelper;
	}
	
	public AgentConfigLoader getConfigurationLoader() {
		return configurationLoader;
	}
	
	public AgentSpecification getAgentSpecification() {
		return agentSpecification;
	}
	
	/**
	 * Register CMMAgent-Lang Ontology
	 */
	protected void registerCMMAgentLang() {
		getContentManager().registerLanguage(cmmCodec);
		getContentManager().registerOntology(cmmOntology);
	}
	
	
	/**
	 * Get the OSGi helper, find the CMM resource bundle and build the agent's
	 * configuration loader.
	 * 
	 * @throws CMMConfigException
	 */
	protected void doResourceAccessConfiguration() throws CMMConfigException {
		try {
			osgiHelper = (OSGIBridgeHelper) getHelper(OSGIBridgeHelper.SERVICE_NAME);
		}
		catch (ServiceException e) {
			throw new CMMConfigException("Failed to configure CMM. Could not access OSGi bridge helper.", e);
		}
		
		BundleContext context = osgiHelper.getBundleContext();
		
		// Iterate through the installed bundles to find our "cmm-resources"
		for (Bundle candidate : context.getBundles()) {
			if (candidate.getSymbolicName().equals(CMMPlatformManager.RESOURCE_BUNDLE_SYMBOLIC_NAME)) {
				resourceBundle = candidate;
				break;
			}
		}
		
		// Now that we have found our resource bundle, setup the configuration
		// loader
		// and load the CMM configuration for located at the standard location:
		// etc/cmm/cmm-config.ttl
		configurationLoader = new AgentConfigLoader(new BundleResourceManager(resourceBundle));
		
		// Lastly, we search for the TimeService that provides the real (or simulated) time, if it was
		// not already loaded (we store as a class variable, so every agent in a bundle will have the same
		// instance of the timeService).
		if (timeService == null) {
			ServiceReference<TimeService> timeServiceRef = context.getServiceReference(TimeService.class);
			if (timeServiceRef == null) {
				timeService = new SystemTimeService();
			}
			else {
				timeService = context.getService(timeServiceRef);
			}
		}
	}
	
	
	protected void signalInitializationOutcome(boolean success) {
		System.out.println("[" + getName() + "]:" + "Sending INIT SUCCESS message to OrgMgr");
		final boolean initSuccessful = success;
		if (localOrgMgr != null) {
			addBehaviour(new OneShotBehaviour(this) {
                private static final long serialVersionUID = 1L;

				@Override
				public void action() {
					ACLMessage initCompleteMsg = new ACLMessage(ACLMessage.INFORM);
					initCompleteMsg.addReceiver(localOrgMgr);
					initCompleteMsg.setContent("initConfirm:" + initSuccessful);
					
					myAgent.send(initCompleteMsg);
				}
			});
		}
    }
	
	
	/**
	 * Register the specific type (sensor, user, queryHandler, CtxCoord) of this
	 * CMMAgent with the local OrgMgr (acting as DF) within the range of the
	 * given <code>appIdentifier</code>.
	 * @param appIdentifier	The unique application identifier for which this agent is registering its service 
	 * @param serviceProperties A CMMAgent service specific list of properties (e.g. isPrimary for CtxQueryHandler). 
	 *  Man be null if no specific properties are required.
	 */
	protected void registerAgentService(String appIdentifier, Property[] serviceProperties) {
    	if (localOrgMgr != null) {
    		DFAgentDescription dfd = new DFAgentDescription();
    		dfd.setName(getAID());
    		
    		ServiceDescription sd = new ServiceDescription();
    		sd.setType(getAgentType().getServiceType());
    		sd.setName(appIdentifier);	// the name of the service is the appIdentifier string
    		sd.addLanguages(cmmCodec.getName());
    		sd.addOntologies(cmmOntology.getName());
    		
    		if (serviceProperties != null) {
    			for (Property p : serviceProperties) {
    				sd.addProperties(p);
    			}
    		}
    		
    		dfd.addServices(sd);
    		
    		try {
    			DFService.register(this, localOrgMgr, dfd);
    		}
    		catch (FIPAException fe) {
    			fe.printStackTrace();
    		}
    	}
    }
	
	@Override
	protected void takeDown() {
		deregisterAgentServices();
	}
	
	
	private void deregisterAgentServices() {
		if (localOrgMgr != null) {
			try {
				DFService.deregister(this, localOrgMgr);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
    }

	
	// public abstract AgentSpecification getAgentSpecification();
	public abstract AgentType getAgentType();
}
