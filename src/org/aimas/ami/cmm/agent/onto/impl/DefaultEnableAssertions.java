package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* Message sent by a CtxQueryHandler to the CtxCoord to which he is associated when a ContextAssertion for which the query handler receives a query does not exist in the ContextStore.
The message also applies as the reply sent by a CtxCoord to a CtxSensor/CtxUser agent in response to a PublishAssertions message.
* Protege name: EnableAssertions
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultEnableAssertions implements EnableAssertions {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultEnableAssertions() {
    this._internalInstanceName = "";
  }

  public DefaultEnableAssertions(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The type of ContextAssertion that a CtxSensor or CtxUser can provide.
   * Protege name: capability
   */
   private List capability = new ArrayList();
   public void addCapability(AssertionCapability elem) { 
     capability.add(elem);
   }
   public boolean removeCapability(AssertionCapability elem) {
     boolean result = capability.remove(elem);
     return result;
   }
   public void clearAllCapability() {
     capability.clear();
   }
   public Iterator getAllCapability() {return capability.iterator(); }
   public List getCapability() {return capability; }
   public void setCapability(List l) {capability = l; }

}
