package org.aimas.ami.cmm.agent.sensor;

import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

import org.aimas.ami.cmm.agent.onto.ExecTask;

public class CommandHandlerBehaviour extends SimpleAchieveREResponder {
    private static final long serialVersionUID = 2291039095257411838L;
    
	public CommandHandlerBehaviour(CtxSensor sensorAgent, MessageTemplate mt) {
	    super(sensorAgent, mt);
    }
	
	@Override
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		// The CtxSensor normally agrees to handle every tasking command and informs of either
		// failure or success in doing so. However, we implement this method just to mark the fact
		// that in a future version, the CtxSensor may directly refuse a given TASKING Command
		
		// One thing we will do though is check the request message to see that it is a valid
		// CMM agent-lang message
		
		try {
	        myAgent.getContentManager().extractContent(request);
        }
        catch (UngroundedException e) {
        	e.printStackTrace();
        	throw new NotUnderstoodException("Invalid grounding in CMMAgentLang message");
        }
        catch (CodecException e) {
	        e.printStackTrace();
	        throw new NotUnderstoodException("Parsing error in CMMAgentLang message");
        }
        catch (OntologyException e) {
	        e.printStackTrace();
	        throw new NotUnderstoodException("Invalid content in CMMAgentLang message");
        }
		
		return null;
	}
	
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) 
			throws FailureException {
		
		ContentElement ce = null;
		try {
	        ce = myAgent.getContentManager().extractContent(request);
		}
        catch (Exception e) {
        	// we don't need to do anything here since we treated the possibility of failure earlier 
        }
        
		if (ce instanceof ExecTask) {
        	ExecTask taskingCommand = (ExecTask)ce;
        	CtxSensor sensorAgent = (CtxSensor)myAgent;
        	
        	String assertionResourceURI = taskingCommand.getAssertion().getAssertionType();
        	SensingManager sensingManager = sensorAgent.getSensingManager();
        	
        	boolean commandSuccess = sensingManager.getAssertionManager(assertionResourceURI).execTask(taskingCommand);
        	ACLMessage informResult = request.createReply();
        	
        	if (commandSuccess) {
        		informResult.setPerformative(ACLMessage.INFORM);
        		informResult.setContent(request.getContent());
        	}
        	else {
        		informResult.setPerformative(ACLMessage.FAILURE);
        		informResult.setContent(request.getContent());
        	}
        	
        	return informResult;
        }
        else {
        	throw new FailureException("Invalid content in TASKING COMMAND request.");
        }
       
	}
}
