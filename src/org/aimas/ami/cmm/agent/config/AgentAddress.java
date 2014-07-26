package org.aimas.ami.cmm.agent.config;

import jade.core.AID;

import org.aimas.ami.cmm.vocabulary.OrgConf;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;

public class AgentAddress {
	private static AID agentIdentifier;
	
	private String agentLocalName;
	private String agentMTPHost;
	private int agentMTPPort;
	
	private CMMAgentContainer agentContainer;
	
	public AgentAddress(String agentLocalName, String agentMTPHost, int agentMTPPort, CMMAgentContainer agentContainer) {
	    this.agentLocalName = agentLocalName;
	    this.agentMTPHost = agentMTPHost;
	    this.agentMTPPort = agentMTPPort;
    
	    this.agentContainer = agentContainer;
	}
	
	public String getLocalName() {
		return agentLocalName;
	}
	
	public String getMTPHost() {
		return agentMTPHost;
	}
	
	public int getMTPPort() {
		return agentMTPPort;
	}
	
	public CMMAgentContainer getAgentContainer() {
	    return agentContainer;
    }
	
	public AID getAID() {
		if (agentIdentifier == null) {
			// If we don't have an agent container, we assume the local name suffices 
			// to construct the correct global AID
			if (agentContainer == null) {
				AID aid = new AID(agentLocalName, false);
				aid.addAddresses(getMTPAddress());
				
				agentIdentifier = aid;
			}
			
			// Otherwise, use the platform name from the Container to construct the global AID
			String globalAgentName = agentLocalName + "@" + agentContainer.getPlatformName();
			AID aid = new AID(globalAgentName, false);
			aid.addAddresses(getMTPAddress());
			
			agentIdentifier = aid;
		}
		
		return agentIdentifier;
	}
	
	public String getMTPAddress() {
		return "http://" + agentMTPHost + ":" + agentMTPPort + "/acc";
	}
	
	
	public static AgentAddress fromConfigurationModel(OntModel cmmConfigurationModel, Resource agentAddressResource) {
		if (agentAddressResource == null)
			return null;
		
		String agentLocalName = agentAddressResource.getProperty(OrgConf.agentName).getString();
		String agentMTPHost = agentAddressResource.getProperty(OrgConf.agentMTPHost).getString();
		int agentMTPPort = agentAddressResource.getProperty(OrgConf.agentMTPPort).getInt();
		
		CMMAgentContainer agentContainer = CMMAgentContainer.fromConfigurationModel(cmmConfigurationModel, 
				agentAddressResource.getPropertyResourceValue(OrgConf.agentContainer));
		
		return new AgentAddress(agentLocalName, agentMTPHost, agentMTPPort, agentContainer);
	}

	@Override
    public int hashCode() {
	    return getAID().hashCode();
    }
	
	
	@Override
    public boolean equals(Object obj) {
	    if (this == obj) {
		    return true;
	    }
	    
	    if (obj == null) {
		    return false;
	    }
	    
	    if (!(obj instanceof AgentAddress)) {
		    return false;
	    }
	    
	    AgentAddress other = (AgentAddress) obj;
	    if (!getAID().equals(other.getAID())) {
		    return false;
	    }
	    
	    return true;
    }
}
