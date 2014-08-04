package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The combined description of the Assertion domain and annotation content and the available update mode and rate.
* Protege name: AssertionCapability
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultAssertionCapability implements AssertionCapability {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultAssertionCapability() {
    this._internalInstanceName = "";
  }

  public DefaultAssertionCapability(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: availableUpdateRate
   */
   private int availableUpdateRate;
   public void setAvailableUpdateRate(int value) { 
    this.availableUpdateRate=value;
   }
   public int getAvailableUpdateRate() {
     return this.availableUpdateRate;
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
   * Protege name: availableUpdateMode
   */
   private String availableUpdateMode;
   public void setAvailableUpdateMode(String value) { 
    this.availableUpdateMode=value;
   }
   public String getAvailableUpdateMode() {
     return this.availableUpdateMode;
   }

}
