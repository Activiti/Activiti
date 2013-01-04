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
package org.activiti.workflow.simple.diagram;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramCanvas;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;

/**
 * @author Joram Barrez
 */
public class WorkflowDIGenerator {

  // Constants
  protected static final int SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH = 45;
  protected static final int ARROW_WIDTH = 5;
  protected static final int SEQUENCE_FLOW_WIDTH = SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH + ARROW_WIDTH;

  protected static final int LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH = SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH * 2;
  protected static final int LONG_SEQUENCE_FLOW_WIDTH = LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH + ARROW_WIDTH;

  protected static final int TASK_WIDTH = 130;
  protected static final int TASK_HEIGHT = 60;
  protected static final int TASK_HEIGHT_SPACING = 10;

  protected static final int EVENT_WIDTH = 20;

  protected static final int GATEWAY_WIDTH = 40;
  protected static final int GATEWAY_HEIGHT = 40;

  protected int TASK_BLOCK_WIDTH = GATEWAY_WIDTH + TASK_WIDTH + SEQUENCE_FLOW_WIDTH + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH;
  
  // Input
  protected WorkflowDefinition workflowDefinition;
  
  protected BpmnModel bpmnModel;
  protected Process process;

  // Will be set during image generation
  protected int startX;
  protected int startY;
  protected int currentWidth;
  protected ProcessDiagramCanvas processDiagramCanvas;
  protected List<BlockOfSteps> allStepBlocks;
  protected Map<String, List<SequenceFlow>> outgoingSequenceFlowMapping;
  protected Map<String, List<SequenceFlow>> incomingSequenceFlowMapping;
  protected Set<String> handledElements;

  public WorkflowDIGenerator(WorkflowDefinition workflowDefinition, BpmnModel bpmnModel) {
    this.workflowDefinition = workflowDefinition;
    this.bpmnModel = bpmnModel;
  }
  
  public void generateDI() {
    generateDI(false);
  }
  
  protected void generateDI(boolean generateImage) {
    
    // Reset any previous DI information
    bpmnModel.getLocationMap().clear();
    bpmnModel.getFlowLocationMap().clear();
    bpmnModel.getLocationMap().clear();
    
    process = bpmnModel.getProcesses().get(0); // will always contain just one
    
    // Create task blocks (used for image generation and canvas size calculation)
    generateTaskBlocks();
    
    // Calulcate image height and width
    this.startX = 0;
    this.startY = calculateMaximumHeight() / 2 + 10;
    this.currentWidth = 0;

    if (generateImage) {
      int width = calculateMaximumWidth() + 50;
      int height = calculateMaximumHeight() + 50;
      processDiagramCanvas = new ProcessDiagramCanvas(width, height);
    }

    Collection<FlowElement> flowElements = process.getFlowElements();
    generateSequenceflowMappings(flowElements);
    this.handledElements = new HashSet<String>();

    // Enough preparation, actually draw some stuff
    for (FlowElement flowElement : flowElements) {

      if (!handledElements.contains(flowElement.getId())) {

        if (flowElement instanceof StartEvent) {

          drawStartEvent(flowElement, startX, startY, EVENT_WIDTH, EVENT_WIDTH, generateImage);

        } else if (flowElement instanceof EndEvent) {

          drawSequenceFlow(incomingSequenceFlowMapping.get(flowElement.getId()).get(0), 
                  generateImage,
                  currentWidth, startY + EVENT_WIDTH / 2, currentWidth
                  + SEQUENCE_FLOW_WIDTH, startY + EVENT_WIDTH / 2);
          drawEndEvent(flowElement, currentWidth, startY, EVENT_WIDTH, EVENT_WIDTH, generateImage);

        } else if (flowElement instanceof ParallelGateway 
                && outgoingSequenceFlowMapping.get(flowElement.getId()).size() > 1) { // fork

          ParallelGateway parallelGateway = (ParallelGateway) flowElement;
          drawSequenceFlow(incomingSequenceFlowMapping.get(flowElement.getId()).get(0), 
                  generateImage,
                  currentWidth, startY + EVENT_WIDTH / 2, currentWidth
                  + SEQUENCE_FLOW_WIDTH, startY + EVENT_WIDTH / 2);
          drawParallelBlock(currentWidth, startY - EVENT_WIDTH / 2, parallelGateway, generateImage);

        } else if (flowElement instanceof Task) {
          drawSequenceFlow(incomingSequenceFlowMapping.get(flowElement.getId()).get(0), 
                  generateImage,
                  currentWidth, startY + EVENT_WIDTH / 2, currentWidth
                  + SEQUENCE_FLOW_WIDTH, startY + EVENT_WIDTH / 2);
          drawTask(flowElement, currentWidth, startY - ((TASK_HEIGHT - EVENT_WIDTH) / 2), 
                  TASK_WIDTH, TASK_HEIGHT, generateImage);
        }
      }
    }
  }
  
  public InputStream generateDiagram() {
    generateDI(true); // Generates DI and also the canvas which can export the image 
    return processDiagramCanvas.generateImage("png");
  }

  protected void generateTaskBlocks() {
    allStepBlocks = new ArrayList<BlockOfSteps>();

    List<StepDefinition> workflowSteps = workflowDefinition.getSteps();
    for (int i=0; i<workflowSteps.size(); i++) {
      StepDefinition stepDefinition = workflowSteps.get(i);
      
      // Parallel tasks are grouped in the same task block
      if (stepDefinition.isStartWithPrevious() && (i != 0)) {
        allStepBlocks.get(allStepBlocks.size() - 1).addStep(stepDefinition);
      } else {
        BlockOfSteps blockOfSteps = new BlockOfSteps();
        blockOfSteps.addStep(stepDefinition);
        allStepBlocks.add(blockOfSteps);
      }
    }
  }
  
  protected void generateSequenceflowMappings(Collection<FlowElement> flowElements) {
    this.outgoingSequenceFlowMapping = new HashMap<String, List<SequenceFlow>>();
    this.incomingSequenceFlowMapping = new HashMap<String, List<SequenceFlow>>();
    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof SequenceFlow) {
        SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
        String srcId = sequenceFlow.getSourceRef();
        String targetId = sequenceFlow.getTargetRef();

        if (outgoingSequenceFlowMapping.get(srcId) == null) {
          outgoingSequenceFlowMapping.put(srcId, new ArrayList<SequenceFlow>());
        }
        outgoingSequenceFlowMapping.get(srcId).add(sequenceFlow);

        if (incomingSequenceFlowMapping.get(targetId) == null) {
          incomingSequenceFlowMapping.put(targetId, new ArrayList<SequenceFlow>());
        }
        incomingSequenceFlowMapping.get(targetId).add(sequenceFlow);
      }
    }
  }

  protected int calculateMaximumWidth() {
    int width = 0;
    for (BlockOfSteps blockOfSteps : allStepBlocks) {
      if (blockOfSteps.getNrOfSteps() == 1) {
        width += TASK_WIDTH + SEQUENCE_FLOW_WIDTH;
      } else {
        width += TASK_BLOCK_WIDTH + SEQUENCE_FLOW_WIDTH;
      }
    }

    width += SEQUENCE_FLOW_WIDTH + 2 * EVENT_WIDTH;

    return width;
  }

  protected int calculateMaximumHeight() {
    int maxNrOfTasksInOneBlock = 0;
    for (BlockOfSteps blockOfSteps : allStepBlocks) {
      if (blockOfSteps.getNrOfSteps() > maxNrOfTasksInOneBlock) {
        maxNrOfTasksInOneBlock = blockOfSteps.getNrOfSteps();
      }
    }

    int extra = 0;
    if (maxNrOfTasksInOneBlock % 2 == 0) { // If there is an even nr of tasks -> evenly spread, but no task in the  middle
      extra = 2 * TASK_HEIGHT;
    }

    return (maxNrOfTasksInOneBlock * (TASK_HEIGHT + TASK_HEIGHT_SPACING)) + extra;
  }

  protected void drawParallelBlock(int x, int y, ParallelGateway parallelGateway, boolean generateImage) {

    int originalCurrentWidth = currentWidth;
    List<SequenceFlow> sequenceFlows = outgoingSequenceFlowMapping.get(parallelGateway.getId());
    int nrOfTasks = sequenceFlows.size();

    // First parallel gateway
    drawParallelGateway(parallelGateway, x, y, GATEWAY_WIDTH, GATEWAY_HEIGHT, generateImage);
    handledElements.add(parallelGateway.getId());

    // Sequence flow up and down
    int centerOfRhombus = x + GATEWAY_WIDTH / 2;
    int maxHeight = (nrOfTasks / 2) * (TASK_HEIGHT + TASK_HEIGHT_SPACING);

    int currentHeight = y - maxHeight;

    // first half
    for (int i = 0; i < nrOfTasks / 2; i++) {
      SequenceFlow sequenceFlow1 = sequenceFlows.get(i);
      drawSequenceFlow(sequenceFlow1, generateImage, centerOfRhombus, y, centerOfRhombus, currentHeight, 
              centerOfRhombus + SEQUENCE_FLOW_WIDTH, currentHeight);

      String targetFlowElementId = sequenceFlow1.getTargetRef();
      FlowElement userTask = process.getFlowElement(targetFlowElementId);
      drawTask(userTask, centerOfRhombus + SEQUENCE_FLOW_WIDTH, 
              currentHeight - ((TASK_HEIGHT + TASK_HEIGHT_SPACING) / 2), TASK_WIDTH, TASK_HEIGHT, generateImage);
      handledElements.add(userTask.getId());

      int seqFlowX = centerOfRhombus + SEQUENCE_FLOW_WIDTH + TASK_WIDTH;
      SequenceFlow sequenceFlow2 = outgoingSequenceFlowMapping.get(userTask.getId()).get(0);
      drawSequenceFlow(sequenceFlow2, generateImage, 
              seqFlowX, currentHeight, 
              seqFlowX + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH, currentHeight, 
              seqFlowX + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH, y);

      currentHeight += TASK_HEIGHT + TASK_HEIGHT_SPACING;
    }

    // middle task
    if (nrOfTasks % 2 != 0) {
      SequenceFlow sequenceFlow1 = sequenceFlows.get(nrOfTasks / 2);
      drawSequenceFlow(sequenceFlow1, generateImage,
              centerOfRhombus + GATEWAY_WIDTH / 2, 
              startY + EVENT_WIDTH / 2, centerOfRhombus + SEQUENCE_FLOW_WIDTH, 
              startY  + EVENT_WIDTH / 2);

      String targetFlowElementId = sequenceFlow1.getTargetRef();
      FlowElement userTask = process.getFlowElement(targetFlowElementId);
      drawTask(userTask, centerOfRhombus + SEQUENCE_FLOW_WIDTH, 
              startY - ((TASK_HEIGHT - GATEWAY_HEIGHT)), TASK_WIDTH, TASK_HEIGHT, generateImage);
      handledElements.add(userTask.getId());

      int seqflowX = centerOfRhombus + GATEWAY_WIDTH / 2 + (SEQUENCE_FLOW_WIDTH - GATEWAY_WIDTH / 2) + TASK_WIDTH;
      SequenceFlow sequenceFlow2 = outgoingSequenceFlowMapping.get(userTask.getId()).get(0);
      drawSequenceFlow(sequenceFlow2, generateImage,
              seqflowX, startY + EVENT_WIDTH / 2, 
              seqflowX + LONG_SEQUENCE_FLOW_WIDTH - GATEWAY_WIDTH / 2 - ARROW_WIDTH, startY
              + EVENT_WIDTH / 2);
    }

    currentHeight = y + GATEWAY_HEIGHT + TASK_HEIGHT + TASK_HEIGHT_SPACING;

    // second half
    int startIndex = nrOfTasks % 2 == 0 ? nrOfTasks / 2 : (nrOfTasks / 2) + 1;
    for (int i = startIndex; i < nrOfTasks; i++) {
      SequenceFlow sequenceFlow1 = sequenceFlows.get(i);
      drawSequenceFlow(sequenceFlow1, generateImage,
              centerOfRhombus, y + GATEWAY_HEIGHT, centerOfRhombus, 
              currentHeight, centerOfRhombus + SEQUENCE_FLOW_WIDTH, currentHeight);
      
      String targetFlowElementId = sequenceFlow1.getTargetRef();
      FlowElement userTask = process.getFlowElement(targetFlowElementId);
      drawTask(userTask, centerOfRhombus + SEQUENCE_FLOW_WIDTH, 
              currentHeight - ((TASK_HEIGHT + TASK_HEIGHT_SPACING) / 2), TASK_WIDTH,
              TASK_HEIGHT, generateImage);

      int seqFlowX = centerOfRhombus + SEQUENCE_FLOW_WIDTH + TASK_WIDTH;
      SequenceFlow sequenceFlow2 = outgoingSequenceFlowMapping.get(userTask.getId()).get(0);
      drawSequenceFlow(sequenceFlow2, generateImage,
              seqFlowX, currentHeight, 
              seqFlowX + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH, currentHeight, seqFlowX
              + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH, y + GATEWAY_HEIGHT);
      handledElements.add(userTask.getId());

      currentHeight += TASK_HEIGHT + TASK_HEIGHT_SPACING;
    }

    // Second parallel gateway
    String someTaskId = sequenceFlows.get(0).getTargetRef();
    FlowElement join = process.getFlowElement(outgoingSequenceFlowMapping.get(someTaskId).get(0).getTargetRef());
    centerOfRhombus = centerOfRhombus + SEQUENCE_FLOW_WIDTH + TASK_WIDTH + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH;
    drawParallelGateway(join, centerOfRhombus - GATEWAY_WIDTH / 2, y, GATEWAY_WIDTH, GATEWAY_HEIGHT, generateImage);
    handledElements.add(join.getId());

    currentWidth = originalCurrentWidth + TASK_BLOCK_WIDTH;
  }

  protected void drawStartEvent(FlowElement flowElement, int x, int y, int width, int height, boolean generateImage) {
    if (generateImage) {
      processDiagramCanvas.drawNoneStartEvent(x, y, width, height);
    }

    createDiagramInterchangeInformation(flowElement, x, y, width, height);
    currentWidth += EVENT_WIDTH;
  }

  protected void drawEndEvent(FlowElement flowElement, int x, int y, int width, int height, boolean generateImage) {
    if (generateImage) {
      processDiagramCanvas.drawNoneEndEvent(x, y, width, height);
    }

    createDiagramInterchangeInformation(flowElement, x, y, width, height);
    currentWidth += EVENT_WIDTH;
  }

  protected void drawParallelGateway(FlowElement flowElement, int x, int y, int width, int height, boolean generateImage) {
    if (generateImage) {
      processDiagramCanvas.drawParallelGateway(x, y, width, height);
    }

    createDiagramInterchangeInformation(flowElement, x, y, width, height);
    currentWidth += GATEWAY_WIDTH;
  }

  protected void drawTask(FlowElement flowElement, int x, int y, int width, int height, boolean generateImage) {
    if (generateImage) {
      if (flowElement instanceof UserTask) {
        processDiagramCanvas.drawUserTask(flowElement.getName(), x, y, width, height);
      } else if (flowElement instanceof ServiceTask) {
        processDiagramCanvas.drawServiceTask(flowElement.getName(), x, y, width, height);
      } else if (flowElement instanceof ScriptTask) {
        processDiagramCanvas.drawScriptTask(flowElement.getName(), x, y, width, height);
      }
    }
    
    createDiagramInterchangeInformation(flowElement, x, y, width, height);
    currentWidth += TASK_WIDTH;
  }

  protected void drawSequenceFlow(SequenceFlow sequenceFlow, boolean generateImage, int... waypoints) {

    // Draw on diagram canvas
    
    int minX = Integer.MAX_VALUE;
    int maxX = 0;
    for (int i = 2; i < waypoints.length; i += 2) { // waypoints.size()
      
      if (generateImage) {
        // minimally 4: x1, y1, x2, y2
        if (i < waypoints.length - 2) {
          processDiagramCanvas.drawSequenceflowWithoutArrow(waypoints[i - 2], 
                  waypoints[i - 1], waypoints[i], waypoints[i + 1], false);
        } else {
          processDiagramCanvas.drawSequenceflow(waypoints[i - 2], 
                  waypoints[i - 1], waypoints[i], waypoints[i + 1], false);
        }
      }

      if (waypoints[i - 2] < minX || waypoints[i] < minX) {
        minX = Math.min(waypoints[i - 2], waypoints[i]);
      }
      if (waypoints[i - 2] > maxX || waypoints[i] > maxX) {
        maxX = Math.max(waypoints[i - 2], waypoints[i]);
      }
    }

    currentWidth += maxX - minX;

    // Generate DI information
    List<GraphicInfo> graphicInfoForWaypoints = new ArrayList<GraphicInfo>();
    for (int i = 0; i < waypoints.length; i += 2) {
      GraphicInfo graphicInfo = new GraphicInfo();
      graphicInfo.element = sequenceFlow;
      graphicInfo.x = waypoints[i];
      graphicInfo.y = waypoints[i + 1];
      graphicInfoForWaypoints.add(graphicInfo);
    }
    bpmnModel.addFlowGraphicInfoList(sequenceFlow.getId(), graphicInfoForWaypoints);
    
  }

  protected void createDiagramInterchangeInformation(FlowElement flowElement,
          int x, int y, int width, int height) {
    GraphicInfo graphicInfo = new GraphicInfo();
    graphicInfo.x = x;
    graphicInfo.y = y;
    graphicInfo.width = width;
    graphicInfo.height = height;
    graphicInfo.element = flowElement;
    bpmnModel.addGraphicInfo(flowElement.getId(), graphicInfo);
  }
  
  // Helper class ------------------------------------------------------------------------

  static class BlockOfSteps {
    
    protected List<StepDefinition> steps;

    public BlockOfSteps() {
      this.steps = new ArrayList<StepDefinition>();
    }

    public List<StepDefinition> getSteps() {
      return steps;
    }

    public void addStep(StepDefinition step) {
      steps.add(step);
    }

    public StepDefinition get(int index) {
      return steps.get(index);
    }

    public int getNrOfSteps() {
      return steps.size();
    }
    
  }

}
