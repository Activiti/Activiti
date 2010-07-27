package org.activiti.impl.cycle.connect.api;

/**
 * Constants for types of content that can be linked.
 * 
 * Do NOT use an enum to keep the types extensible without touching the core
 * code.
 * 
 * TODO: Add MimeTypes?
 * 
 * @author bernd.ruecker@camunda.com
 */
public class ContentRepresentationType {

	public static final String IMAGE = "img";
	public static final String XML = "xml";
	public static final String HTML = "html";
	public static final String JAVA = "java";
  public static final String TEXT = "txt";
	
}
