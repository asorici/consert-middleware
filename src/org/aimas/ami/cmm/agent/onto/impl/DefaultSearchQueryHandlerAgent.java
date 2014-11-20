package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The message that a CtxUser sends to an OrgMgr to get access to a CtxQueryHandler responding to queries about the Context Model pertaining to the ContextDomain that the OrgMgr manages,
* Protege name: SearchQueryHandlerAgent
* @author OntologyBeanGenerator v4.1
* @version 2014/11/15, 15:58:45
*/
public class DefaultSearchQueryHandlerAgent implements SearchQueryHandlerAgent {

  private static final long serialVersionUID = -4150817687749589253L;

  private String _internalInstanceName = null;

  public DefaultSearchQueryHandlerAgent() {
    this._internalInstanceName = "";
  }

  public DefaultSearchQueryHandlerAgent(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

}
