package org.aimas.ami.cmm.agent.onto;


import jade.util.leap.*;

/**
* Message sent by a CtxQueryHandler to the CtxCoord to which he is associated when a ContextAssertion for which the query handler receives a query does not exist in the ContextStore.
The message also applies as the reply sent by a CtxCoord to a CtxSensor/CtxUser agent in response to a PublishAssertions message.
* Protege name: EnableAssertions
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
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
