package org.aimas.ami.cmm;

import jade.util.Event;

public interface CMMPlatformRequestExecutor {
	public Event createCMMInstance();
	
	public Event startCMMInstance();
	
	public Event stopCMMInstance();
	
	public Event killCMMInstance();
}
