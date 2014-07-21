package org.aimas.ami.cmm.functions;

import java.util.Calendar;

import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

public class compareTime extends FunctionBase2 {
	
	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2) {
		if (!v1.isDateTime()) {
			throw new ExprEvalException("First argument not a datetime: " + v1) ;
		}
		
		if (!v2.isDateTime()) {
			throw new ExprEvalException("Second argument not a datetime: " + v1) ;
		}
		
		Calendar time1 = v1.getDateTime().toGregorianCalendar();
		Calendar time2 = v2.getDateTime().toGregorianCalendar();
		
		Calendar t1 = Calendar.getInstance();
		Calendar t2 = (Calendar)t1.clone();
		
		t1.set(Calendar.HOUR_OF_DAY, time1.get(Calendar.HOUR_OF_DAY));
		t1.set(Calendar.MINUTE, time1.get(Calendar.MINUTE));
		t1.set(Calendar.SECOND, time1.get(Calendar.SECOND));
		
		t2.set(Calendar.HOUR_OF_DAY, time2.get(Calendar.HOUR_OF_DAY));
		t2.set(Calendar.MINUTE, time2.get(Calendar.MINUTE));
		t2.set(Calendar.SECOND, time2.get(Calendar.SECOND));
		
		return NodeValue.makeInteger(t1.compareTo(t2));
	}
}
