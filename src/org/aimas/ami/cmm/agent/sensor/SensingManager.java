package org.aimas.ami.cmm.agent.sensor;

import jade.core.AID;

import java.util.HashMap;
import java.util.Map;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.osgi.framework.BundleContext;

public class SensingManager {
	public static interface AssertionActiveListener {
		public void assertionActiveChanged(String assertionResourceURI, boolean active);
	}
	
	protected CMMAgent managingAgent;
	protected AssertionActiveListener assertionActiveListener;
	
	protected Map<String, AssertionManager> managedAssertions = new HashMap<String, AssertionManager>();
	protected AID assertionUpdatesDestination;
	
	public SensingManager(CMMAgent managingAgent, AssertionActiveListener assertionActiveListener) {
		this.managingAgent = managingAgent;
		this.assertionActiveListener = assertionActiveListener;
	}
	
	public CMMAgent getManagingAgent() {
		return managingAgent;
	}
	
	public Map<String, AssertionManager> getManagedAssertions() {
		return managedAssertions;
	}

	public AssertionManager getAssertionManager(String assertionResourceURI) {
		return managedAssertions.get(assertionResourceURI);
	}
	
	public AID getAssertionUpdatesDestination() {
		return assertionUpdatesDestination;
	}

	public void setAssertionUpdateDestination(AID assertionUpdatesDestination) {
		this.assertionUpdatesDestination = assertionUpdatesDestination;
	}
	
	public boolean hasUpdateDestination() {
	    return assertionUpdatesDestination != null;
    }
	
	public void addManagedContextAssertion(String assertionResourceURI, String adaptorClassName, 
			String updateMode, int updateRate) {
		
    	// Access the assertion adaptor service instance via ServiceTracker
    	BundleContext context = managingAgent.getOSGiBridge().getBundleContext();
    	AssertionAdaptorTracker adaptorTracker = new AssertionAdaptorTracker(context, adaptorClassName, 
    			assertionResourceURI, managingAgent.getLocalName());
    	
    	// When we create the assertion manager updates are not yet enabled.
    	// Enabling will be done by the CtxSensor agent, once it gets the OK from the CtxCoord
    	// for the ContextAssertions it wants to publish.
    	AssertionManager assertionManager = new AssertionManager(assertionResourceURI, updateMode, 
    			updateRate, adaptorTracker, this);
    	
    	managedAssertions.put(assertionResourceURI, assertionManager);
	}
	
	
	public void removeManagedContextAssertion(String assertionResourceURI) {
		AssertionManager assertionManager = managedAssertions.remove(assertionResourceURI);
		assertionManager.close();
	}
	
	
	public void notifyAssertionActive(String assertionResourceURI, boolean active) {
		if (assertionActiveListener != null) {
			assertionActiveListener.assertionActiveChanged(assertionResourceURI, active);
		}
	}
}
