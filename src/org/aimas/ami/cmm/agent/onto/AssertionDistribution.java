package org.aimas.ami.cmm.agent.onto;


import jade.util.leap.*;

/**
* A set of AssertionAssignments that a OrgMgr sends to a CtxSensor or CtxUser that has announced its presence and capabilities.
* Protege name: AssertionDistribution
* @author OntologyBeanGenerator v4.1
* @version 2014/07/14, 15:42:54
*/
public interface AssertionDistribution extends jade.content.Concept {

   /**
   * The AssertionAssignments that a OrgMgr communicates to a CtxSensor or CtxUser agent following IsPresent and PublishAssertions messages sent by those agents.
   * Protege name: assignment
   */
   public void addAssignment(String elem);
   public boolean removeAssignment(String elem);
   public void clearAllAssignment();
   public Iterator getAllAssignment();
   public List getAssignment();
   public void setAssignment(List l);

}
