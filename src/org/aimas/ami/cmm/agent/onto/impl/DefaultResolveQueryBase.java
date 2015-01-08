package org.aimas.ami.cmm.agent.onto.impl;


import jade.core.AID;

import org.aimas.ami.cmm.agent.onto.ResolveQueryBase;
import org.aimas.ami.cmm.agent.onto.UserQuery;

/**
* The message sent by a CtxQueryHandler, or relayed by an OrgMgr agent in case of domain-based queries which cannot be handled by the local CtxQueryHandler.
* Protege name: ResolveQueryBase
* @author OntologyBeanGenerator v4.1
* @version 2014/12/16, 20:08:31
*/
public class DefaultResolveQueryBase implements ResolveQueryBase {

  private static final long serialVersionUID = 220153654612988791L;

  private String _internalInstanceName = null;

  public DefaultResolveQueryBase() {
    this._internalInstanceName = "";
  }

  public DefaultResolveQueryBase(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

  /**
   * Indicates the Provisioning Agent (CtxUser or other CtxQueryHandler) from which this query was received.
   * Protege name: receivedFromAgent
   */
   private AID receivedFromAgent;
   public void setReceivedFromAgent(AID value) { 
    this.receivedFromAgent=value;
   }
   public AID getReceivedFromAgent() {
     return this.receivedFromAgent;
   }

   /**
   * The query for which to collect the query base.
   * Protege name: query
   */
   private UserQuery query;
   public void setQuery(UserQuery value) { 
    this.query=value;
   }
   public UserQuery getQuery() {
     return this.query;
   }

}
