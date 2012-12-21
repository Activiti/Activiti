package org.activiti.bpmn.model.parse;

import javax.xml.stream.XMLStreamReader;

public class Warning {

  protected String warningMessage;
  protected String resource;
  protected int line;
  protected int column;
  
  public Warning(String warningMessage, XMLStreamReader xtr) {
    this.warningMessage = warningMessage;
    this.resource = xtr.getLocalName();
    this.line = xtr.getLocation().getLineNumber();
    this.column = xtr.getLocation().getColumnNumber();
  }
  
  public Warning(String warningMessage, String elementId) {
    this.warningMessage = warningMessage;
    this.resource = elementId;
  }
  
  public String toString() {
    return warningMessage + (resource != null ? " | "+resource : "") + " | line " +line + " | column " + column;
  }
}
