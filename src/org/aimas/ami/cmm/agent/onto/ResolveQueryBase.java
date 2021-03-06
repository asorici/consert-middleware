package org.aimas.ami.cmm.agent.onto;



/**
* The message sent by a CtxQueryHandler, or relayed by an OrgMgr agent in case of domain-based queries which cannot be handled by the local CtxQuertHandler.
* Protege name: ResolveQueryBase
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public interface ResolveQueryBase extends jade.content.AgentAction {

   /**
   * The query for which to collect the query base.
   * Protege name: forQuery
   */
   public void setForQuery(UserQuery value);
   public UserQuery getForQuery();

}
