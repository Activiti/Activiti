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
package org.activiti.kickstart.diagram;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.bpmn.deployer.ProcessDiagramCanvas;
import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.Definitions;
import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.Process;
import org.activiti.kickstart.bpmn20.model.activity.type.UserTask;
import org.activiti.kickstart.bpmn20.model.bpmndi.BPMNEdge;
import org.activiti.kickstart.bpmn20.model.bpmndi.BPMNPlane;
import org.activiti.kickstart.bpmn20.model.bpmndi.BPMNShape;
import org.activiti.kickstart.bpmn20.model.bpmndi.dc.Bounds;
import org.activiti.kickstart.bpmn20.model.bpmndi.dc.Point;
import org.activiti.kickstart.bpmn20.model.connector.SequenceFlow;
import org.activiti.kickstart.bpmn20.model.event.EndEvent;
import org.activiti.kickstart.bpmn20.model.event.StartEvent;
import org.activiti.kickstart.bpmn20.model.gateway.ParallelGateway;
import org.activiti.kickstart.dto.AdhocWorkflowDto;
import org.activiti.kickstart.dto.TaskBlock;

/**
 * @author Joram Barrez
 */
public class ProcessDiagramGenerator {

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

  // Instance members
  protected AdhocWorkflowDto adhocWorkflow;

  // Will be set during image generation
  protected int startX;
  protected int startY;
  protected int currentWidth;
  protected ProcessDiagramCanvas processDiagramCanvas;
  protected BPMNPlane plane;
  protected Map<String, List<SequenceFlow>> outgoingSequenceFlowMapping;
  protected Map<String, List<SequenceFlow>> incomingSequenceFlowMapping;
  protected Set<String> handledElements;

  public ProcessDiagramGenerator(AdhocWorkflowDto adhocWorkflow) {
    this.adhocWorkflow = adhocWorkflow;
  }

  public InputStream execute() {

    this.startX = 0;
    this.startY = calculateMaximumHeight() / 2 + 10;
    this.currentWidth = 0;

    int width = calculateMaximumWidth() + 50;
    int height = calculateMaximumHeight() + 50;
    processDiagramCanvas = new ProcessDiagramCanvas(width, height);

    Definitions definitions = adhocWorkflow.toBpmn20Xml();
    Process process = getProcess(definitions);
    this.plane = getPlane(definitions);

    List<FlowElement> flowElements = process.getFlowElement();
    generateSequenceflowMappings(flowElements);
    this.handledElements = new HashSet<String>();

    for (FlowElement flowElement : flowElements) {

      if (!handledElements.contains(flowElement.getId())) {

        if (flowElement instanceof StartEvent) {

          drawStartEvent(flowElement, startX, startY, EVENT_WIDTH, EVENT_WIDTH);

        } else if (flowElement instanceof EndEvent) {

          drawSequenceFlow(incomingSequenceFlowMapping.get(flowElement.getId()).get(0), 
                  currentWidth, startY + EVENT_WIDTH / 2, currentWidth
                  + SEQUENCE_FLOW_WIDTH, startY + EVENT_WIDTH / 2);
          drawEndEvent(flowElement, currentWidth, startY, EVENT_WIDTH, EVENT_WIDTH);

        } else if (flowElement instanceof ParallelGateway 
                && outgoingSequenceFlowMapping.get(flowElement.getId()).size() > 1) { // fork

          ParallelGateway parallelGateway = (ParallelGateway) flowElement;
          drawSequenceFlow(incomingSequenceFlowMapping.get(flowElement.getId()).get(0), 
                  currentWidth, startY + EVENT_WIDTH / 2, currentWidth
                  + SEQUENCE_FLOW_WIDTH, startY + EVENT_WIDTH / 2);
          drawParallelBlock(currentWidth, startY - EVENT_WIDTH / 2, parallelGateway);

        } else if (flowElement instanceof UserTask) {

          drawSequenceFlow(incomingSequenceFlowMapping.get(flowElement.getId()).get(0), 
                  currentWidth, startY + EVENT_WIDTH / 2, currentWidth
                  + SEQUENCE_FLOW_WIDTH, startY + EVENT_WIDTH / 2);
          drawUserTask(flowElement, currentWidth, startY - ((TASK_HEIGHT - EVENT_WIDTH) / 2), 
                  TASK_WIDTH, TASK_HEIGHT);

        }

      }

    }

    return processDiagramCanvas.generateImage("png");
  }

  protected Process getProcess(Definitions definitions) {
    for (BaseElement rootElement : definitions.getRootElement()) {
      if (rootElement instanceof Process) {
        return (Process) rootElement;
      }
    }
    return null;
  }

  protected BPMNPlane getPlane(Definitions definitions) {
    return definitions.getDiagram().get(0).getBPMNPlane();
  }

  protected void generateSequenceflowMappings(List<FlowElement> flowElements) {
    this.outgoingSequenceFlowMapping = new HashMap<String, List<SequenceFlow>>();
    this.incomingSequenceFlowMapping = new HashMap<String, List<SequenceFlow>>();
    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof SequenceFlow) {
        SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
        String srcId = sequenceFlow.getSourceRef().getId();
        String targetId = sequenceFlow.getTargetRef().getId();

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
    for (TaskBlock taskBlock : adhocWorkflow.getTaskBlocks()) {
      if (taskBlock.getNrOfTasks() == 1) {
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
    for (TaskBlock taskBlock : adhocWorkflow.getTaskBlocks()) {
      if (taskBlock.getNrOfTasks() > maxNrOfTasksInOneBlock) {
        maxNrOfTasksInOneBlock = taskBlock.getNrOfTasks();
      }
    }

    int extra = 0;
    if (maxNrOfTasksInOneBlock % 2 == 0) { // If there is an even nr of tasks -> evenly spread, but no task in the  middle
      extra = 2 * TASK_HEIGHT;
    }

    return (maxNrOfTasksInOneBlock * (TASK_HEIGHT + TASK_HEIGHT_SPACING)) + extra;
  }

  protected void drawParallelBlock(int x, int y, ParallelGateway parallelGateway) {

    int originalCurrentWidth = currentWidth;
    List<SequenceFlow> sequenceFlows = outgoingSequenceFlowMapping.get(parallelGateway.getId());
    int nrOfTasks = sequenceFlows.size();

    // First parallel gateway
    drawParallelGateway(parallelGateway, x, y, GATEWAY_WIDTH, GATEWAY_HEIGHT);
    handledElements.add(parallelGateway.getId());

    // Sequence flow up and down
    int centerOfRhombus = x + GATEWAY_WIDTH / 2;
    int maxHeight = (nrOfTasks / 2) * (TASK_HEIGHT + TASK_HEIGHT_SPACING);

    int currentHeight = y - maxHeight;

    // first half
    for (int i = 0; i < nrOfTasks / 2; i++) {
      SequenceFlow sequenceFlow1 = sequenceFlows.get(i);
      drawSequenceFlow(sequenceFlow1, centerOfRhombus, y, centerOfRhombus, currentHeight, 
              centerOfRhombus + SEQUENCE_FLOW_WIDTH, currentHeight);

      FlowElement userTask = sequenceFlow1.getTargetRef();
      drawUserTask(userTask, centerOfRhombus + SEQUENCE_FLOW_WIDTH, 
              currentHeight - ((TASK_HEIGHT + TASK_HEIGHT_SPACING) / 2), TASK_WIDTH, TASK_HEIGHT);
      handledElements.add(sequenceFlow1.getTargetRef().getId());

      int seqFlowX = centerOfRhombus + SEQUENCE_FLOW_WIDTH + TASK_WIDTH;
      SequenceFlow sequenceFlow2 = outgoingSequenceFlowMapping.get(userTask.getId()).get(0);
      drawSequenceFlow(sequenceFlow2, seqFlowX, currentHeight, 
              seqFlowX + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH, currentHeight, 
              seqFlowX + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH, y);

      currentHeight += TASK_HEIGHT + TASK_HEIGHT_SPACING;
    }

    // middle task
    if (nrOfTasks % 2 != 0) {
      SequenceFlow sequenceFlow1 = sequenceFlows.get(nrOfTasks / 2);
      drawSequenceFlow(sequenceFlow1, centerOfRhombus + GATEWAY_WIDTH / 2, 
              startY + EVENT_WIDTH / 2, centerOfRhombus + SEQUENCE_FLOW_WIDTH, 
              startY  + EVENT_WIDTH / 2);

      FlowElement userTask = sequenceFlow1.getTargetRef();
      drawUserTask(sequenceFlow1.getTargetRef(), centerOfRhombus + SEQUENCE_FLOW_WIDTH, 
              startY - ((TASK_HEIGHT - GATEWAY_HEIGHT)), TASK_WIDTH, TASK_HEIGHT);
      handledElements.add(sequenceFlow1.getTargetRef().getId());

      int seqflowX = centerOfRhombus + GATEWAY_WIDTH / 2 + (SEQUENCE_FLOW_WIDTH - GATEWAY_WIDTH / 2) + TASK_WIDTH;
      SequenceFlow sequenceFlow2 = outgoingSequenceFlowMapping.get(userTask.getId()).get(0);
      drawSequenceFlow(sequenceFlow2, seqflowX, startY + EVENT_WIDTH / 2, 
              seqflowX + LONG_SEQUENCE_FLOW_WIDTH - GATEWAY_WIDTH / 2 - ARROW_WIDTH, startY
              + EVENT_WIDTH / 2);
    }

    currentHeight = y + GATEWAY_HEIGHT + TASK_HEIGHT + TASK_HEIGHT_SPACING;

    // second half
    int startIndex = nrOfTasks % 2 == 0 ? nrOfTasks / 2 : (nrOfTasks / 2) + 1;
    for (int i = startIndex; i < nrOfTasks; i++) {
      SequenceFlow sequenceFlow1 = sequenceFlows.get(i);
      drawSequenceFlow(sequenceFlow1, centerOfRhombus, y + GATEWAY_HEIGHT, centerOfRhombus, 
              currentHeight, centerOfRhombus + SEQUENCE_FLOW_WIDTH, currentHeight);
      FlowElement userTask = sequenceFlow1.getTargetRef();
      drawUserTask(sequenceFlow1.getTargetRef(), centerOfRhombus + SEQUENCE_FLOW_WIDTH, 
              currentHeight - ((TASK_HEIGHT + TASK_HEIGHT_SPACING) / 2), TASK_WIDTH,
              TASK_HEIGHT);

      int seqFlowX = centerOfRhombus + SEQUENCE_FLOW_WIDTH + TASK_WIDTH;
      SequenceFlow sequenceFlow2 = outgoingSequenceFlowMapping.get(userTask.getId()).get(0);
      drawSequenceFlow(sequenceFlow2, seqFlowX, currentHeight, 
              seqFlowX + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH, currentHeight, seqFlowX
              + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH, y + GATEWAY_HEIGHT);
      handledElements.add(sequenceFlow1.getTargetRef().getId());

      currentHeight += TASK_HEIGHT + TASK_HEIGHT_SPACING;
    }

    // Second parallel gateway
    String someTaskId = sequenceFlows.get(0).getTargetRef().getId();
    FlowElement join = outgoingSequenceFlowMapping.get(someTaskId).get(0).getTargetRef();
    centerOfRhombus = centerOfRhombus + SEQUENCE_FLOW_WIDTH + TASK_WIDTH + LONG_SEQUENCE_FLOW_WITHOUT_ARROW_WIDTH;
    drawParallelGateway(join, centerOfRhombus - GATEWAY_WIDTH / 2, y, GATEWAY_WIDTH, GATEWAY_HEIGHT);
    handledElements.add(join.getId());

    currentWidth = originalCurrentWidth + TASK_BLOCK_WIDTH;
  }

  protected void drawStartEvent(FlowElement flowElement, int x, int y, int width, int height) {
    processDiagramCanvas.drawNoneStartEvent(x, y, width, height);
    currentWidth += EVENT_WIDTH;

    createDiagramInterchangeInformation(flowElement, x, y, width, height);
  }

  protected void drawEndEvent(FlowElement flowElement, int x, int y, int width, int height) {
    processDiagramCanvas.drawNoneEndEvent(x, y, width, height);
    currentWidth += EVENT_WIDTH;

    createDiagramInterchangeInformation(flowElement, x, y, width, height);
  }

  protected void drawParallelGateway(FlowElement flowElement, int x, int y, int width, int height) {
    processDiagramCanvas.drawParallelGateway(x, y, width, height);
    currentWidth += GATEWAY_WIDTH;

    createDiagramInterchangeInformation(flowElement, x, y, width, height);
  }

  protected void drawUserTask(FlowElement flowElement, int x, int y, int width, int height) {
    processDiagramCanvas.drawUserTask(flowElement.getName(), x, y, width, height);
    currentWidth += TASK_WIDTH;

    createDiagramInterchangeInformation(flowElement, x, y, width, height);
  }

  protected void drawSequenceFlow(SequenceFlow sequenceFlow, int... waypoints) {

    // Drawing
    int minX = Integer.MAX_VALUE;
    int maxX = 0;
    for (int i = 2; i < waypoints.length; i += 2) { // waypoints.size()
                                                    // minimally 4: x1, y1, x2,
                                                    // y2
      if (i < waypoints.length - 2) {
        processDiagramCanvas.drawSequenceflowWithoutArrow(waypoints[i - 2], 
                waypoints[i - 1], waypoints[i], waypoints[i + 1], false);
      } else {
        processDiagramCanvas.drawSequenceflow(waypoints[i - 2], 
                waypoints[i - 1], waypoints[i], waypoints[i + 1], false);
      }

      if (waypoints[i - 2] < minX || waypoints[i] < minX) {
        minX = Math.min(waypoints[i - 2], waypoints[i]);
      }
      if (waypoints[i - 2] > maxX || waypoints[i] > maxX) {
        maxX = Math.max(waypoints[i - 2], waypoints[i]);
      }
    }

    currentWidth += maxX - minX;

    // DI information
    BPMNEdge edge = new BPMNEdge();
    edge.setId(sequenceFlow.getId() + "_edge");
    edge.setBpmnElement(sequenceFlow);

    for (int i = 0; i < waypoints.length; i += 2) {
      edge.getWaypoint().add(new Point(waypoints[i], waypoints[i + 1]));
    }

    plane.getDiagramElement().add(edge);
  }

  protected void createDiagramInterchangeInformation(FlowElement flowElement,
          int x, int y, int width, int height) {

    BPMNShape shape = new BPMNShape();
    shape.setId(flowElement.getId() + "_shape");
    shape.setBpmnElement(flowElement);

    Bounds bounds = new Bounds();
    bounds.setX(x);
    bounds.setY(y);
    bounds.setWidth(width);
    bounds.setHeight(height);

    shape.setBounds(bounds);
    plane.getDiagramElement().add(shape);
  }

}
