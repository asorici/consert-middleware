package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: UpdateProfiledAssertion
* @author OntologyBeanGenerator v4.1
* @version 2014/11/18, 18:02:30
*/
public class DefaultUpdateProfiledAssertion implements UpdateProfiledAssertion {

  private static final long serialVersionUID = -4256531765151457794L;

  private String _internalInstanceName = null;

  public DefaultUpdateProfiledAssertion() {
    this._internalInstanceName = "";
  }

  public DefaultUpdateProfiledAssertion(String instance_name) {
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
