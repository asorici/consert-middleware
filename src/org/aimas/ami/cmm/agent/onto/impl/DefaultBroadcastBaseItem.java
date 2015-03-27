package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: BroadcastBaseItem
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public class DefaultBroadcastBaseItem implements BroadcastBaseItem {

  private static final long serialVersionUID = 3798988534798726725L;

  private String _internalInstanceName = null;

  public DefaultBroadcastBaseItem() {
    this._internalInstanceName = "";
  }

  public DefaultBroadcastBaseItem(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The CtxCoord agent to which the ContextAssertions of this assignment can be sent.
   * Protege name: coordinator
   */
   private jade.core.AID coordinator;
   public void setCoordinator(jade.core.AID value) { 
    this.coordinator=value;
   }
   public jade.core.AID getCoordinator() {
     return this.coordinator;
   }

   /**
   * Protege name: broadcastUpperBound
   */
   private String broadcastUpperBound;
   public void setBroadcastUpperBound(String value) { 
    this.broadcastUpperBound=value;
   }
   public String getBroadcastUpperBound() {
     return this.broadcastUpperBound;
   }

   /**
   * Protege name: broadcastLowerBound
   */
   private String broadcastLowerBound;
   public void setBroadcastLowerBound(String value) { 
    this.broadcastLowerBound=value;
   }
   public String getBroadcastLowerBound() {
     return this.broadcastLowerBound;
   }

}
