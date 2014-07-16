package org.aimas.ami.cmm.agent.onto;


import jade.util.leap.*;

/**
* The message a CtxSensor or CtxUser sends to a CtxCoord agent proposing his capability of sending the ContextAssertions he is in charge of.
* Protege name: PublishAssertions
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:55
*/
public interface PublishAssertions extends jade.content.AgentAction {

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
