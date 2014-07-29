package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Action requested by an OrgMgr to a CtxCoord as part of the resolve-query-base process. The CtxCoord must select one CtxQueryHandler in its subordination to answer the query.
* Protege name: CollectQueryBase
* @author OntologyBeanGenerator v4.1
* @version 2014/07/29, 17:38:36
*/
public class DefaultCollectQueryBase implements CollectQueryBase {

  private static final long serialVersionUID = 7138998227437283033L;

  private String _internalInstanceName = null;

  public DefaultCollectQueryBase() {
    this._internalInstanceName = "";
  }

  public DefaultCollectQueryBase(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

}
