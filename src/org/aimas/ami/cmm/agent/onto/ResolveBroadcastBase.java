package org.aimas.ami.cmm.agent.onto;

import jade.core.AID;



/**
* Protege name: ResolveBroadcastBase
* @author OntologyBeanGenerator v4.1
* @version 2015/03/17, 13:57:23
*/
public interface ResolveBroadcastBase extends jade.content.AgentAction {
	/**
	* Indicates the Provisioning Agent (CtxUser or other CtxCoordinator) from which the broadcast was received.
	* Protege name: receivedFromAgent
   	*/
	public void setReceivedFromAgent(AID value);
	public AID getReceivedFromAgent();
	   
	/**
    * Protege name: broadcastUpperBound
    */
    public void setBroadcastUpperBound(String value);
    public String getBroadcastUpperBound();

    /**
    * Protege name: broadcastLowerBound
    */
    public void setBroadcastLowerBound(String value);
    public String getBroadcastLowerBound();
}
