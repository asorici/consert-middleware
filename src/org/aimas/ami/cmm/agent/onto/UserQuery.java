package org.aimas.ami.cmm.agent.onto;



/**
* Base class for queries submitted by a CtxUser agent to a CtxQueryHandler.
* Protege name: UserQuery
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public interface UserQuery extends jade.content.Predicate {

   /**
   * The type of query submitted by a CtxUser: local (to its direct CtxQueryHandler) or domain-based
   * Protege name: queryTarget
   */
   public void setQueryTarget(String value);
   public String getQueryTarget();

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
   * The URI of the ContextDomain value that defines the upper bound (closest to the root) of the domain to pose the query to in the ContextDomain hieararchy.
   * Protege name: domain-upper-bound
   */
   public void setDomain_upper_bound(String value);
   public String getDomain_upper_bound();

   /**
   * In the case of a SUBSCRIBE message this optional field mentions the interval (in seconds) at which to send results of the submitted subscription query.
   * Protege name: repeatInterval
   */
   public void setRepeatInterval(int value);
   public int getRepeatInterval();

}
