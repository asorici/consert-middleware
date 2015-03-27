package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: UpdateProfiledAssertion
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public class DefaultUpdateProfiledAssertion implements UpdateProfiledAssertion {

  private static final long serialVersionUID = 3798988534798726725L;

  private String _internalInstanceName = null;

  public DefaultUpdateProfiledAssertion() {
    this._internalInstanceName = "";
  }

  public DefaultUpdateProfiledAssertion(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Indicates the ContextAssertion description for which this TaskingCommand is intended.
   * Protege name: assertion
   */
   private AssertionDescription assertion;
   public void setAssertion(AssertionDescription value) { 
    this.assertion=value;
   }
   public AssertionDescription getAssertion() {
     return this.assertion;
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
   * The SPARQL INSERT queries that constitute the content of this ContextAssertion update.
   * Protege name: assertionContent
   */
   private String assertionContent;
   public void setAssertionContent(String value) { 
    this.assertionContent=value;
   }
   public String getAssertionContent() {
     return this.assertionContent;
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
