package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: DeregisterCMMAgent
* @author OntologyBeanGenerator v4.1
* @version 2014/11/12, 17:13:34
*/
public interface DeregisterCMMAgent extends jade.content.AgentAction {

   /**
   * Protege name: appIdentifier
   */
   public void setAppIdentifier(String value);
   public String getAppIdentifier();

   /**
   * The type of the CMMAgent that requests registration with an OrgMgr.
   * Protege name: agentType
   */
   public void setAgentType(String value);
   public String getAgentType();

}
