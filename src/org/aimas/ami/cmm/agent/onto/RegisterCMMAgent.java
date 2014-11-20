package org.aimas.ami.cmm.agent.onto;



/**
* Protege name: RegisterCMMAgent
* @author OntologyBeanGenerator v4.1
* @version 2014/11/12, 17:13:34
*/
public interface RegisterCMMAgent extends jade.content.AgentAction {

   /**
   * Protege name: appIdentifier
   */
   public void setAppIdentifier(String value);
   public String getAppIdentifier();

   /**
   * Protege name: agentState
   */
   public void setAgentActive(boolean value);
   public boolean isAgentActive();

   /**
   * The type of the CMMAgent that requests registration with an OrgMgr.
   * Protege name: agentType
   */
   public void setAgentService(String value);
   public String getAgentService();

}
