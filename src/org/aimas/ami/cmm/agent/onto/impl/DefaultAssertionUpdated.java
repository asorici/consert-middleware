package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The message used by a CtxSensor or CtxUser agent to inform the delivery of a new ContextAssertion to a CtxCoord agent.
* Protege name: AssertionUpdated
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultAssertionUpdated implements AssertionUpdated {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultAssertionUpdated() {
    this._internalInstanceName = "";
  }

  public DefaultAssertionUpdated(String instance_name) {
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
