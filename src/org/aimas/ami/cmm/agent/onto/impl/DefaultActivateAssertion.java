package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Message sent by a CtxQueryHandler to the CtxCoord to which he is associated when a ContextAssertion for which the query handler receives a query does not exist in the ContextStore.
* Protege name: ActivateAssertion
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:55
*/
public class DefaultActivateAssertion implements ActivateAssertion {

  private static final long serialVersionUID = 6722885266148375100L;

  private String _internalInstanceName = null;

  public DefaultActivateAssertion() {
    this._internalInstanceName = "";
  }

  public DefaultActivateAssertion(String instance_name) {
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
