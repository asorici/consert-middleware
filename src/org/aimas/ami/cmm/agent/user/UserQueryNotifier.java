package org.aimas.ami.cmm.agent.user;

import jade.lang.acl.ACLMessage;

import org.aimas.ami.contextrep.engine.api.QueryResult;

public interface UserQueryNotifier {
	public void notifyQueryResult(ACLMessage queryMessage, QueryResult result);
	
	public void notifyRefuse(ACLMessage queryMessage, ACLMessage refusal);
}
