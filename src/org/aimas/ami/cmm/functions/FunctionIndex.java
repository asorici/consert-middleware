package org.aimas.ami.cmm.functions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aimas.ami.cmm.vocabulary.CoordConf;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;

public class FunctionIndex {
	private static Map<String, Class<?>> customFunctions = new HashMap<String, Class<?>>();
	static {
		// register now function
		customFunctions.put(CoordConf.NS + "compareTime", compareTime.class) ;		
	}
	
	public static Class<?> getFunctionClass(String operatorURI) {
		return customFunctions.get(operatorURI);
	}
	
	
	public static List<Class<?>> listRegisteredFunctions() {
		return new LinkedList<Class<?>>(customFunctions.values());
	}
	
	public static Map<String, Class<?>> getFunctions() {
		return customFunctions;
	}
	
	/**
	 * Add a function to this index. Do not yet perform registration of the function.
	 * @param functionURI Function URI
	 * @param functionClass	Class of the custom Java implementation of this function
	 */
	public static void addFunction(String functionURI, Class<?> functionClass) {
		addFunction(functionURI, functionClass, false);
	}
	
	
	/**
	 * Add a function to this index. If <code>register</code> is {@literal true} perform registration of the function.
	 * @param functionURI Function URI
	 * @param functionClass Class of the custom Java implementation of this function
	 * @param register Boolean flag telling whether to directly register the function with the {@link FunctionRegister} of the Jena API
	 */
	public static void addFunction(String functionURI, Class<?> functionClass, boolean register) {
		customFunctions.put(functionURI, functionClass);
		
		if (register) {
			FunctionRegistry.get().put(functionURI, functionClass) ;
		}
	}
	
	/**
	 * Register all custom function definitions from the specified CMM configuration model.
	 * These are defined as instances of spin:Function. Their implementation is given either as a SPARQL query, 
	 * either as a custom Java class. The functions implemented as SPARQL queries are registered directly 
	 * with the {@link SPINModuleRegistry}, while the ones having a Java implementation are registered with the 
	 * {@link FunctionRegistry} of the Jena API.
	 * @param configModel The ontology model containing the Function definitions
	 */
	public static void registerCustomFunctions(OntModel configModel) {
		// register SPIN system functions and templates 
		SPINModuleRegistry.get().init();
		
		// register SPIN custom functions and templates which the Context Model Function module defines
		SPINModuleRegistry.get().registerAll(configModel, null);
		
		// make a Jena registration of the Java classes that implement custom filter functions defined in the Context Model Function module
		registerCustomFilterFunctions();
	}
	
	private static void registerCustomFilterFunctions() {
		for (String functionURI : customFunctions.keySet()) {
			FunctionRegistry.get().put(functionURI, customFunctions.get(functionURI)) ;
		}
	}
}
