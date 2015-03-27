package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: UpdateEntityDescriptions
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public class DefaultUpdateEntityDescriptions implements UpdateEntityDescriptions {

  private static final long serialVersionUID = 3798988534798726725L;

  private String _internalInstanceName = null;

  public DefaultUpdateEntityDescriptions() {
    this._internalInstanceName = "";
  }

  public DefaultUpdateEntityDescriptions(String instance_name) {
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
   * Protege name: entityContents
   */
   private String entityContents;
   public void setEntityContents(String value) { 
    this.entityContents=value;
   }
   public String getEntityContents() {
     return this.entityContents;
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
