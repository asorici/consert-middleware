package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Message sent by an OrgMgr to a local (on the same machine) CtxSensor or CtxUser agent when the mobile machine they are running on enters a new ContextDomain.
* Protege name: DomainDetected
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public class DefaultDomainDetected implements DomainDetected {

  private static final long serialVersionUID = 7138998227437283033L;

  private String _internalInstanceName = null;

  public DefaultDomainDetected() {
    this._internalInstanceName = "";
  }

  public DefaultDomainDetected(String instance_name) {
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
