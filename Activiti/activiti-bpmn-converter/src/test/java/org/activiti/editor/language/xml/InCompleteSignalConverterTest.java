package org.activiti.editor.language.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ProcessValidatorFactory;
import org.activiti.validation.ValidationError;
import org.junit.Test;

public class InCompleteSignalConverterTest extends AbstractConverterTest {

  @Test
  public void convertXMLToModel() throws Exception {
    BpmnModel bpmnModel = readXMLFile();
    validateModel(bpmnModel);
  }

  protected String getResource() {
    return "incompletesignalmodel.bpmn";
  }

  private void validateModel(BpmnModel model) {
    FlowElement flowElement = model.getMainProcess().getFlowElement("task");
    assertNotNull(flowElement);
    assertTrue(flowElement instanceof UserTask);
    assertEquals("task", flowElement.getId());

    ProcessValidator processValidator = new ProcessValidatorFactory().createDefaultProcessValidator();
    List<ValidationError> errors = processValidator.validate(model);
    assertNotNull(errors);
    assertEquals(2, errors.size());
  }
}
