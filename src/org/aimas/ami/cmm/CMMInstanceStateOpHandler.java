package org.aimas.ami.cmm;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aimas.ami.cmm.api.CMMInstanceStateOpCallback;
import org.aimas.ami.cmm.api.CMMOperationFuture;

public class CMMInstanceStateOpHandler {
	
	private ExecutorService operationHandler;
	
	private CMMInstanceStateOpWrapper currentOperation;
	private LinkedList<CMMInstanceStateOpWrapper> operationBuffer;
	
	private Object opSyncObj = new Object();
	private boolean active = true;
	
	public CMMInstanceStateOpHandler() {
		operationHandler = Executors.newSingleThreadExecutor();
		operationBuffer = new LinkedList<CMMInstanceStateOpWrapper>();
	}
	
	public void start() {
		operationHandler = Executors.newSingleThreadExecutor();
		operationHandler.execute(new OpExecTask());
	}
	
	public void close() {
		synchronized(opSyncObj) {
			active = false;
			opSyncObj.notify();
		}
		
		operationHandler.shutdown();
		operationHandler = null;
	}
	
	public CMMOperationFuture<Void> execOperation(CMMInstanceStateOp operation) {
		return execOperation(operation, null);
	}
	
	public CMMOperationFuture<Void> execOperation(CMMInstanceStateOp operation, 
			CMMInstanceStateOpCallback operationCallback) {
		
		CMMInstanceStateOpWrapper opWrapper = new CMMInstanceStateOpWrapper(operation, operationCallback);
		
		synchronized(opSyncObj) {
			operationBuffer.add(opWrapper);
			opSyncObj.notify();
		}
		
		return opWrapper;
	}
	
	private class OpExecTask implements Runnable {
		
		@Override
        public void run() {
	        synchronized(opSyncObj) {
	        	if (active) {
		        	// wait until there is smth in the buffer
		        	while(operationBuffer.isEmpty()) {
		        		try {
		                    opSyncObj.wait();
		                    if (!active) {
		                    	return;
		                    }
	                    }
	                    catch (InterruptedException e) {}
		        	}
		        	
		        	// we have something in the buffer
		        	currentOperation = operationBuffer.poll();
		        	currentOperation.run();
	        	}
	        }
        }
		
	}
}
