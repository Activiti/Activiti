package org.activiti.camel.util;

/**
 * Used in the gateway's condition. 
 * 
 * @author stefan.schulze@accelsis.biz
 *
 */
public enum Routing {

	/**
	 * Process should terminate normally.
	 */
	DEFAULT,
	
	/**
	 * Camel route should throw an exception and propagate it back to the caller.
	 */
	PROVOKE_ERROR,
	
	/**
	 * Camel route should throw an exception and handle it.
	 */
	HANDLE_ERROR

}
