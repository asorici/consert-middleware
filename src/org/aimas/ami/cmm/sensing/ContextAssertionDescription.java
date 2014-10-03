package org.aimas.ami.cmm.sensing;

import java.util.LinkedList;
import java.util.List;

public class ContextAssertionDescription {
	private String contextAssertionURI;
	private List<String> supportedAnnotationURIs;
	
	public ContextAssertionDescription(String contextAssertionURI) {
		this.contextAssertionURI = contextAssertionURI;
		this.supportedAnnotationURIs = new LinkedList<String>();
	}

	public String getContextAssertionURI() {
		return contextAssertionURI;
	}
	
	public void addSupportedAnnotationURI(String annotationURI) {
		supportedAnnotationURIs.add(annotationURI);
	}
	
	public void setSupportedAnnotationURIs(List<String> supportedAnnotationURIs) {
		this.supportedAnnotationURIs = supportedAnnotationURIs;
	}

	public List<String> getSupportedAnnotationURIs() {
		return supportedAnnotationURIs;
	}
}
