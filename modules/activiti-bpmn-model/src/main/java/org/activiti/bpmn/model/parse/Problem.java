package org.activiti.bpmn.model.parse;

import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.GraphicInfo;

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
  
  public Problem(String errorMessage, BaseElement element) {
    this.errorMessage = errorMessage;
    this.resource = element.getId();
    line = element.getXmlRowNumber();
    column = element.getXmlColumnNumber();
  }
  
  public Problem(String errorMessage, GraphicInfo graphicInfo) {
    this.errorMessage = errorMessage;
    line = graphicInfo.getXmlRowNumber();
    column = graphicInfo.getXmlColumnNumber();
  }
  
  public String toString() {
    return errorMessage + (resource != null ? " | "+resource : "") + " | line " +line + " | column " + column;
  }
}
