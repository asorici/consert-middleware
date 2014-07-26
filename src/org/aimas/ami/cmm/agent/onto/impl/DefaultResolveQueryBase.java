package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The message sent by a CtxQueryHandler, or relayed by an OrgMgr agent in case of domain-based queries which cannot be handled by the local CtxQuertHandler.
* Protege name: ResolveQueryBase
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public class DefaultResolveQueryBase implements ResolveQueryBase {

  private static final long serialVersionUID = -8749049500310255927L;

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
   * The query for which to collect the query base.
   * Protege name: forQuery
   */
   private Query forQuery;
   public void setForQuery(Query value) { 
    this.forQuery=value;
   }
   public Query getForQuery() {
     return this.forQuery;
   }

}
