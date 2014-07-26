package org.aimas.ami.cmm.agent;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.osgi.OSGIBridgeHelper;

import org.aimas.ami.cmm.agent.config.AgentSpecification;
import org.aimas.ami.cmm.agent.onto.CMMAgentLangOntology;
import org.aimas.ami.cmm.exceptions.CMMConfigException;
import org.aimas.ami.cmm.utils.AgentConfigLoader;
import org.aimas.ami.contextrep.utils.BundleResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public abstract class CMMAgent extends Agent {
	private static final long serialVersionUID = 6980230538089503193L;
	
	public static final String RESOURCE_BUNDLE_NAME = "cmm-resources";
	
	
	/* Administrative helper stuff */
	protected OSGIBridgeHelper helper;
	protected Bundle resourceBundle;
	protected AgentConfigLoader configurationLoader;	
	
	/* Agent Specification */
	protected AgentSpecification agentSpecification;
	
	/* The codec and ontology settings for a CMM agent */
    protected Codec cmmCodec = new SLCodec();
    protected Ontology cmmOntology = CMMAgentLangOntology.getInstance();
	
	/* The (optional) local OrgMgr instance */
	protected AID localOrgMgr;
	
	public OSGIBridgeHelper getOSGiBridge() {
		return helper;
	}
	
	public AgentConfigLoader getConfigurationLoader() {
		return configurationLoader;
	}
	
	public AgentSpecification getAgentSpecification() {
		return agentSpecification;
	}
	
	public Codec getCMMCodec() {
		return cmmCodec;
	}
	
	public Ontology getCMMOntology() {
		return cmmOntology;
	}
	
	/**
	 * Get the OSGi helper, find the CMM resource bundle and build the agent's
	 * configuration loader.
	 * 
	 * @throws CMMConfigException
	 */
	protected void doResourceAccessConfiguration() throws CMMConfigException {
		try {
			helper = (OSGIBridgeHelper) getHelper(OSGIBridgeHelper.SERVICE_NAME);
		}
		catch (ServiceException e) {
			throw new CMMConfigException(
			        "Failed to configure CMM. Could not access OSGi bridge helper.",
			        e);
		}
		
		BundleContext context = helper.getBundleContext();
		
		// Iterate through the installed bundles to find our "cmm-resources"
		for (Bundle candidate : context.getBundles()) {
			if (candidate.getSymbolicName().equals(RESOURCE_BUNDLE_NAME)) {
				resourceBundle = candidate;
				break;
			}
		}
		
		// Now that we have found our resource bundle, setup the configuration
		// loader
		// and load the CMM configuration for located at the standard location:
		// etc/cmm/cmm-config.ttl
		configurationLoader = new AgentConfigLoader(new BundleResourceManager(
		        resourceBundle));
	}
	
	/**
	 * Register the specific type (sensor, user, queryHandler, CtxCoord) of this
	 * CMMAgent with the local OrgMgr (acting as DF) within the range of the
	 * given <code>appIdentifier</code>.
	 */
	protected void registerAgentService(String appIdentifier) {
    	if (localOrgMgr != null) {
    		DFAgentDescription dfd = new DFAgentDescription();
    		dfd.setName(getAID());
    		
    		ServiceDescription sd = new ServiceDescription();
    		sd.setType(getAgentType().getServiceType());
    		sd.setName(appIdentifier);	// the name of the service is the appIdentifier string
    		sd.addLanguages(cmmCodec.getName());
    		sd.addOntologies(cmmOntology.getName());
    		
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
