package org.aimas.ami.cmm.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface CMMOperationFuture<V> {
	
	boolean cancelOperation();
	
	V awaitOperation() throws InterruptedException, ExecutionException;
	
	V awaitOperation(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
	
	public boolean isOperationCancelled();

    public boolean isOperationDone();
}
