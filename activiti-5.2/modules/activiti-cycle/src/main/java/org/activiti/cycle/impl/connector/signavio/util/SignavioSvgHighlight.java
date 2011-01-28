package org.activiti.cycle.impl.connector.signavio.util;

public class SignavioSvgHighlight {

  private final SignavioSvgNodeType nodeType;

  private final SignavioSvgHighlightType highlightType;

  private final String nodeId;

  private final String message;
  
  public SignavioSvgHighlight(SignavioSvgNodeType nodeType, SignavioSvgHighlightType highlightType, String nodeId, String message) {
    this.nodeType = nodeType;
    this.highlightType = highlightType;
    this.nodeId = nodeId;
    this.message = message;
  }

  public SignavioSvgNodeType getNodeType() {
    return nodeType;
  }

  public SignavioSvgHighlightType getHighlightType() {
    return highlightType;
  }

  public String getNodeId() {
    return nodeId;
  }

  public String getMessage() {
    return message;
  }

}
