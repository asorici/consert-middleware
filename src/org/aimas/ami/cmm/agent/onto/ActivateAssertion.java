package org.aimas.ami.cmm.agent.onto;



/**
* Message sent by a CtxQueryHandler to the CtxCoord to which he is associated when a ContextAssertion for which the query handler receives a query does not exist in the ContextStore.
* Protege name: ActivateAssertion
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public interface ActivateAssertion extends jade.content.AgentAction {

   /**
   * Indicates the ContextAssertion description for which this TaskingCommand is intended.
   * Protege name: assertion
   */
   public void setAssertion(AssertionDescription value);
   public AssertionDescription getAssertion();

}
