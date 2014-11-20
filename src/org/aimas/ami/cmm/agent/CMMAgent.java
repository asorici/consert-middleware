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

import java.util.Calendar;

import org.aimas.ami.cmm.agent.config.AgentSpecification;
import org.aimas.ami.cmm.agent.onto.CMMAgentLangOntology;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.utils.AgentConfigLoader;
import org.aimas.ami.cmm.utils.JadeOSGIBridgeHelper;
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
	protected JadeOSGIBridgeHelper osgiHelper;
	protected Bundle cmmInstanceBundle;
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
	
	/* The local and (optionally) assigned OrgMgr instance */
	protected AID localOrgMgr;
	protected AID assignedOrgMgr;
	protected String appIdentifier;
	
	public JadeOSGIBridgeHelper getOSGiBridge() {
		return osgiHelper;
	}
	
	public AgentConfigLoader getConfigurationLoader() {
		return configurationLoader;
	}
	
	public AgentSpecification getAgentSpecification() {
		return agentSpecification;
	}
	
	public String getAppIdentifier() {
		return appIdentifier;
	}
	
	@Override
	public void setup() {
		// STEP 1:	retrieve the initialization arguments and set CMM agent language
    	Object[] initArgs = getArguments();
    	
    	String cmmInstanceBundleLocation = (String)initArgs[0];
    	String agentSpecURI = (String)initArgs[1];
    	appIdentifier = (String)initArgs[2];
    	localOrgMgr = (AID)initArgs[3];
    	
    	// STEP 2: configure cmm resource access
    	try {
	        doResourceAccessConfiguration(cmmInstanceBundleLocation);
        }
        catch (CMMConfigException e) {
        	// signal our initialization failure
    		e.printStackTrace();
        	signalInitializationOutcome(false);
        	return;
        }
    	
    	// STEP 3: register the CMMAgent-Lang ontology
    	registerCMMAgentLang();
    	
    	// STEP 4: call the agent specific setup
    	doAgentSpecificSetup(agentSpecURI, appIdentifier);
    	
    	// STEP 5: after the agent specific setup all agents must register with their assigned OrgMgr, 
    	// if they have one
    	registerWithAssignedManager();
	}
	
	/* Register CMMAgent-Lang Ontology */
	private void registerCMMAgentLang() {
		getContentManager().registerLanguage(cmmCodec);
		getContentManager().registerOntology(cmmOntology);
	}
	
	
	/* Get the OSGi helper, find the CMM resource bundle and build the agent's configuration loader. */
	private void doResourceAccessConfiguration(String cmmInstanceBundleLocation) throws CMMConfigException {
		try {
			osgiHelper = (JadeOSGIBridgeHelper) getHelper(JadeOSGIBridgeHelper.SERVICE_NAME);
		}
		catch (ServiceException e) {
			throw new CMMConfigException("Could not find OSGIBridgeHelper JADE kernel service", e);
		}
		
		BundleContext context = osgiHelper.getBundleContext();
		cmmInstanceBundle = context.getBundle(cmmInstanceBundleLocation);
		
		// Now that we have found our resource bundle, setup the configuration
		// loader and load the CMM configuration for located at the standard location:
		// etc/cmm/agent-config.ttl
		configurationLoader = new AgentConfigLoader(new BundleResourceManager(cmmInstanceBundle));
		
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
		//System.out.println("[" + getName() + "]:" + "Sending INIT SUCCESS message to OrgMgr");
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
    		sd.setType(getAgentType().getServiceName());
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
	
	protected abstract void doAgentSpecificSetup(String agentSpecURI, String appIdentifier);
	
	protected abstract void registerWithAssignedManager();
}
