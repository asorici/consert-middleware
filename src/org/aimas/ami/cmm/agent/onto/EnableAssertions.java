package org.aimas.ami.cmm.agent.onto;


import jade.util.leap.*;

/**
* Protege name: EnableAssertions
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public interface EnableAssertions extends jade.content.AgentAction {

   /**
   * The type of ContextAssertion that a CtxSensor or CtxUser can provide.
   * Protege name: capability
   */
   public void addCapability(AssertionDescription elem);
   public boolean removeCapability(AssertionDescription elem);
   public void clearAllCapability();
   public Iterator getAllCapability();
   public List getCapability();
   public void setCapability(List l);

}
