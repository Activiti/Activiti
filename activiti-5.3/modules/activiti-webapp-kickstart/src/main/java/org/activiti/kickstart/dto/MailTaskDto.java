package org.activiti.kickstart.dto;

import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.activity.type.ServiceTask;
import org.activiti.kickstart.bpmn20.model.extension.ExtensionElements;
import org.activiti.kickstart.bpmn20.model.extension.activiti.ActivitFieldExtensionElement;

public class MailTaskDto extends BaseTaskDto {

  public static class Field {

    public Field(String name) {
      this.name = name;
    }

    private String name;

    public String getName() {
      return name;
    }

    private String stringValue;

    public String getStringValue() {
      return stringValue;
    }
    public void setStringValue(String stringValue) {
      this.stringValue = stringValue;
    }

    private String expression;

    public String getExpression() {
      return expression;
    }
    public void setExpression(String expression) {
      this.expression = expression;
    }
  }

  private Field to = new Field("to");
  private Field from = new Field("from");
  private Field cc = new Field("cc");
  private Field subject = new Field("subject");
  private Field bcc = new Field("bcc");
  private Field html = new Field("html");
  private Field text = new Field("text");

  public Field getTo() {
    return to;
  }
  public Field getFrom() {
    return from;
  }
  public Field getCc() {
    return cc;
  }
  public Field getSubject() {
    return subject;
  }
  public Field getBcc() {
    return bcc;
  }
  public Field getHtml() {
    return html;
  }
  public Field getText() {
    return text;
  }

  @Override
  public FlowElement createFlowElement() {

    ServiceTask serviceTask = new ServiceTask();
    serviceTask.setType("mail");
    ExtensionElements extensionElements = new ExtensionElements();

    addIfFilled(extensionElements, getTo());
    addIfFilled(extensionElements, getFrom());
    addIfFilled(extensionElements, getSubject());
    addIfFilled(extensionElements, getCc());
    addIfFilled(extensionElements, getBcc());
    addIfFilled(extensionElements, getHtml());
    addIfFilled(extensionElements, getText());

    serviceTask.setExtensionElements(extensionElements);
    return serviceTask;
  }

  private void addIfFilled(ExtensionElements extenstionElements, Field fieldToAdd) {
    ActivitFieldExtensionElement element = prepareExtensionElement(fieldToAdd);
    if (element != null) {
      extenstionElements.add(element);
    }
  }

  private ActivitFieldExtensionElement prepareExtensionElement(Field field) {
    if (field.getStringValue() == null && field.getExpression() == null) {
      return null;
    }

    ActivitFieldExtensionElement extensionElement = new ActivitFieldExtensionElement();
    extensionElement.setName(field.getName());
    extensionElement.setStringValue(field.getStringValue());
    extensionElement.setExpression(field.getExpression());
    return extensionElement;
  }
}
