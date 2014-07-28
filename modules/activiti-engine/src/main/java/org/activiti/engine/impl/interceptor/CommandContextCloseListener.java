package org.activiti.engine.impl.interceptor;

/**
 * A listener that can be used to be notified of the closure of a {@link CommandContext}.
 * 
 * @author Joram Barrez
 */
public interface CommandContextCloseListener {
	
	void closing(CommandContext commandContext);
	
	void closed(CommandContext commandContext);

}
