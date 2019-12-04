package org.activiti.bpmn.model.parse;

import org.activiti.bpmn.model.BaseElement;

public class Warning {

  protected String warningMessage;
  protected String resource;
  protected int line;
  protected int column;

  public Warning(String warningMessage, String localName, int lineNumber, int columnNumber) {
    this.warningMessage = warningMessage;
    this.resource = localName;
    this.line = lineNumber;
    this.column = columnNumber;
  }

  public Warning(String warningMessage, BaseElement element) {
    this.warningMessage = warningMessage;
    this.resource = element.getId();
    line = element.getXmlRowNumber();
    column = element.getXmlColumnNumber();
  }

  public String toString() {
    return warningMessage + (resource != null ? " | " + resource : "") + " | line " + line + " | column " + column;
  }
}
