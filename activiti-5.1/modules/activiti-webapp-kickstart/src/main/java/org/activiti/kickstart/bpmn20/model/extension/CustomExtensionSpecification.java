package org.activiti.kickstart.bpmn20.model.extension;

/**
 * Sub types of this interface are used to identify a specific vendor and 
 * define the used namespace prefixes. This kind of configuration is required to
 * determine unnecessary namespace declaration used by vendor extensions. 
 * 
 * @author Sven Wagner-Boysen
 *
 */
public interface CustomExtensionSpecification {
	String[] namespacePrefixes = {};
}
