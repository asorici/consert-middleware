package org.aimas.ami.cmm.exceptions;

import com.hp.hpl.jena.rdf.model.Resource;

public class DomainTypeNotFoundException extends Exception {
	
    private static final long serialVersionUID = 1L;

	public DomainTypeNotFoundException(Resource domainTypeResource) {
		super("Domain value with URI " + domainTypeResource + " not found in ContextDomain hierarchy");
	}
	
}
