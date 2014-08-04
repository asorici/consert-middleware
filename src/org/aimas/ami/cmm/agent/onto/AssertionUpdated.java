package org.aimas.ami.cmm.agent.onto;



/**
* The message used by a CtxSensor or CtxUser agent to inform the delivery of a new ContextAssertion to a CtxCoord agent.
* Protege name: AssertionUpdated
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public interface AssertionUpdated extends jade.content.Predicate {

   /**
   * Indicates the ContextAssertion description for which this TaskingCommand is intended.
   * Protege name: assertion
   */
   public void setAssertion(AssertionDescription value);
   public AssertionDescription getAssertion();

   /**
   * The SPARQL INSERT queries that constitute the content of this ContextAssertion update.
   * Protege name: assertionContent
   */
   public void setAssertionContent(String value);
   public String getAssertionContent();

}
