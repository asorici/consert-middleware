package org.aimas.ami.cmm.agent.sensor;

import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.ExecTask;
import org.aimas.ami.cmm.agent.onto.SetUpdateMode;
import org.aimas.ami.cmm.agent.onto.StartSending;
import org.aimas.ami.cmm.agent.onto.StopSending;
import org.aimas.ami.cmm.resources.ContextAssertionAdaptor;

public class AssertionManager {
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
	
	private SensingManager sensingManager;
	private AssertionAdaptorTracker assertionAdaptorTracker;
	
	
	/**
	 * @param assertionResourceURI
	 * @param updateMode
	 * @param updateRate
	 */
    public AssertionManager(String assertionResourceURI, String updateMode, int updateRate,
    		AssertionAdaptorTracker assertionAdaptorTracker, SensingManager sensingManager) {
	    this.assertionResourceURI = assertionResourceURI;
	    this.updateMode = updateMode;
	    this.updateRate = updateRate;
	    this.updateEnabled = false;
	    
	    this.sensingManager = sensingManager;
	    this.assertionAdaptorTracker = assertionAdaptorTracker;
	    assertionAdaptorTracker.setAssertionManager(this);
	    
	    ContextAssertionAdaptor assertionAdaptor = assertionAdaptorTracker.getService();
	    if (assertionAdaptor != null) {
	    	this.active = true;
	    	
	    	assertionAdaptor.setState(updateEnabled, updateMode, updateRate);
	    }
    }
    
    public AssertionDescription getAssertionDescription() {
    	ContextAssertionAdaptor assertionAdaptor = assertionAdaptorTracker.getService();
	    if (assertionAdaptor != null) {
	    	return assertionAdaptor.getProvidedAssertion();
	    }
	    
	    return null;
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
