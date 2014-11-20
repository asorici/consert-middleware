package org.aimas.ami.cmm.agent.orgmgr;

import jade.util.Event;

public interface CMMPlatformInterface {
	public Event createCMMInstance();
	
	public Event startCMMInstance();
	
	public Event stopCMMInstance();
	
	public Event killCMMInstance();
}
