package org.aimas.ami.cmm;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aimas.ami.cmm.api.CMMInstanceStateOpCallback;
import org.aimas.ami.cmm.api.CMMOperationFuture;

public class CMMInstanceStateOpWrapper extends FutureTask<Void> implements CMMOperationFuture<Void> {
	
	private CMMInstanceStateOp wrappedOperation;
	private CMMInstanceStateOpCallback opCallback;
	
	public CMMInstanceStateOpWrapper(CMMInstanceStateOp wrappedOperation, CMMInstanceStateOpCallback opCallback) {
	    super(wrappedOperation);
	    
	    this.wrappedOperation = wrappedOperation;
	    this.opCallback = opCallback;
    }
	
	public CMMInstanceStateOpWrapper(CMMInstanceStateOp wrappedOperation) {
	    this(wrappedOperation, null);
    }
	
	public boolean opposes(CMMInstanceStateOpWrapper wrappedOp) {
		return wrappedOperation.opposes(wrappedOp.wrappedOperation);
	}
	
	
	@Override
    public boolean cancelOperation() {
	    return cancel(false);
    }


	@Override
    public Void awaitOperation() throws InterruptedException, ExecutionException {
	    return get();
    }


	@Override
    public Void awaitOperation(long timeout, TimeUnit unit) throws InterruptedException, 
    	ExecutionException, TimeoutException {
		
		return get(timeout, unit);
    }
	
	
	@Override
    public boolean isOperationCancelled() {
	    return isCancelled();
    }

	@Override
    public boolean isOperationDone() {
	    return isDone();
    }
	
	
	@Override
	protected void done() {
		if (opCallback != null) {
			try {
				// We call get() to see if there were any exceptions thrown during execution
				get();
	            
				// If no errors, then we signal completion
	            opCallback.operationCompleted(wrappedOperation.getContextDomainInfo().getApplicationId(), 
						wrappedOperation.getContextDomainInfo().getContextDimensionURI(),
						wrappedOperation.getContextDomainInfo().getContextDomainValueURI());
            }
            catch (Exception error) {
	            // Otherwise we signal the error
            	opCallback.operationFailed(wrappedOperation.getContextDomainInfo().getApplicationId(), 
						wrappedOperation.getContextDomainInfo().getContextDimensionURI(),
						wrappedOperation.getContextDomainInfo().getContextDomainValueURI(), error);
            }
		}
	}
	
	
}
