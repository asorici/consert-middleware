package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Message sent by a CtxUser to a CtxSensor that forces it to connect to a given OrgMgr agent of a new ContextDomain as decided by the CtxUser.
* Protege name: ConnectToDomain
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:55
*/
public class DefaultConnectToDomain implements ConnectToDomain {

  private static final long serialVersionUID = 6722885266148375100L;

  private String _internalInstanceName = null;

  public DefaultConnectToDomain() {
    this._internalInstanceName = "";
  }

  public DefaultConnectToDomain(String instance_name) {
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
