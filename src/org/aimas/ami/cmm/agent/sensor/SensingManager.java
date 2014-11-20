package org.aimas.ami.cmm.agent.config;

import jade.core.AID;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.sensor.AssertionAdaptorTracker;
import org.aimas.ami.cmm.agent.sensor.AssertionManager;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.osgi.framework.BundleContext;

public abstract class AbstractSensingManager {
	protected CMMAgent managingAgent;
	protected Map<String, AssertionManager> managedAssertions = new HashMap<String, AssertionManager>();
	protected Map<String, List<AID>> assertionUpdateDestinations = new HashMap<String, List<AID>>();
	
	public AbstractSensingManager(CMMAgent managingAgent) throws CMMConfigException {
		this.managingAgent = managingAgent;
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
	
	
	public void manageContextAssertion(String assertionResourceURI, String adaptorClassName,
			String updateMode, int updateRate, Map<String, String> assertionSources, AID[] destinationCoordinators) {
		
		// Get the physical (sensorID, type) list which is used, together with the adaptorClassName,
    	// to identify the matching ContextAssertionAdaptor implementation
		List<String> sensorIdList = new LinkedList<String>();
    	for (String instanceIdURI : assertionSources.keySet() ) {
    		String instanceType = assertionSources.get(instanceIdURI);
    		
    		sensorIdList.add(instanceIdURI + " " + instanceType);
    	}
    	
    	// Access the assertion adaptor service instance via ServiceTracker
    	BundleContext context = managingAgent.getOSGiBridge().getBundleContext();
    	AssertionAdaptorTracker adaptorTracker = new AssertionAdaptorTracker(context, adaptorClassName, sensorIdList);
    	
    	// When we create the assertion manager updates are not yet enabled.
    	// Enabling will be done by the CtxSensor agent, once it gets the OK from the CtxCoord
    	// for the ContextAssertions it wants to publish.
    	AssertionManager assertionManager = new AssertionManager(assertionResourceURI, updateMode, 
    			updateRate, adaptorTracker, this);
    	
    	// Create the list of assertion update destinatios
    	List<AID> assertionDestinationList = new LinkedList<AID>();
    	for (AID destinationCoord : destinationCoordinators) {
    		assertionDestinationList.add(destinationCoord);
    	}
    	
    	assertionUpdateDestinations.put(assertionResourceURI, assertionDestinationList);
    	managedAssertions.put(assertionResourceURI, assertionManager);
	}
	
	
	public void removeContextAssertion(String assertionResourceURI) {
		managedAssertions.remove(assertionResourceURI);
		assertionUpdateDestinations.remove(assertionResourceURI);
	}
	
	
	public void addCoordinatorDestination(String assertionResourceURI, AID ctxCoordinator) {
		List<AID> destinationCoordinators = assertionUpdateDestinations.get(assertionResourceURI);
		if (destinationCoordinators == null) {
			destinationCoordinators = new LinkedList<AID>();
			assertionUpdateDestinations.put(assertionResourceURI, destinationCoordinators);
		}
		
		destinationCoordinators.add(ctxCoordinator);
	}
	
	
	public void removeCoordinatorDestination(String assertionResourceURI, AID ctxCoordinator) {
		List<AID> destinationCoordinators = assertionUpdateDestinations.get(assertionResourceURI);
		destinationCoordinators.remove(ctxCoordinator);
	}
	
	
	public abstract void notifyAssertionActive(String assertionResourceURI, boolean updateEnabled);
}
