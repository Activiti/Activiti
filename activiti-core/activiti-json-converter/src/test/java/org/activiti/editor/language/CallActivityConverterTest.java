package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IOParameter;
import org.junit.jupiter.api.Test;

public class CallActivityConverterTest extends AbstractConverterTest {

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
    return "test.callactivitymodel.json";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("callactivity", true);
    assertThat(flowElement).isNotNull();
    assertThat(flowElement).isInstanceOf(CallActivity.class);
    CallActivity callActivity = (CallActivity) flowElement;
    assertThat(callActivity.getId()).isEqualTo("callactivity");
    assertThat(callActivity.getName()).isEqualTo("Call activity");

    assertThat(callActivity.getCalledElement()).isEqualTo("processId");

    List<IOParameter> parameters = callActivity.getInParameters();
    assertThat(parameters).hasSize(2);
    IOParameter parameter = parameters.get(0);
    assertThat(parameter.getSource()).isEqualTo("test");
    assertThat(parameter.getTarget()).isEqualTo("test");
    parameter = parameters.get(1);
    assertThat(parameter.getSourceExpression()).isEqualTo("${test}");
    assertThat(parameter.getTarget()).isEqualTo("test");

    parameters = callActivity.getOutParameters();
    assertThat(parameters).hasSize(1);
    parameter = parameters.get(0);
    assertThat(parameter.getSource()).isEqualTo("test");
    assertThat(parameter.getTarget()).isEqualTo("test");
  }
}
