package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* A request sent by the OrgMgr agent to a CtxSensor or CtxUser agent that has announced its presence to the remote manager.
* Protege name: InformAssertions
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultInformAssertions implements InformAssertions {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultInformAssertions() {
    this._internalInstanceName = "";
  }

  public DefaultInformAssertions(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

}
