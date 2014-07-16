package org.aimas.ami.cmm.agent.onto;



/**
* Tasking command that tells a CtxSensor agent what the desired update mode (time-based, change-based) for the given ContextAssertion is.
* Protege name: SetUpdateMode
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:55
*/
public interface SetUpdateMode extends ExecTask {

   /**
   * The update mode (time-based, change-based) desired for the UpdateMode command
   * Protege name: updateMode
   */
   public void setUpdateMode(String value);
   public String getUpdateMode();

   /**
   * The update rate (in milliseconds) for the UpdateMode command, in case it is of time-based type
   * Protege name: updateRate
   */
   public void setUpdateRate(int value);
   public int getUpdateRate();

}
