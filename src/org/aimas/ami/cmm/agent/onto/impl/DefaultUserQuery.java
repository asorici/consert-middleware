package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Base class for queries submitted by a CtxUser agent to a CtxQueryHandler.
* Protege name: UserQuery
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultUserQuery implements UserQuery {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultUserQuery() {
    this._internalInstanceName = "";
  }

  public DefaultUserQuery(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The URI of the ContextDomain value that defines the lower bound of the domain to pose the query to in the ContextDomain hieararchy.
   * Protege name: domain-lower-bound
   */
   private String domain_lower_bound;
   public void setDomain_lower_bound(String value) { 
    this.domain_lower_bound=value;
   }
   public String getDomain_lower_bound() {
     return this.domain_lower_bound;
   }

   /**
   * The type of query submitted by a CtxUser: local (to its direct CtxQueryHandler) or domain-based
   * Protege name: queryTarget
   */
   private String queryTarget;
   public void setQueryTarget(String value) { 
    this.queryTarget=value;
   }
   public String getQueryTarget() {
     return this.queryTarget;
   }

   /**
   * The SPARQL content of the query that is being sent to the CtxQueryHandler.
   * Protege name: queryContent
   */
   private String queryContent;
   public void setQueryContent(String value) { 
    this.queryContent=value;
   }
   public String getQueryContent() {
     return this.queryContent;
   }

   /**
   * The URI of the ContextDomain value that defines the upper bound (closest to the root) of the domain to pose the query to in the ContextDomain hieararchy.
   * Protege name: domain-upper-bound
   */
   private String domain_upper_bound;
   public void setDomain_upper_bound(String value) { 
    this.domain_upper_bound=value;
   }
   public String getDomain_upper_bound() {
     return this.domain_upper_bound;
   }

   /**
   * In the case of a SUBSCRIBE message this optional field mentions the interval (in seconds) at which to send results of the submitted subscription query.
   * Protege name: repeatInterval
   */
   private int repeatInterval;
   public void setRepeatInterval(int value) { 
    this.repeatInterval=value;
   }
   public int getRepeatInterval() {
     return this.repeatInterval;
   }

}
