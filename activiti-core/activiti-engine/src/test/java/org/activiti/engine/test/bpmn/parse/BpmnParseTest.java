/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.bpmn.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;

/**
 *
 */
public class BpmnParseTest extends PluggableActivitiTestCase {

  public void testInvalidProcessDefinition() {
    assertThatExceptionOfType(XMLException.class)
      .isThrownBy(() -> {
        String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidProcessDefinition");
        repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      });
  }

  public void testParseWithBpmnNamespacePrefix() {
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/parse/BpmnParseTest.testParseWithBpmnNamespacePrefix.bpmn20.xml").deploy();
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);

    repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
  }

  public void testParseWithMultipleDocumentation() {
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/parse/BpmnParseTest.testParseWithMultipleDocumentation.bpmn20.xml").deploy();
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);

    repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
  }

//  @Deployment
//  public void testParseDiagramInterchangeElements() {
//
//    // Graphical information is not yet exposed publicly, so we need to do some plumbing
//
//    BpmnModel bpmnModel = repositoryService.getBpmnModel(repositoryService.createProcessDefinitionQuery().singleResult().getId());
//    Process process = bpmnModel.getMainProcess();
//
//    // Check if diagram has been created based on Diagram Interchange when  it's not a headless instance
//    List<String> resourceNames = repositoryService.getDeploymentResourceNames(repositoryService.createProcessDefinitionQuery().singleResult().getDeploymentId());
//    assertThat(resourceNames).hasSize(2);
//
//    assertActivityBounds(bpmnModel, "theStart", 70, 255, 30, 30);
//    assertActivityBounds(bpmnModel, "task1", 176, 230, 100, 80);
//    assertActivityBounds(bpmnModel, "gateway1", 340, 250, 40, 40);
//    assertActivityBounds(bpmnModel, "task2", 445, 138, 100, 80);
//    assertActivityBounds(bpmnModel, "gateway2", 620, 250, 40, 40);
//    assertActivityBounds(bpmnModel, "task3", 453, 304, 100, 80);
//    assertActivityBounds(bpmnModel, "theEnd", 713, 256, 28, 28);
//
//    assertSequenceFlowWayPoints(bpmnModel, "flowStartToTask1", 100, 270, 176, 270);
//    assertSequenceFlowWayPoints(bpmnModel, "flowTask1ToGateway1", 276, 270, 340, 270);
//    assertSequenceFlowWayPoints(bpmnModel, "flowGateway1ToTask2", 360, 250, 360, 178, 445, 178);
//    assertSequenceFlowWayPoints(bpmnModel, "flowGateway1ToTask3", 360, 290, 360, 344, 453, 344);
//    assertSequenceFlowWayPoints(bpmnModel, "flowTask2ToGateway2", 545, 178, 640, 178, 640, 250);
//    assertSequenceFlowWayPoints(bpmnModel, "flowTask3ToGateway2", 553, 344, 640, 344, 640, 290);
//    assertSequenceFlowWayPoints(bpmnModel, "flowGateway2ToEnd", 660, 270, 713, 270);
//
//  }

  @Deployment
  public void testParseNamespaceInConditionExpressionType() {

    BpmnModel bpmnModel = repositoryService.getBpmnModel(repositoryService.createProcessDefinitionQuery().singleResult().getId());
    Process process = bpmnModel.getProcesses().get(0);
    assertThat(process).isNotNull();

    SequenceFlow sequenceFlow = (SequenceFlow) process.getFlowElement("SequenceFlow_3");
    assertThat(sequenceFlow.getConditionExpression()).isEqualTo("#{approved}");

    sequenceFlow = (SequenceFlow) process.getFlowElement("SequenceFlow_4");
    assertThat(sequenceFlow.getConditionExpression()).isEqualTo("#{!approved}");

  }

  @Deployment
  public void testParseDiagramInterchangeElementsForUnknownModelElements() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("TestAnnotation").singleResult();
    BpmnModel model = repositoryService.getBpmnModel(processDefinition.getId());
    Process mainProcess = model.getMainProcess();
    assertThat(mainProcess.getExtensionElements()).hasSize(0);
  }

  public void testParseSwitchedSourceAndTargetRefsForAssociations() {
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/parse/BpmnParseTest.testParseSwitchedSourceAndTargetRefsForAssociations.bpmn20.xml").deploy();

    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);

    repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
  }

  protected void assertActivityBounds(BpmnModel bpmnModel, String activityId, Integer x, Integer y, Integer width, Integer height) {
    assertThat(bpmnModel.getGraphicInfo(activityId).getX()).isEqualTo(x.doubleValue());
    assertThat(bpmnModel.getGraphicInfo(activityId).getY()).isEqualTo(y.doubleValue());
    assertThat(bpmnModel.getGraphicInfo(activityId).getWidth()).isEqualTo(width.doubleValue());
    assertThat(bpmnModel.getGraphicInfo(activityId).getHeight()).isEqualTo(height.doubleValue());
  }

  protected void assertSequenceFlowWayPoints(BpmnModel bpmnModel, String sequenceFlowId, Integer... waypoints) {
    List<GraphicInfo> graphicInfos = bpmnModel.getFlowLocationGraphicInfo(sequenceFlowId);
    assertThat(graphicInfos).hasSize(waypoints.length / 2);
    for (int i = 0; i < waypoints.length; i += 2) {
      Integer x = waypoints[i];
      Integer y = waypoints[i+1];
      assertThat(graphicInfos.get(i/2).getX()).isEqualTo(x.doubleValue());
      assertThat(graphicInfos.get(i/2).getY()).isEqualTo(y.doubleValue());
    }
  }

}
