package org.aimas.ami.cmm;

public class ContextDomainWrapper {
	
	private String applicationId;
	private String contextDimensionURI;
	private String contextDomainValueURI;
	
	public ContextDomainWrapper(String contextDimensionURI, String contextDomainValueURI, String applicationId) {
        this.applicationId = applicationId;
		this.contextDimensionURI = contextDimensionURI;
        this.contextDomainValueURI = contextDomainValueURI;
    }

	public String getContextDimensionURI() {
		return contextDimensionURI;
	}

	public String getContextDomainValueURI() {
		return contextDomainValueURI;
	}
	
	public String getApplicationId() {
		return applicationId;
	}
	
	@Override
    public int hashCode() {
		String s = 	applicationId != null ? applicationId : "none" + "-" +
					contextDimensionURI != null ? contextDimensionURI : "none" + "-" +
					contextDomainValueURI != null ? contextDomainValueURI : "none";
		
		return s.hashCode(); 
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj) {
	        return true;
        }
        
        if (!(obj instanceof ContextDomainWrapper)) {
	        return false;
        }
        
        ContextDomainWrapper other = (ContextDomainWrapper) obj;
        if (applicationId == null) {
	        if (other.applicationId != null) {
		        return false;
	        }
        }
        else if (!applicationId.equals(other.applicationId)) {
	        return false;
        }
        
        if (contextDimensionURI == null) {
	        if (other.contextDimensionURI != null) {
		        return false;
	        }
        }
        else if (!contextDimensionURI.equals(other.contextDimensionURI)) {
	        return false;
        }
        
        if (contextDomainValueURI == null) {
	        if (other.contextDomainValueURI != null) {
		        return false;
	        }
        }
        else if (!contextDomainValueURI.equals(other.contextDomainValueURI)) {
	        return false;
        }
        
        return true;
    }
}
