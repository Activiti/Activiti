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
public enum ContentType {

  PNG("image/png"),
  GIF("image/gif"), 
  JPEG("image/jpeg") /* or use "image/jpeg;charset=ISO-8859-1" ? */, 
  XML("application/xml"), 
  HTML("text/html"), 
  TEXT("text/plain"), 
  PDF("application/pdf"), 
  JSON("application/json;charset=UTF-8"), 
  MS_WORD("application/msword"), 
  MS_POWERPOINT("application/powerpoint"), 
  MS_EXCEL("application/excel"), 
  JAVASCRIPT("application/javascript"),
  //  
  // // TODO: Hmm?
  BINARY("unknown");

  private final String name;

  private ContentType(String description) {
    this.name = description;
  }

  public String getName() {
    return this.name;
  }

}