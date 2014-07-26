package org.aimas.ami.cmm.resources.sensing;

import jade.core.behaviours.Behaviour;

import org.aimas.ami.cmm.agent.onto.AssertionDescription;
import org.aimas.ami.cmm.agent.onto.ExecTask;
import org.aimas.ami.cmm.agent.sensor.CtxSensor;

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
	public AssertionDescription getProvidedAssertion();
	
	// =========== ContextAssertion Update =========== //
	/** 
	 * Register the CtxSensor instance which commands this AssertionAdaptor and to which it 
	 * will be sending updates.
	 * @param sensorAgent the CtxSensor agent that handles the ContextAssertion sensing update.  
	 */
	public void registerSensorAgent(CtxSensor sensorAgent);
	
	/** 
	 * Deliver an update of the ContextAssertion to a CtxSensor agent. 
	 * This method is expected to produce a {@link SenseNotification} {@link Behaviour} 
	 * that is added to specified {@link CtxSensor} agent.
	 * @param sensorAgent the CtxSensor agent that handles the ContextAssertion sensing update.  
	 */
	public void deliverUpdate(CtxSensor sensorAgent);
	
	
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
