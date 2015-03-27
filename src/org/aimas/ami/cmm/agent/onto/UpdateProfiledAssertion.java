package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: UpdateProfiledAssertion
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public interface UpdateProfiledAssertion extends jade.content.AgentAction {

   /**
   * Indicates the ContextAssertion description for which this TaskingCommand is intended.
   * Protege name: assertion
   */
   public void setAssertion(AssertionDescription value);
   public AssertionDescription getAssertion();

   /**
   * The URI of the ContextDomain value that defines the lower bound of the domain to pose the query to in the ContextDomain hieararchy.
   * Protege name: domain-lower-bound
   */
   public void setDomain_lower_bound(String value);
   public String getDomain_lower_bound();

   /**
   * The SPARQL INSERT queries that constitute the content of this ContextAssertion update.
   * Protege name: assertionContent
   */
   public void setAssertionContent(String value);
   public String getAssertionContent();

   /**
   * The URI of the ContextDomain value that defines the upper bound (closest to the root) of the domain to pose the query to in the ContextDomain hieararchy.
   * Protege name: domain-upper-bound
   */
   public void setDomain_upper_bound(String value);
   public String getDomain_upper_bound();

}
