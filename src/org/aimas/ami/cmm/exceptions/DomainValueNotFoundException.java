package org.aimas.ami.cmm.exceptions;

import com.hp.hpl.jena.rdf.model.Resource;

public class DomainValueNotFoundException extends Exception {
    private static final long serialVersionUID = -106765600056938870L;

	public DomainValueNotFoundException(Resource domainValueResource) {
		super("Domain value with URI " + domainValueResource + " not found in ContextDomain hierarchy");
	}
	
}
