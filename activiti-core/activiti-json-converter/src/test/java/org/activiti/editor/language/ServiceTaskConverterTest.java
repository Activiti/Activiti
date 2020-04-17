package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.ServiceTask;
import org.junit.jupiter.api.Test;

public class ServiceTaskConverterTest extends AbstractConverterTest {

  @Test
  public void convertJsonToModel() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    validateModel(bpmnModel);
  }

  @Test
  public void doubleConversionValidation() throws Exception {
    BpmnModel bpmnModel = readJsonFile();
    bpmnModel = convertToJsonAndBack(bpmnModel);
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "test.servicetaskmodel.json";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("servicetask", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ServiceTask.class);
    assertThat(flowElement.getId()).isEqualTo("servicetask");
    ServiceTask serviceTask = (ServiceTask) flowElement;
    assertThat(serviceTask.getId()).isEqualTo("servicetask");
    assertThat(serviceTask.getName()).isEqualTo("Service task");

    List<FieldExtension> fields = serviceTask.getFieldExtensions();
    assertThat(fields).hasSize(2);
    FieldExtension field = (FieldExtension) fields.get(0);
    assertThat(field.getFieldName()).isEqualTo("testField");
    assertThat(field.getStringValue()).isEqualTo("test");
    field = (FieldExtension) fields.get(1);
    assertThat(field.getFieldName()).isEqualTo("testField2");
    assertThat(field.getExpression()).isEqualTo("${test}");

    List<ActivitiListener> listeners = serviceTask.getExecutionListeners();
    assertThat(listeners).hasSize(3);
    ActivitiListener listener = (ActivitiListener) listeners.get(0);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("org.test.TestClass");
    assertThat(listener.getEvent()).isEqualTo("start");
    listener = (ActivitiListener) listeners.get(1);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${testExpression}");
    assertThat(listener.getEvent()).isEqualTo("end");
    listener = (ActivitiListener) listeners.get(2);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${delegateExpression}");
    assertThat(listener.getEvent()).isEqualTo("start");
  }
}
