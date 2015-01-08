package org.aimas.ami.cmm.agent.onto.impl;


import org.aimas.ami.cmm.agent.onto.*;

/**
* The message sent by a CtxUser to a CtxQueryHandler to register a new query/subscription client.
* Protege name: RegisterUser
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultRegisterQueryRequester implements RegisterQueryRequester {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultRegisterQueryRequester() {
    this._internalInstanceName = "";
  }

  public DefaultRegisterQueryRequester(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The CMM Agent (CtxUser, OrgMgr or another CtxQueryHandler) agent that needs to be registered.
   * Protege name: queryRequester
   */
   private jade.core.AID queryRequester;
   public void setQueryRequester(jade.core.AID value) { 
    this.queryRequester=value;
   }
   public jade.core.AID getQueryRequester() {
     return this.queryRequester;
   }

}
