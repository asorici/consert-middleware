package org.aimas.ami.cmm.agent.onto;



/**
* Message sent by a CtxUser to a CtxSensor that forces it to connect to a given OrgMgr agent of a new ContextDomain as decided by the CtxUser.
* Protege name: ConnectToDomain
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public interface ConnectToDomain extends jade.content.AgentAction {

   /**
   * The detected ContextDomain.
   * Protege name: domain
   */
   public void setDomain(ContextDomain value);
   public ContextDomain getDomain();

}
