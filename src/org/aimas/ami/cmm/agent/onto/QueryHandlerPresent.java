package org.aimas.ami.cmm.agent.onto;



/**
* The message by which a CtxQueryHandler announces its presence to a CtxCoord.
* Protege name: QueryHandlerPresent
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public interface QueryHandlerPresent extends jade.content.Predicate {

   /**
   * Protege name: isPrimary
   */
   public void setIsPrimary(boolean value);
   public boolean getIsPrimary();

   /**
   * The type of CMM agent.
   * Protege name: agent
   */
   public void setAgent(jade.core.AID value);
   public jade.core.AID getAgent();

}
