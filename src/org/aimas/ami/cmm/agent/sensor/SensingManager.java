package org.aimas.ami.cmm.agent.sensor;

import java.util.LinkedList;
import java.util.List;

import org.aimas.ami.cmm.agent.CMMAgent;
import org.aimas.ami.cmm.agent.config.AbstractSensingManager;
import org.aimas.ami.cmm.agent.config.SensingPolicy;
import org.aimas.ami.cmm.agent.sensor.CtxSensor.SensorState;
import org.aimas.ami.cmm.api.CMMConfigException;
import org.aimas.ami.cmm.vocabulary.CoordConf;
import org.aimas.ami.cmm.vocabulary.SensorConf;
import org.osgi.framework.BundleContext;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class SensingManager extends AbstractSensingManager {
	
	public SensingManager(CMMAgent managingAgent, List<SensingPolicy> sensingPolicies) throws CMMConfigException {
		super(managingAgent);
		configure(sensingPolicies);
	}
	
	public void addPolicies(List<SensingPolicy> sensingPolicies) throws CMMConfigException {
		configure(sensingPolicies);
	}
	
	private void configure(List<SensingPolicy> sensingPolicies) throws CMMConfigException {
	    for (SensingPolicy sensingPolicy : sensingPolicies) {
	    	Resource assertionRes = sensingPolicy.getContextAssertionRes();
	    	String adaptorClassName = sensingPolicy.getAssertionAdaptorClass();
	    	String configureDoc = sensingPolicy.getFileNameOrURI();
	    	
	    	// Retrieve the ContextAssertion-specific sensing policy, the one that specifies
	    	// update mode, update rate and the list of physical sensor IDs to which it applies
	    	OntModel sensingConfigModel = managingAgent.getConfigurationLoader().load(configureDoc);
	    	Resource assertionSenseSpec = 
	    		sensingConfigModel.listResourcesWithProperty(CoordConf.forContextAssertion, assertionRes).next();
	    	
	    	// Get the updateMode and updateRate assertion update specifications
	    	String updateMode = assertionSenseSpec.getPropertyResourceValue(SensorConf.hasUpdateMode).getLocalName();
	    	int updateRate = 0;
	    	
	    	Statement updateRateStmt = assertionSenseSpec.getProperty(SensorConf.hasUpdateRate);
	    	if (updateRateStmt != null) {
	    		updateRate = updateRateStmt.getInt();
	    	}
	    	
	    	// Get the physical (sensorID, type) list which is used, together with the adaptorClassName,
	    	// to identify the matching ContextAssertionAdaptor implementation
	    	StmtIterator sensorIdIt = assertionSenseSpec.listProperties(SensorConf.appliesTo);
	    	List<String> sensorIdList = new LinkedList<String>();
	    	for (; sensorIdIt.hasNext(); ) {
	    		Resource sensorIdRes = sensorIdIt.next().getResource();
	    		Resource instanceIDRes = sensorIdRes.getPropertyResourceValue(SensorConf.instanceID);
	    		Resource instanceType = sensorIdRes.getPropertyResourceValue(SensorConf.instanceType);
	    		
	    		sensorIdList.add(instanceIDRes.getURI() + " " + instanceType.getURI());
	    	}
	    	
	    	// Access the assertion adaptor service instance via ServiceTracker
	    	BundleContext context = managingAgent.getOSGiBridge().getBundleContext();
	    	AssertionAdaptorTracker adaptorTracker = new AssertionAdaptorTracker(context, adaptorClassName, sensorIdList);
	    	
	    	// When we create the assertion manager updates are not yet enabled.
	    	// Enabling will be done by the CtxSensor agent, once it gets the OK from the CtxCoord
	    	// for the ContextAssertions it wants to publish.
	    	AssertionManager assertionManager = new AssertionManager(assertionRes.getURI(), updateMode, 
	    			updateRate, adaptorTracker, this);
	    
	    	managedAssertions.put(assertionRes.getURI(), assertionManager);
	    }
    }
	
	@Override
	public void notifyAssertionActive(String assertionResourceURI, boolean updateEnabled) {
	    CtxSensor ctxSensor = (CtxSensor)managingAgent;
		
		if (updateEnabled) {
	    	// It means that one of the managed assertions has resumed updates, so we set the
	    	// CtxSensor in a TRANSMITTING state if we are in the CONNECTED one
	    	if (ctxSensor.getSensorState() == SensorState.CONNECTED) {
	    		ctxSensor.setSensorState(SensorState.TRANSMITTING);
	    	}
	    }
	    else {
	    	// If we are in the transmitting state and one of our assertions stops its updates,
	    	// then we need to figure out if others we are managing are still active. If no more
	    	// are active, set the state to just CONNECTED
	    	if (ctxSensor.getSensorState() == SensorState.TRANSMITTING) {
	    		boolean hasActive = false;
	    		for (String assertionURI : managedAssertions.keySet()) {
	    			if (!assertionURI.equals(assertionResourceURI)) {
	    				AssertionManager assertionManager = managedAssertions.get(assertionURI);
	    				if (assertionManager.isActive() && assertionManager.updateEnabled()) {
	    					hasActive = true;
	    					break;
	    				}
	    			}
	    		}
	    		
	    		if (!hasActive) {
	    			ctxSensor.setSensorState(SensorState.CONNECTED);
	    		}
	    	}
	    }
	}
	
}
