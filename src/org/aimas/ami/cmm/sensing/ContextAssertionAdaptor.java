package org.aimas.ami.cmm.sensing;



public interface ContextAssertionAdaptor {
	public static final String CHANGE_BASED = "change-based";
	public static final String TIME_BASED = "time-based"; 
	
	public static final int ASSERTION_ENTITY_UPDATE 	=	1;
	public static final int ASSERTION_ID_CREATE 		=	2;
	public static final int ASSERTION_CONTENT_UPDATE 	= 	3;
	public static final int ASSERTION_ANNOTATION_UPDATE = 	4;
	
	/** The fully qualified name of the class implementing the ContextAssertionAdaptor service. */
	public static final String ADAPTOR_IMPL_CLASS	= "adaptor.implementation";
	
	/** The URI identifying the ContextAssertion which this adaptor service instance manages. */
	public static final String ADAPTOR_ASSERTION	= "adaptor.contextassertion";
	
	/** The JADE local name of the CMM CtxSensor / CtxUser that will use this adaptor service instance. */
	public static final String ADAPTOR_CMM_AGENT	= "adaptor.cmmagent";
	
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
