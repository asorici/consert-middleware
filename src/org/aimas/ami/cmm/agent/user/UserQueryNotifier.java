package org.aimas.ami.cmm.agent.user;

import org.aimas.ami.contextrep.engine.api.QueryResult;

import jade.lang.acl.ACLMessage;

public interface UserQueryNotifier {
	public void notifyQueryResult(ACLMessage queryMessage, QueryResult result);
	
	public void notifyRefuse(ACLMessage queryMessage, ACLMessage refusal);
}
