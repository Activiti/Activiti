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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.test.Deployment;


/**
 * 
 * @author Joram Barrez
 */
public class BpmnParseTest extends PluggableActivitiTestCase {
  
  public void testInvalidProcessDefinition() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testInvalidProcessDefinition");
      repositoryService.createDeployment().name(resource).addClasspathResource(resource).deploy();
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("Attribute 'invalidAttribute' is not allowed to appear in element 'process'", e.getMessage());
    }
  }
  
  public void testParseWithBpmnNamespacePrefix() {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/parse/BpmnParseTest.testParseWithBpmnNamespacePrefix.bpmn20.xml")
        .deploy();
      assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
      
      repositoryService.deleteDeploymentCascade(repositoryService.createDeploymentQuery().singleResult().getId());
  }
  
  @Deployment
  public void testParseDiagramInterchangeElements() {
    
    // Graphical information is not yet exposed publicly, so we need to do some plumbing
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    ProcessDefinitionEntity processDefinitionEntity = commandExecutor.execute(new Command<ProcessDefinitionEntity>() {
      public ProcessDefinitionEntity execute(CommandContext commandContext) {
        return commandContext.getRepositorySession().findDeployedLatestProcessDefinitionByKey("myProcess");
      }
    });
    
    assertNotNull(processDefinitionEntity);
    assertEquals(7, processDefinitionEntity.getActivities().size());
    
    for (ActivityImpl activity : processDefinitionEntity.getActivities()) {
      
      if (activity.getId().equals("theStart")) {
        assertActivityBounds(activity, 70, 255, 30, 30);
      } else if (activity.getId().equals("task1")) {
        assertActivityBounds(activity, 176, 230, 100, 80);
      } else if (activity.getId().equals("gateway1")) {
        assertActivityBounds(activity, 340, 250, 40, 40);
      } else if (activity.getId().equals("task2")) {
        assertActivityBounds(activity, 445, 138, 100, 80);
      } else if (activity.getId().equals("gateway2")) {
        assertActivityBounds(activity, 620, 250, 40, 40);
      } else if (activity.getId().equals("task3")) {
        assertActivityBounds(activity, 453, 304, 100, 80);
      } else if (activity.getId().equals("theEnd")) {
        assertActivityBounds(activity, 713, 256, 28, 28);
      } 
      
      for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
        assertTrue( ((TransitionImpl)sequenceFlow).getWaypoints().size() >= 4);
        
        TransitionImpl transitionImpl = (TransitionImpl) sequenceFlow;
        if (transitionImpl.getId().equals("flowStartToTask1")) {
          assertSequenceFlowWayPoints(transitionImpl, 100, 270, 176, 270);
        } else  if (transitionImpl.getId().equals("flowTask1ToGateway1")) {
          assertSequenceFlowWayPoints(transitionImpl, 276, 270, 340,270);
        } else  if (transitionImpl.getId().equals("flowGateway1ToTask2")) {
          assertSequenceFlowWayPoints(transitionImpl, 360, 250, 360, 178, 445, 178);
        } else  if (transitionImpl.getId().equals("flowGateway1ToTask3")) {
          assertSequenceFlowWayPoints(transitionImpl, 360, 290, 360, 344, 453, 344);
        } else  if (transitionImpl.getId().equals("flowTask2ToGateway2")) {
          assertSequenceFlowWayPoints(transitionImpl, 545, 178, 640, 178, 640, 250);
        } else  if (transitionImpl.getId().equals("flowTask3ToGateway2")) {
          assertSequenceFlowWayPoints(transitionImpl, 553, 344, 640, 344, 640, 290);
        } else  if (transitionImpl.getId().equals("flowGateway2ToEnd")) {
          assertSequenceFlowWayPoints(transitionImpl, 660, 270, 713, 270);
        }
        
      }
    }
  }
  
  protected void assertActivityBounds(ActivityImpl activity, int x, int y, int width, int height) {
    assertEquals(x, activity.getX());
    assertEquals(y, activity.getY());
    assertEquals(width, activity.getWidth());
    assertEquals(height, activity.getHeight());
  }
  
  protected void assertSequenceFlowWayPoints(TransitionImpl sequenceFlow, Integer ... waypoints) {
    assertEquals(waypoints.length, sequenceFlow.getWaypoints().size());
    for (int i = 0; i < waypoints.length; i++) {
      assertEquals(waypoints[i], sequenceFlow.getWaypoints().get(i));
    }
  }

}
