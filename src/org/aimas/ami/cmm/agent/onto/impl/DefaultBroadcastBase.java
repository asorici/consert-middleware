package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* Protege name: BroadcastBase
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public class DefaultBroadcastBase implements BroadcastBase {

  private static final long serialVersionUID = 3798988534798726725L;

  private String _internalInstanceName = null;

  public DefaultBroadcastBase() {
    this._internalInstanceName = "";
  }

  public DefaultBroadcastBase(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: broadcastBaseItem
   */
   private List broadcastBaseItem = new ArrayList();
   public void addBroadcastBaseItem(BroadcastBaseItem elem) { 
     broadcastBaseItem.add(elem);
   }
   public boolean removeBroadcastBaseItem(BroadcastBaseItem elem) {
     boolean result = broadcastBaseItem.remove(elem);
     return result;
   }
   public void clearAllBroadcastBaseItem() {
     broadcastBaseItem.clear();
   }
   public Iterator getAllBroadcastBaseItem() {return broadcastBaseItem.iterator(); }
   public List getBroadcastBaseItem() {return broadcastBaseItem; }
   public void setBroadcastBaseItem(List l) {broadcastBaseItem = l; }

}
