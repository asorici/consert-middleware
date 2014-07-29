package org.aimas.ami.cmm.api;

import org.aimas.ami.contextrep.engine.api.QueryResult;

import com.hp.hpl.jena.query.Query;

public interface QueryNotificationHandler {
	public void handleResultNotification(Query query, QueryResult result);
	
	public void handleRefuse(Query query);
}
