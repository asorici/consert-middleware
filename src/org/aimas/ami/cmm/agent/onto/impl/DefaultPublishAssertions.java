package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* The message a CtxSensor or CtxUser sends to a CtxCoord agent proposing his capability of sending the ContextAssertions he is in charge of.
* Protege name: PublishAssertions
* @author OntologyBeanGenerator v4.1
* @version 2014/07/31, 13:46:14
*/
public class DefaultPublishAssertions implements PublishAssertions {

  private static final long serialVersionUID = 5438106203733924709L;

  private String _internalInstanceName = null;

  public DefaultPublishAssertions() {
    this._internalInstanceName = "";
  }

  public DefaultPublishAssertions(String instance_name) {
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
