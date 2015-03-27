package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: BroadcastBaseItem
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public interface BroadcastBaseItem extends jade.content.Concept {

   /**
   * The CtxCoord agent to which the ContextAssertions of this assignment can be sent.
   * Protege name: coordinator
   */
   public void setCoordinator(jade.core.AID value);
   public jade.core.AID getCoordinator();

   /**
   * Protege name: broadcastUpperBound
   */
   public void setBroadcastUpperBound(String value);
   public String getBroadcastUpperBound();

   /**
   * Protege name: broadcastLowerBound
   */
   public void setBroadcastLowerBound(String value);
   public String getBroadcastLowerBound();

}
