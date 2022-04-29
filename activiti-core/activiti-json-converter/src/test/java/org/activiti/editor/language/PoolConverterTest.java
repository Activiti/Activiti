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
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.junit.jupiter.api.Test;

public class PoolConverterTest extends AbstractConverterTest {

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
    return "test.poolmodel.json";
  }

  private void validateModel(BpmnModel model) {

    String idPool = "idPool";
    String idProcess = "poolProcess";

    assertThat(model.getPools()).hasSize(1);

    Pool pool = model.getPool(idPool);
    assertThat(pool.getId()).isEqualTo(idPool);
    assertThat(pool.getProcessRef()).isEqualTo(idProcess);
    assertThat(pool.isExecutable()).isTrue();

    Process process = model.getProcess(idPool);
    assertThat(process.getId()).isEqualTo(idProcess);
    assertThat(process.isExecutable()).isTrue();
    assertThat(process.getLanes()).hasSize(3);

    Lane lane = process.getLanes().get(0);
    assertThat(lane.getId()).isEqualTo("idLane1");
    assertThat(lane.getName()).isEqualTo("Lane 1");
    assertThat(lane.getFlowReferences()).hasSize(7);
    assertThat(lane.getFlowReferences().contains("startevent")).isTrue();
    assertThat(lane.getFlowReferences().contains("usertask1")).isTrue();
    assertThat(lane.getFlowReferences().contains("usertask6")).isTrue();
    assertThat(lane.getFlowReferences().contains("endevent")).isTrue();

    lane = process.getLanes().get(1);
    assertThat(lane.getId()).isEqualTo("idLane2");
    assertThat(lane.getName()).isEqualTo("Lane 2");
    assertThat(lane.getFlowReferences()).hasSize(4);
    assertThat(lane.getFlowReferences().contains("usertask2")).isTrue();
    assertThat(lane.getFlowReferences().contains("usertask5")).isTrue();

    lane = process.getLanes().get(2);
    assertThat(lane.getId()).isEqualTo("idLane3");
    assertThat(lane.getName()).isEqualTo("Lane 3");
    assertThat(lane.getFlowReferences()).hasSize(4);
    assertThat(lane.getFlowReferences().contains("usertask3")).isTrue();
    assertThat(lane.getFlowReferences().contains("usertask4")).isTrue();

    assertThat(process.getFlowElement("startevent", true)).isNotNull();
    assertThat(process.getFlowElement("usertask1", true)).isNotNull();
    assertThat(process.getFlowElement("usertask2", true)).isNotNull();
    assertThat(process.getFlowElement("usertask3", true)).isNotNull();
    assertThat(process.getFlowElement("usertask4", true)).isNotNull();
    assertThat(process.getFlowElement("usertask5", true)).isNotNull();
    assertThat(process.getFlowElement("usertask6", true)).isNotNull();
    assertThat(process.getFlowElement("endevent", true)).isNotNull();

    assertThat(process.getFlowElement("flow1", true)).isNotNull();
    assertThat(process.getFlowElement("flow2", true)).isNotNull();
    assertThat(process.getFlowElement("flow3", true)).isNotNull();
    assertThat(process.getFlowElement("flow4", true)).isNotNull();
    assertThat(process.getFlowElement("flow5", true)).isNotNull();
    assertThat(process.getFlowElement("flow6", true)).isNotNull();
    assertThat(process.getFlowElement("flow7", true)).isNotNull();
  }
}
