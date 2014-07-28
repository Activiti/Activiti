package org.activiti.editor.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.junit.Test;

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
    
    assertEquals(1, model.getPools().size());
    
    Pool pool = model.getPool(idPool);
    assertEquals(idPool, pool.getId());
    assertEquals(idProcess, pool.getProcessRef());
    assertFalse(pool.isExecutable());
    
    Process process = model.getProcess(idPool);
    assertEquals(idProcess, process.getId());
    assertFalse(process.isExecutable());
    assertEquals(3, process.getLanes().size());
    
  }
}
