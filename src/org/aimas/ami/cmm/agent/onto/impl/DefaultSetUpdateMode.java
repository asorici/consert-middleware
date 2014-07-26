package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Tasking command that tells a CtxSensor agent what the desired update mode (time-based, change-based) for the given ContextAssertion is.
* Protege name: SetUpdateMode
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public class DefaultSetUpdateMode implements SetUpdateMode {

  private static final long serialVersionUID = -8749049500310255927L;

  private String _internalInstanceName = null;

  public DefaultSetUpdateMode() {
    this._internalInstanceName = "";
  }

  public DefaultSetUpdateMode(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Indicates the ContextAssertion description for which this TaskingCommand is intended.
   * Protege name: assertion
   */
   private AssertionDescription assertion;
   public void setAssertion(AssertionDescription value) { 
    this.assertion=value;
   }
   public AssertionDescription getAssertion() {
     return this.assertion;
   }

   /**
   * The update mode (time-based, change-based) desired for the UpdateMode command
   * Protege name: updateMode
   */
   private String updateMode;
   public void setUpdateMode(String value) { 
    this.updateMode=value;
   }
   public String getUpdateMode() {
     return this.updateMode;
   }

   /**
   * The update rate (in milliseconds) for the UpdateMode command, in case it is of time-based type
   * Protege name: updateRate
   */
   private int updateRate;
   public void setUpdateRate(int value) { 
    this.updateRate=value;
   }
   public int getUpdateRate() {
     return this.updateRate;
   }

}
