package org.aimas.ami.cmm.agent.orgmgr;

import jade.core.behaviours.SequentialBehaviour;
import jade.util.Event;

public class CMMKillBehaviour extends SequentialBehaviour {
	
    private static final long serialVersionUID = 6966623503376723180L;
	
    static final int INIT_SUCCESS = 	0;
    static final int INIT_FAILURE =    -1;
	
    private Event cmmKillEvent;
	
	public CMMKillBehaviour(OrgMgr orgMgr, Event killEvent) {
		super(orgMgr);
		
		this.cmmKillEvent = killEvent;
	}
	
}
