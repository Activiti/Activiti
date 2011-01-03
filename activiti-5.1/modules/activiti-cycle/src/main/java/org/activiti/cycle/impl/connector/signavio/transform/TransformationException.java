package org.activiti.cycle.impl.connector.signavio.transform;


/**
 * @author christian.lipphardt@camunda.com
 */
public class TransformationException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private static final String DEFAULT_RENDER_MSG = "An error occured during the transformation";
  
  private String renderMessage;
  private String renderContent;
  
  public TransformationException(String renderMessage, String renderContent, String message, Throwable t) {
    super(message, t);
    this.renderMessage = renderMessage;
  }
  
  public TransformationException(String renderMessage, String renderContent, Throwable t) {
    super(t);
    this.renderMessage = renderMessage;
  }

  public TransformationException(String renderMessage, String renderContent, String message) {
    super(message);
    this.renderMessage = renderMessage;
    this.renderContent = renderContent;
  }
  
  public TransformationException(String renderMessage, String renderContent) {
    super();
    this.renderMessage = renderMessage;
    this.renderContent = renderContent;
  }
  
  public void setRenderMessage(String renderMessage) {
    this.renderMessage = renderMessage;
  }

  public String getRenderMessage() {
    if (renderMessage == null) {
      renderMessage = DEFAULT_RENDER_MSG;
    }
    
    return renderMessage;
  }

  public String getRenderContent() {
    return renderContent;
  }

  public void setRenderContent(String renderContent) {
    this.renderContent = renderContent;
  }
}
