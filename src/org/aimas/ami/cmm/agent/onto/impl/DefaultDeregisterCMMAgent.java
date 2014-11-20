package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: DeregisterCMMAgent
* @author OntologyBeanGenerator v4.1
* @version 2014/11/12, 17:13:34
*/
public class DefaultDeregisterCMMAgent implements DeregisterCMMAgent {

  private static final long serialVersionUID = -742403669511333337L;

  private String _internalInstanceName = null;

  public DefaultDeregisterCMMAgent() {
    this._internalInstanceName = "";
  }

  public DefaultDeregisterCMMAgent(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: appIdentifier
   */
   private String appIdentifier;
   public void setAppIdentifier(String value) { 
    this.appIdentifier=value;
   }
   public String getAppIdentifier() {
     return this.appIdentifier;
   }

   /**
   * The type of the CMMAgent that requests registration with an OrgMgr.
   * Protege name: agentType
   */
   private String agentType;
   public void setAgentType(String value) { 
    this.agentType=value;
   }
   public String getAgentType() {
     return this.agentType;
   }

}
