package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The message by which: 
 - a CtxSensor/CtxUser announces its presence to a  
   remote OrgMgr  
 - a CtxQueryHandler announces its presence to a 
   CtxCoord
* Protege name: IsPresent
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:55
*/
public class DefaultIsPresent implements IsPresent {

  private static final long serialVersionUID = 6722885266148375100L;

  private String _internalInstanceName = null;

  public DefaultIsPresent() {
    this._internalInstanceName = "";
  }

  public DefaultIsPresent(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The type of CMM agent.
   * Protege name: agent
   */
   private jade.core.AID agent;
   public void setAgent(jade.core.AID value) { 
    this.agent=value;
   }
   public jade.core.AID getAgent() {
     return this.agent;
   }

}
