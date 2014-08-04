package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The message by which a CtxSensor/CtxUser announces its presence to a remote OrgMgr
* Protege name: SensorPresent
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultSensorPresent implements SensorPresent {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultSensorPresent() {
    this._internalInstanceName = "";
  }

  public DefaultSensorPresent(String instance_name) {
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
