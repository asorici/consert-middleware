package org.aimas.ami.cmm.agent.onto;



/**
* The message by which a CtxSensor/CtxUser announces its presence to a remote OrgMgr
* Protege name: SensorPresent
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public interface SensorPresent extends jade.content.Predicate {

   /**
   * The type of CMM agent.
   * Protege name: agent
   */
   public void setAgent(jade.core.AID value);
   public jade.core.AID getAgent();

}
