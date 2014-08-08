package org.aimas.ami.cmm.api;

import jade.core.behaviours.Behaviour;

import org.aimas.ami.cmm.agent.sensor.CtxSensor;

import com.hp.hpl.jena.update.UpdateRequest;

public interface ContextAssertionAdaptor {
	// ======== ContextAssertion Capabilities ======== //
	/**
	 * @return Whether the physical sensor which is managed by this adaptor natively supports
	 * change-based updates.
	 */
	public boolean supportsChangeBasedUpdateMode();
	
	/**
	 * @return The minimum number of seconds that must pass between two successive notifications 
	 * in a time-based update mode.
	 */
	public int getMaxUpdateRate();
	
	/**
	 * @return The description (URI of ContextAssertion and, optionally, 
	 * the URIs of provided ContextAnnotations) for the ContextAssertion managed by this adaptor.
	 */
	public ContextAssertionDescription getProvidedAssertion();
	
	// =========== ContextAssertion Update =========== //
	/** 
	 * Register the ApplicationSensingAdaptor instance which commands this AssertionAdaptor and to which it 
	 * will be sending updates.
	 * @param sensingAdaptor the ApplicationSensingAdaptor that handles the ContextAssertion sensing update.  
	 */
	public void registerSensingAdaptor(ApplicationSensingAdaptor sensingAdaptor);
	
	/** 
	 * Deliver an update of the ContextAssertion to a ApplicationSensingAdaptor instance. 
	 * This method is expected to produce an {@link UpdateRequest} that contains the content and annotation update
	 * of the ContextAssertion that this adaptor manages.
	 * @param sensorAdaptor the ApplicationSensingAdaptor agent that handles the ContextAssertion sensing update.  
	 */
	public UpdateRequest deliverUpdate(ApplicationSensingAdaptor sensingAdaptor);
	
	
	// ========== TASKING Commands Handling ========== //
	/**
	 * Execute a Start/Stop Sending TASKING Command.
	 * @param active The requested state of functioning.
	 * @return	True if <code>task</code> has been executed successfully or false 
	 * if the sensor cannot comply with the requested command.
	 */
	public boolean setUpdateEnabled(boolean active);
	
	/**
	 * Execute a Set Update Mode TASKING Command.
	 * @param updateMode The requested update mode (time-based or change-based)
	 * @param updateRate The requested update rate if using a time-based update mode or 0 otherwise
	 * @return	True if <code>task</code> has been executed successfully or false 
	 * if the sensor cannot comply with the requested command.
	 */
	public boolean setUpdateMode(String updateMode, int updateRate);
	
	/**
	 * Sets the update state. This function is usually called at initialization (or change of adaptor)
	 * to set initial update configuration for the physical sensor.
	 * @param active Indicates whether updates for the ContextAssertion are active
	 * @param updateMode The update mode (time-based, change-based)
	 * @param updateRate The update rate - interval (in seconds) at which to send an update
	 */
	public void setState(boolean active, String updateMode, int updateRate);
}
