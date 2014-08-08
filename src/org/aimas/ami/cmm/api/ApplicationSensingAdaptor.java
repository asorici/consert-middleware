package org.aimas.ami.cmm.api;

import com.hp.hpl.jena.update.UpdateRequest;

public interface ApplicationSensingAdaptor {
	public void deliverUpdate(ContextAssertionDescription assertionDesc, UpdateRequest update);
}
