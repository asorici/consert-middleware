package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Predicate used by a OrgMgr to inform data about the current Context Domain as a response to a InformDomain request from a CMM Agent.
* Protege name: DomainDescription
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public class DefaultDomainDescription implements DomainDescription {

  private static final long serialVersionUID = 7138998227437283033L;

  private String _internalInstanceName = null;

  public DefaultDomainDescription() {
    this._internalInstanceName = "";
  }

  public DefaultDomainDescription(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The detected ContextDomain.
   * Protege name: domain
   */
   private ContextDomain domain;
   public void setDomain(ContextDomain value) { 
    this.domain=value;
   }
   public ContextDomain getDomain() {
     return this.domain;
   }

}
