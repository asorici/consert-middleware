package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The message used by a CtxSensor or CtxUser agent to inform the delivery of a new ContextAssertion to a CtxCoord agent.
* Protege name: AssertionUpdate
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:54
*/
public class DefaultAssertionUpdate implements AssertionUpdate {

  private static final long serialVersionUID = 6722885266148375100L;

  private String _internalInstanceName = null;

  public DefaultAssertionUpdate() {
    this._internalInstanceName = "";
  }

  public DefaultAssertionUpdate(String instance_name) {
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
   * The SPARQL INSERT queries that constitute the content of this ContextAssertion update.
   * Protege name: assertionContent
   */
   private String assertionContent;
   public void setAssertionContent(String value) { 
    this.assertionContent=value;
   }
   public String getAssertionContent() {
     return this.assertionContent;
   }

}
