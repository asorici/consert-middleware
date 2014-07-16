package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The base class for tasking commands that a CtxSensor agent can receive from a CtxCoord agent.
* Protege name: ExecTask
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:55
*/
public class DefaultExecTask implements ExecTask {

  private static final long serialVersionUID = 6722885266148375100L;

  private String _internalInstanceName = null;

  public DefaultExecTask() {
    this._internalInstanceName = "";
  }

  public DefaultExecTask(String instance_name) {
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
