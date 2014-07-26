package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Message sent by the OrgMgr to a CtxSensor shared between multiple applications when one of those applications is installed, becomes active or inactive.
* Protege name: ApplyConfiguration
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public class DefaultApplyConfiguration implements ApplyConfiguration {

  private static final long serialVersionUID = -8749049500310255927L;

  private String _internalInstanceName = null;

  public DefaultApplyConfiguration() {
    this._internalInstanceName = "";
  }

  public DefaultApplyConfiguration(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The RDF content of the agent specific configuration expressed according to the vocabulary of the OrgMgrConf ontology.
   * Protege name: configContent
   */
   private String configContent;
   public void setConfigContent(String value) { 
    this.configContent=value;
   }
   public String getConfigContent() {
     return this.configContent;
   }

}
