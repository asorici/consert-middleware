package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Base class for queries submitted by a CtxUser agent to a CtxQueryHandler.
* Protege name: Query
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public class DefaultQuery implements Query {

  private static final long serialVersionUID = -8749049500310255927L;

  private String _internalInstanceName = null;

  public DefaultQuery() {
    this._internalInstanceName = "";
  }

  public DefaultQuery(String instance_name) {
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
   * The type of query submitted by a CtxUser: local (to its direct CtxQueryHandler) or domain-based
   * Protege name: queryType
   */
   private String queryType;
   public void setQueryType(String value) { 
    this.queryType=value;
   }
   public String getQueryType() {
     return this.queryType;
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

}
