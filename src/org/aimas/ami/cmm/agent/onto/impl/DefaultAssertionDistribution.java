package org.aimas.ami.cmm.agent.onto.impl;


import jade.util.leap.*;
import org.aimas.ami.cmm.agent.onto.*;

/**
* A set of AssertionAssignments that a OrgMgr sends to a CtxSensor or CtxUser that has announced its presence and capabilities.
* Protege name: AssertionDistribution
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:54
*/
public class DefaultAssertionDistribution implements AssertionDistribution {

  private static final long serialVersionUID = 6722885266148375100L;

  private String _internalInstanceName = null;

  public DefaultAssertionDistribution() {
    this._internalInstanceName = "";
  }

  public DefaultAssertionDistribution(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * The AssertionAssignments that a OrgMgr communicates to a CtxSensor or CtxUser agent following IsPresent and PublishAssertions messages sent by those agents.
   * Protege name: assignment
   */
   private List assignment = new ArrayList();
   public void addAssignment(String elem) { 
     assignment.add(elem);
   }
   public boolean removeAssignment(String elem) {
     boolean result = assignment.remove(elem);
     return result;
   }
   public void clearAllAssignment() {
     assignment.clear();
   }
   public Iterator getAllAssignment() {return assignment.iterator(); }
   public List getAssignment() {return assignment; }
   public void setAssignment(List l) {assignment = l; }

}
