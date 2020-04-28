package org.activiti.editor.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.junit.jupiter.api.Test;

public class NotExecutablePoolConverterTest extends AbstractConverterTest {

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
    return "test.notexecutablepoolmodel.json";
  }

  private void validateModel(BpmnModel model) {

    String idPool = "idPool";
    String idProcess = "poolProcess";

    assertThat(model.getPools()).hasSize(1);

    Pool pool = model.getPool(idPool);
    assertThat(pool.getId()).isEqualTo(idPool);
    assertThat(pool.getProcessRef()).isEqualTo(idProcess);
    assertThat(pool.isExecutable()).isFalse();

    Process process = model.getProcess(idPool);
    assertThat(process.getId()).isEqualTo(idProcess);
    assertThat(process.isExecutable()).isFalse();
    assertThat(process.getLanes()).hasSize(3);

  }
}
