package org.aimas.ami.cmm.agent.onto;



/**
* The message by which: 
 - a CtxSensor/CtxUser announces its presence to a  
   remote OrgMgr  
 - a CtxQueryHandler announces its presence to a 
   CtxCoord
* Protege name: IsPresent
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public interface IsPresent extends jade.content.Predicate {

   /**
   * The type of CMM agent.
   * Protege name: agent
   */
   public void setAgent(jade.core.AID value);
   public jade.core.AID getAgent();

}
