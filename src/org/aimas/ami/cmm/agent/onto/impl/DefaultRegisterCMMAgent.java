package org.aimas.ami.cmm.agent.onto.impl;

import org.aimas.ami.cmm.agent.onto.RegisterCMMAgent;



/**
* Protege name: RegisterCMMAgent
* @author OntologyBeanGenerator v4.1
* @version 2014/11/12, 17:13:34
*/
public class DefaultRegisterCMMAgent implements RegisterCMMAgent {

  private static final long serialVersionUID = -742403669511333337L;

  private String _internalInstanceName = null;

  public DefaultRegisterCMMAgent() {
    this._internalInstanceName = "";
  }

  public DefaultRegisterCMMAgent(String instance_name) {
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
   * Protege name: agentActive
   */
   private boolean agentActive;
   public void setAgentActive(boolean value) { 
    this.agentActive=value;
   }
   public boolean getAgentActive() {
     return this.agentActive;
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
