package org.aimas.ami.cmm.agent.onto;



/**
* The base class for tasking commands that a CtxSensor agent can receive from a CtxCoord agent.
* Protege name: ExecTask
* @author OntologyBeanGenerator v4.1
* @version 2014/07/25, 19:43:09
*/
public interface ExecTask extends jade.content.AgentAction {

   /**
   * Indicates the ContextAssertion description for which this TaskingCommand is intended.
   * Protege name: assertion
   */
   public void setAssertion(AssertionDescription value);
   public AssertionDescription getAssertion();

}
