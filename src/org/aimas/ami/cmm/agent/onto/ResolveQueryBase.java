package org.aimas.ami.cmm.agent.onto;

import jade.core.AID;



/**
* The message sent by a CtxQueryHandler, or relayed by an OrgMgr agent in case of domain-based queries which cannot be handled by the local CtxQueryHandler.
* Protege name: ResolveQueryBase
* @author OntologyBeanGenerator v4.1
* @version 2014/12/16, 20:08:31
*/
public interface ResolveQueryBase extends jade.content.AgentAction {

   /**
   * Indicates the Provisioning Agent (CtxUser or other CtxQueryHandler) from which this query was received.
   * Protege name: receivedFromAgent
   */
   public void setReceivedFromAgent(AID value);
   public AID getReceivedFromAgent();

   /**
   * The query for which to collect the query base.
   * Protege name: query
   */
   public void setQuery(UserQuery value);
   public UserQuery getQuery();

}
