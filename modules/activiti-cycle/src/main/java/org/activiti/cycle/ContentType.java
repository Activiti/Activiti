package org.activiti.cycle;

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
public class ContentType {

	public static final String PNG = "image/png";
  public static final String GIF = "image/gif";
  public static final String JPEG = "image/jpeg"; // or use "image/jpeg;charset=ISO-8859-1" ?
  public static final String XML = "application/xml";
	public static final String HTML = "text/html";
  public static final String TEXT = "text/plain";
  public static final String PDF = "application/pdf";
  public static final String JSON = "application/json;charset=UTF-8";
  public static final String MS_WORD = "application/msword";
  public static final String MS_POWERPOINT = "application/powerpoint";
  public static final String MS_EXCEL = "application/excel";

}
