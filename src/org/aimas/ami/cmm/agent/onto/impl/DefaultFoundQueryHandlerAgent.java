package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: FoundQueryHandlerAgent
* @author OntologyBeanGenerator v4.1
* @version 2014/11/15, 16:03:39
*/
public class DefaultFoundQueryHandlerAgent implements FoundQueryHandlerAgent {

  private static final long serialVersionUID = -3171230109543094309L;

  private String _internalInstanceName = null;

  public DefaultFoundQueryHandlerAgent() {
    this._internalInstanceName = "";
  }

  public DefaultFoundQueryHandlerAgent(String instance_name) {
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
