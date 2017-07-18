package org.activiti.bpmn.model.parse;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.GraphicInfo;

public class Problem {

  protected String errorMessage;
  protected String resource;
  protected int line;
  protected int column;

  public Problem(String errorMessage, String localName, int lineNumber, int columnNumber) {
    this.errorMessage = errorMessage;
    this.resource = localName;
    this.line = lineNumber;
    this.column = columnNumber;
  }

  public Problem(String errorMessage, BaseElement element) {
    this.errorMessage = errorMessage;
    this.resource = element.getId();
    this.line = element.getXmlRowNumber();
    this.column = element.getXmlColumnNumber();
  }

  public Problem(String errorMessage, GraphicInfo graphicInfo) {
    this.errorMessage = errorMessage;
    this.line = graphicInfo.getXmlRowNumber();
    this.column = graphicInfo.getXmlColumnNumber();
  }

  public String toString() {
    return errorMessage + (resource != null ? " | " + resource : "") + " | line " + line + " | column " + column;
  }
}
