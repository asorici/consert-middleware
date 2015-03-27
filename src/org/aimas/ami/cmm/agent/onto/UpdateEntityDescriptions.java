package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: UpdateEntityDescriptions
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public interface UpdateEntityDescriptions extends jade.content.AgentAction {

   /**
   * The URI of the ContextDomain value that defines the lower bound of the domain to pose the query to in the ContextDomain hieararchy.
   * Protege name: domain-lower-bound
   */
   public void setDomain_lower_bound(String value);
   public String getDomain_lower_bound();

   /**
   * Protege name: entityContents
   */
   public void setEntityContents(String value);
   public String getEntityContents();

   /**
   * The URI of the ContextDomain value that defines the upper bound (closest to the root) of the domain to pose the query to in the ContextDomain hieararchy.
   * Protege name: domain-upper-bound
   */
   public void setDomain_upper_bound(String value);
   public String getDomain_upper_bound();

}
