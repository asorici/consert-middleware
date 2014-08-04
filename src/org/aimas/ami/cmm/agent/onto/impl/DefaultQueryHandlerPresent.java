package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The message by which a CtxQueryHandler announces its presence to a CtxCoord.
* Protege name: QueryHandlerPresent
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultQueryHandlerPresent implements QueryHandlerPresent {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultQueryHandlerPresent() {
    this._internalInstanceName = "";
  }

  public DefaultQueryHandlerPresent(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: isPrimary
   */
   private boolean isPrimary;
   public void setIsPrimary(boolean value) { 
    this.isPrimary=value;
   }
   public boolean getIsPrimary() {
     return this.isPrimary;
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
