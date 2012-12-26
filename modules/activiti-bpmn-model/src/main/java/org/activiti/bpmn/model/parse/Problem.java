package org.activiti.bpmn.model.parse;

import javax.xml.stream.XMLStreamReader;

public class Problem {

  protected String errorMessage;
  protected String resource;
  protected int line;
  protected int column;
  
  public Problem(String errorMessage, XMLStreamReader xtr) {
    this.errorMessage = errorMessage;
    this.resource = xtr.getLocalName();
    this.line = xtr.getLocation().getLineNumber();
    this.column = xtr.getLocation().getColumnNumber();
  }
  
  public Problem(String errorMessage, String elementId) {
    this.errorMessage = errorMessage;
    this.resource = elementId;
  }
  
  public String toString() {
    return errorMessage + (resource != null ? " | "+resource : "") + " | line " +line + " | column " + column;
  }
}
