package org.aimas.ami.cmm.agent.onto;


import jade.util.leap.*;

/**
* Protege name: BroadcastBase
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public interface BroadcastBase extends jade.content.Predicate {

   /**
   * Protege name: broadcastBaseItem
   */
   public void addBroadcastBaseItem(BroadcastBaseItem elem);
   public boolean removeBroadcastBaseItem(BroadcastBaseItem elem);
   public void clearAllBroadcastBaseItem();
   public Iterator getAllBroadcastBaseItem();
   public List getBroadcastBaseItem();
   public void setBroadcastBaseItem(List l);

}
