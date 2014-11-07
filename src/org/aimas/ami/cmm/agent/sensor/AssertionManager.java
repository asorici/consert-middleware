package org.aimas.ami.cmm.agent.sensor;

import org.aimas.ami.cmm.agent.config.AbstractSensingManager;
import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.AssertionUpdated;
import org.aimas.ami.cmm.agent.onto.ExecTask;
import org.aimas.ami.cmm.agent.onto.SetUpdateMode;
import org.aimas.ami.cmm.agent.onto.StartSending;
import org.aimas.ami.cmm.agent.onto.StopSending;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionDescription;
import org.aimas.ami.cmm.agent.onto.impl.DefaultAssertionUpdated;
import org.aimas.ami.cmm.sensing.ApplicationSensingAdaptor;
import org.aimas.ami.cmm.sensing.ContextAssertionAdaptor;
import org.aimas.ami.cmm.sensing.ContextAssertionDescription;

import com.hp.hpl.jena.update.UpdateRequest;

public class AssertionManager implements ApplicationSensingAdaptor {
	public static final String TIME_BASED 		= 	"time-based";
	public static final String CHANGE_BASED 	= 	"change-based";
	
	/** The active/inactive state of this AssertionManager depending on the 
	 * availability of its ContextAssertionAdaptor */
	private boolean active = false;
	
	/** The URI of ontology resource in the Context Model identifying this ContextAssertion */
	private String assertionResourceURI;
	
	/** Whether updates for this ContextAssertion are enabled */
	private boolean updateEnabled;
	
	/** The current update mode for this ContextAssertion */
	private String updateMode;
	
	/** The current update rate (in seconds) for this ContextAssertion */
	private int updateRate;
	
	private AbstractSensingManager sensingManager;
	private AssertionAdaptorTracker assertionAdaptorTracker;
	
	/**
	 * @param assertionResourceURI
	 * @param updateMode
	 * @param updateRate
	 */
    public AssertionManager(String assertionResourceURI, String updateMode, int updateRate,
    		AssertionAdaptorTracker assertionAdaptorTracker, AbstractSensingManager sensingManager) {
	    this.assertionResourceURI = assertionResourceURI;
	    this.updateMode = updateMode;
	    this.updateRate = updateRate;
	    this.updateEnabled = false;
	    
	    this.sensingManager = sensingManager;
	    this.assertionAdaptorTracker = assertionAdaptorTracker;
	    assertionAdaptorTracker.setAssertionManager(this);
	    assertionAdaptorTracker.open(true);
	    
	    ContextAssertionAdaptor assertionAdaptor = assertionAdaptorTracker.getService();
	    if (assertionAdaptor != null) {
	    	this.active = true;
	    	
	    	assertionAdaptor.registerSensingAdaptor(this);
	    	assertionAdaptor.setState(updateEnabled, updateMode, updateRate);
	    }
    }
    
    @Override
    public void deliverUpdate(ContextAssertionDescription assertionDesc, UpdateRequest update) {
	    // Create the AssertionUpdated message
    	AssertionUpdated assertionUpdate = new DefaultAssertionUpdated();
    	assertionUpdate.setAssertion(fromAdaptor(assertionDesc));
    	assertionUpdate.setAssertionContent(update.toString());
    	
    	AssertionUpdateBehaviour updateBehaviour = new AssertionUpdateBehaviour(sensingManager.getManagingAgent(), assertionUpdate);
    	sensingManager.getManagingAgent().addBehaviour(updateBehaviour);
    }
    
    public AssertionDescription getAssertionDescription() {
    	System.out.println("[Assertion Manager " + assertionResourceURI + "] Getting description");
    	
    	ContextAssertionAdaptor assertionAdaptor = assertionAdaptorTracker.getService();
	    if (assertionAdaptor != null) {
	    	ContextAssertionDescription desc = assertionAdaptor.getProvidedAssertion();
	    	
	    	AssertionDescription info = fromAdaptor(desc);
	    	return info;
	    }
	    
	    System.out.println("[Assertion Manager " + assertionResourceURI + "] Assertion Adaptor not available");
	    return null;
    }
    
    private AssertionDescription fromAdaptor(ContextAssertionDescription desc) {
    	DefaultAssertionDescription info = new DefaultAssertionDescription();
    	info.setAssertionType(desc.getContextAssertionURI());
    	
    	for (String annotationURI : desc.getSupportedAnnotationURIs()) {
    		info.addAnnotationType(annotationURI);
    	}
    	
    	return info;
    }
    
    public void setActive(boolean active) {
    	this.active = active;
    	
    	if (active) {
    		ContextAssertionAdaptor assertionAdaptor = assertionAdaptorTracker.getService();
    		if (assertionAdaptor != null) {
    			assertionAdaptor.setState(updateEnabled, updateMode, updateRate);
    			
    			// If we can contact the underlying assertion adaptor, notify the SensingManger that
    			// we are resuming the existing updateEnabled state.
    			sensingManager.notifyAssertionActive(assertionResourceURI, updateEnabled);
    		}
    	}
    	else {
    		// We can no longer contact the underlying assertion adaptor, so notify the SensingManager
    		// of this fact.
    		sensingManager.notifyAssertionActive(assertionResourceURI, false);
    	}
    }
    
    public boolean isActive() {
    	return active;
    }
    
    public String getAssertionResourceURI() {
		return assertionResourceURI;
	}
    
	public boolean updateEnabled() {
		return updateEnabled;
	}
	
	public boolean setUpdateEnabled(boolean updateEnabled) {
		ContextAssertionAdaptor assertionAdaptor = assertionAdaptorTracker.getService();
		
		if (assertionAdaptor != null) {
			if (assertionAdaptor.setUpdateEnabled(updateEnabled)) {
				this.updateEnabled = updateEnabled;
				sensingManager.notifyAssertionActive(assertionResourceURI, updateEnabled);
				
				return true;
			}
		}
		
		return false;
	}
	
	public String getUpdateMode() {
		return updateMode;
	}
	
	public int getUpdateRate() {
		return updateRate;
	}
	
	public boolean setUpdateMode(String updateMode, int updateRate) {
		ContextAssertionAdaptor assertionAdaptor = assertionAdaptorTracker.getService();
		
		if (assertionAdaptor != null) {
			if (assertionAdaptor.setUpdateMode(updateMode, updateRate)) {
				this.updateMode = updateMode;
				this.updateRate = updateRate;
				this.updateEnabled = true;
				
				sensingManager.notifyAssertionActive(assertionResourceURI, updateEnabled);
				
				return true;
			}
		}
		
		return false;
	}
	
	
	public boolean execTask(ExecTask task) {
		// switch based on the implementation type of the task
		if (task instanceof StartSending) {
			return setUpdateEnabled(true);
		}
		else if (task instanceof StopSending) {
			return setUpdateEnabled(false);
		}
		else if (task instanceof SetUpdateMode) {
			SetUpdateMode modeTask = (SetUpdateMode)task;
			return setUpdateMode(modeTask.getUpdateMode(), modeTask.getUpdateRate()); 
		}
		
		return false;
	}
}
