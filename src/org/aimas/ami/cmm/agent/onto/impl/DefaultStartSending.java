package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Tasking command that tells a CtxSensor agent to start sending updates of the given ContextAssertion.
* Protege name: StartSending
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public class DefaultStartSending implements StartSending {

  private static final long serialVersionUID = 7138998227437283033L;

  private String _internalInstanceName = null;

  public DefaultStartSending() {
    this._internalInstanceName = "";
  }

  public DefaultStartSending(String instance_name) {
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

}
