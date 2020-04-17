package org.activiti.editor.language.xml;

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
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  @Test
  public void convertModelToXML() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    BpmnModel parsedModel = exportAndReadXMLFile(bpmnModel);
    validateModel(parsedModel);
    deployProcess(parsedModel);
  }

  protected String getResource() {
    return "servicetaskmodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("servicetask");
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(ServiceTask.class);
    assertThat(flowElement.getId()).isEqualTo("servicetask");
    ServiceTask serviceTask = (ServiceTask) flowElement;
    assertThat(serviceTask.getId()).isEqualTo("servicetask");
    assertThat(serviceTask.getName()).isEqualTo("Service task");

    List<FieldExtension> fields = serviceTask.getFieldExtensions();
    assertThat(fields).hasSize(2);
    FieldExtension field = fields.get(0);
    assertThat(field.getFieldName()).isEqualTo("testField");
    assertThat(field.getStringValue()).isEqualTo("test");
    field = fields.get(1);
    assertThat(field.getFieldName()).isEqualTo("testField2");
    assertThat(field.getExpression()).isEqualTo("${test}");

    List<ActivitiListener> listeners = serviceTask.getExecutionListeners();
    assertThat(listeners).hasSize(3);
    ActivitiListener listener = listeners.get(0);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("org.test.TestClass");
    assertThat(listener.getEvent()).isEqualTo("start");
    listener = listeners.get(1);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${testExpression}");
    assertThat(listener.getEvent()).isEqualTo("end");
    listener = listeners.get(2);
    assertThat(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType())).isTrue();
    assertThat(listener.getImplementation()).isEqualTo("${delegateExpression}");
    assertThat(listener.getEvent()).isEqualTo("start");

    assertThat(serviceTask.getFailedJobRetryTimeCycleValue()).isEqualTo("R5/PT5M");
  }
}
