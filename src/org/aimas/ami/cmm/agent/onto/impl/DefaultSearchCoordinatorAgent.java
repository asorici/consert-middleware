package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* Message addressed by a CtxUser, CtxSensor or CtxQueryHandler to their assigned OrgMgr to find the AID of the CtxCoordinator that managed the context provisioning associated with the given ContextDomain.
* Protege name: SearchCoordinatorAgent
* @author OntologyBeanGenerator v4.1
* @version 2014/11/15, 15:58:45
*/
public class DefaultSearchCoordinatorAgent implements SearchCoordinatorAgent {

  private static final long serialVersionUID = -4150817687749589253L;

  private String _internalInstanceName = null;

  public DefaultSearchCoordinatorAgent() {
    this._internalInstanceName = "";
  }

  public DefaultSearchCoordinatorAgent(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

}
