package org.aimas.ami.cmm.agent.onto;



/**
* The message sent by CMM agents to their OrgMgr agent asking for details of the current ContextDomain for the givent application identifier.
* Protege name: InformDomain
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public interface InformDomain extends jade.content.AgentAction {

   /**
   * Protege name: appIdentifier
   */
   public void setAppIdentifier(String value);
   public String getAppIdentifier();

}
