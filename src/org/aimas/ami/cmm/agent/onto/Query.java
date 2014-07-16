package org.aimas.ami.cmm.agent.onto;



/**
* Base class for queries submitted by a CtxUser agent to a CtxQueryHandler.
* Protege name: Query
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:54
*/
public interface Query extends jade.content.Concept {

   /**
   * The URI of the ContextDomain value that defines the lower bound of the domain to pose the query to in the ContextDomain hieararchy.
   * Protege name: domain-lower-bound
   */
   public void setDomain_lower_bound(String value);
   public String getDomain_lower_bound();

   /**
   * The SPARQL content of the query that is being sent to the CtxQueryHandler.
   * Protege name: queryContent
   */
   public void setQueryContent(String value);
   public String getQueryContent();

   /**
   * The type of query submitted by a CtxUser: local (to its direct CtxQueryHandler) or domain-based
   * Protege name: queryType
   */
   public void setQueryType(String value);
   public String getQueryType();

   /**
   * The URI of the ContextDomain value that defines the upper bound (closest to the root) of the domain to pose the query to in the ContextDomain hieararchy.
   * Protege name: domain-upper-bound
   */
   public void setDomain_upper_bound(String value);
   public String getDomain_upper_bound();

}
