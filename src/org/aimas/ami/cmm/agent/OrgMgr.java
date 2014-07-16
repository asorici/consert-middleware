package org.aimas.ami.cmm.agent;

import jade.core.AID;
import jade.core.ServiceException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.df;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.osgi.OSGIBridgeHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.aimas.ami.cmm.exceptions.CMMConfigException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class OrgMgr extends df {
    private static final long serialVersionUID = 8067387356505727111L;
    
    public static final String NICKNAME = "OrgMgr";
    public static final String RESOURCE_BUNDLE_NAME = "cmm-resources";
    
    /**
     * The helper that allows access to the OSGi framework that supports this CMM deployment.
     */
    private OSGIBridgeHelper helper;
    private Bundle resourceBundle;
    
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
    	}
    	catch(CMMConfigException e) {
    		System.out.println("Failed to initialize OrgMgr agent. Reason: " + e);
    	}
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
			if (candidate.getSymbolicName().equals(RESOURCE_BUNDLE_NAME)) {
				resourceBundle = candidate;
				break;
			}
		}
		
		// Now that we have found our resource bundle, access the cmm-config.rdf file which we know
		// has this standard name and is placed under the standard location etc/cmm/cmm-config.rdf.
		try {
			URL cmmConfigURL = resourceBundle.getEntry("etc/cmm/cmm-config.rdf");
	        InputStream cmmConfigStream = cmmConfigURL.openStream();
	        
	        
	        Model cmmConfigModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        }
        catch (IOException e) {
	        throw new CMMConfigException("Failed to read OrgMgr cmm-config.rdf file.", e);
        }
    }
	
	
	@Override
	protected void takeDown() {
	}
}
