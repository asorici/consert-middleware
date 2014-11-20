package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: UpdateProfiledAssertion
* @author OntologyBeanGenerator v4.1
* @version 2014/11/18, 18:02:30
*/
public interface UpdateProfiledAssertion extends jade.content.AgentAction {

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
