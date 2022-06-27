/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
