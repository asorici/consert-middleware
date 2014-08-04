package org.aimas.ami.cmm.agent.onto;



/**
* The combined description of the Assertion domain and annotation content and the available update mode and rate.
* Protege name: AssertionCapability
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public interface AssertionCapability extends jade.content.Concept {

   /**
   * Protege name: availableUpdateRate
   */
   public void setAvailableUpdateRate(int value);
   public int getAvailableUpdateRate();

   /**
   * Indicates the ContextAssertion description for which this TaskingCommand is intended.
   * Protege name: assertion
   */
   public void setAssertion(AssertionDescription value);
   public AssertionDescription getAssertion();

   /**
   * Protege name: availableUpdateMode
   */
   public void setAvailableUpdateMode(String value);
   public String getAvailableUpdateMode();

}
