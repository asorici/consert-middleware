package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The message sent by CMM agents to their OrgMgr agent asking for details of the current ContextDomain for the givent application identifier.
* Protege name: InformDomain
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public class DefaultInformDomain implements InformDomain {

  private static final long serialVersionUID = 7138998227437283033L;

  private String _internalInstanceName = null;

  public DefaultInformDomain() {
    this._internalInstanceName = "";
  }

  public DefaultInformDomain(String instance_name) {
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

}
