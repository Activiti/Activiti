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

package org.activiti.bpmn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;

/**
 * Auto layouts a {@link BpmnModel}.
 * 
 * @author Joram Barrez
 */
public class BpmnAutoLayout {
  
  private static final String STYLE_EVENT = "styleEvent";
  private static final String STYLE_GATEWAY = "styleGateway";
  private static final String STYLE_SEQUENCEFLOW = "styleSequenceFlow";
  
  protected BpmnModel bpmnModel;
  
  protected int eventSize = 30;
  protected int gatewaySize = 30;
  protected int taskWidth = 100;
  protected int taskHeight = 50;
  
  protected mxGraph graph;
  protected Object cellParent;
  protected List<SequenceFlow> sequenceFlows;
  protected List<BoundaryEvent> boundaryEvents;
  protected Map<String, FlowElement> handledFlowElements;
  protected Map<String, Object> generatedVertices;
  protected Map<String, Object> generatedEdges;
  
  public BpmnAutoLayout(BpmnModel bpmnModel) {
    this.bpmnModel = bpmnModel;
  }
  
  public void execute() {
    
    for (Process process : bpmnModel.getProcesses()) {
      
      graph = new mxGraph();
      cellParent = graph.getDefaultParent();
      graph.getModel().beginUpdate();
      
      handledFlowElements = new HashMap<String, FlowElement>();
      generatedVertices = new HashMap<String, Object>();
      generatedEdges = new HashMap<String, Object>();
      
      sequenceFlows = new ArrayList<SequenceFlow>(); // Sequence flow are gathered and processed afterwards, because we must be sure we alreadt found source and target
      boundaryEvents = new ArrayList<BoundaryEvent>(); // Boundary events are gathered and processed afterwards, because we must be sure we have its parent
      
      // Process all elements
      for (FlowElement flowElement : process.getFlowElements()) {
        
        if (flowElement instanceof SequenceFlow) {
          handleSequenceFlow(flowElement);
        } else if (flowElement instanceof Event) {
          handleEvent(flowElement);
        } else if (flowElement instanceof Gateway) {
          createGatewayVertex(flowElement);
        } else if (flowElement instanceof Task) {
          handleTask(flowElement);
        } else if (flowElement instanceof SubProcess) {
          // TODO
        }
        
        handledFlowElements.put(flowElement.getId(), flowElement);
      }
      
      // Process gathered elements
      handleBoundaryEvents();
      handleSequenceFlow();
      
      // All elements are now put in the graph. Let's layout them!
      CustomLayout layout = new CustomLayout(graph, SwingConstants.WEST);
      layout.setIntraCellSpacing(100.0);
      layout.setResizeParent(true);
      layout.setFineTuning(true);
      layout.setParentBorder(20);
      layout.setMoveParent(true);
      layout.setDisableEdgeStyle(false);
      layout.execute(graph.getDefaultParent());
      
      graph.getModel().endUpdate();
      
      generateDiagramInterchangeElements();
    }
    
  }

  // BPMN element handling

  protected void handleSequenceFlow(FlowElement flowElement) {
    sequenceFlows.add((SequenceFlow) flowElement);
  }
  
  protected void handleEvent(FlowElement flowElement) {
    // Boundary events are an exception to the general way of drawing an event
    if (flowElement instanceof BoundaryEvent) {
      boundaryEvents.add((BoundaryEvent) flowElement);
    } else {
      createEventVertex(flowElement);
    }
  }
  
  protected void handleTask(FlowElement flowElement) {
    Object taskVertex = graph.insertVertex(cellParent, flowElement.getId(), "", 0, 0, taskWidth, taskHeight);
    generatedVertices.put(flowElement.getId(), taskVertex);
  }
  
  protected void handleBoundaryEvents() {
    for (BoundaryEvent boundaryEvent : boundaryEvents) {
      mxGeometry geometry = new mxGeometry(0.8, 1.0, eventSize, eventSize);
      geometry.setOffset(new mxPoint(-(eventSize/2), -(eventSize/2)));
      geometry.setRelative(true);
      mxCell boundaryPort = new mxCell(null, geometry, "shape=ellipse;perimter=ellipsePerimeter");
      boundaryPort.setVertex(true);

      Object portParent = null;
      if (boundaryEvent.getAttachedToRefId() != null) {
        portParent = generatedVertices.get(boundaryEvent.getAttachedToRefId());
      } else if (boundaryEvent.getAttachedToRef() != null) {
        portParent = generatedVertices.get(boundaryEvent.getAttachedToRef().getId());
      } else {
        throw new RuntimeException("Could not generate DI: boundaryEvent '" + boundaryEvent.getId() + "' has no attachedToRef");
      }
      
      graph.addCell(boundaryPort, portParent);
      generatedVertices.put(boundaryEvent.getId(), boundaryPort);
    }
  }
  
  protected void handleSequenceFlow() {
    
    Hashtable<String, Object> edgeStyle = new Hashtable<String, Object>();
    edgeStyle.put(mxConstants.STYLE_ORTHOGONAL, true);
    edgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.ElbowConnector); // TODO: DISCUSS
    graph.getStylesheet().putCellStyle(STYLE_SEQUENCEFLOW, edgeStyle);
    
    for (SequenceFlow sequenceFlow : sequenceFlows) {
      Object sourceVertex = generatedVertices.get(sequenceFlow.getSourceRef());
      Object targertVertex = generatedVertices.get(sequenceFlow.getTargetRef());
      
      Object sequenceFlowEdge = graph.insertEdge(cellParent, sequenceFlow.getId(), "", sourceVertex, targertVertex, STYLE_SEQUENCEFLOW);
      generatedEdges.put(sequenceFlow.getId(), sequenceFlowEdge);
    }
  }
  
  // Graph cell creation
  
  protected void createEventVertex(FlowElement flowElement) {
    // Add styling for events if needed
    if (!graph.getStylesheet().getStyles().containsKey(STYLE_EVENT)) {
      Hashtable<String, Object> eventStyle = new Hashtable<String, Object>();
      eventStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
      graph.getStylesheet().putCellStyle(STYLE_EVENT, eventStyle);  
    }
    
    // Add vertex representing event to graph
    Object eventVertex = graph.insertVertex(cellParent, flowElement.getId(), "", 0, 0, eventSize, eventSize, STYLE_EVENT);
    generatedVertices.put(flowElement.getId(), eventVertex);
  }
  
  protected void createGatewayVertex(FlowElement flowElement) {
    // Add styling for gateways if needed
    if (graph.getStylesheet().getStyles().containsKey(STYLE_GATEWAY)) {
      Hashtable<String, Object> style = new Hashtable<String, Object>();
      style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
      graph.getStylesheet().putCellStyle(STYLE_GATEWAY, style);
    }
    
    // Create gateway node 
    Object gatewayVertex = graph.insertVertex(cellParent, flowElement.getId(), "", 0, 0, gatewaySize, gatewaySize, STYLE_GATEWAY);
    generatedVertices.put(flowElement.getId(), gatewayVertex);
  }
  
  // Diagram interchange generation
  
  protected void generateDiagramInterchangeElements() {
    
    // Reset any previous DI information
    bpmnModel.getLocationMap().clear();
    bpmnModel.getFlowLocationMap().clear();

    for (String flowElementId : generatedVertices.keySet()) {
      Object vertex = generatedVertices.get(flowElementId);
      mxGeometry geometry = graph.getCellGeometry(vertex);
      createDiagramInterchangeInformation(handledFlowElements.get(flowElementId), 
              (int) geometry.getX(), (int) geometry.getY(), (int) geometry.getWidth(), (int) geometry.getHeight());
    }
    
    for (String sequenceFlowId : generatedEdges.keySet()) {
      Object edge = generatedEdges.get(sequenceFlowId);
      List<mxPoint> points = graph.getView().getState(edge).getAbsolutePoints();
      int[] waypoints = new int[points.size() * 2];
      int index = 0;
      for (mxPoint point : points) {
        waypoints[index++] = (int) point.getX();
        waypoints[index++] = (int) point.getY();
      }
      createDiagramInterchangeInformation((SequenceFlow) handledFlowElements.get(sequenceFlowId), waypoints);
    }
    
  }
  
  protected void createDiagramInterchangeInformation(FlowElement flowElement, int x, int y, int width, int height) {
    GraphicInfo graphicInfo = new GraphicInfo();
    graphicInfo.setX(x);
    graphicInfo.setY(y);
    graphicInfo.setWidth(width);
    graphicInfo.setHeight(height);
    graphicInfo.setElement(flowElement);
    bpmnModel.addGraphicInfo(flowElement.getId(), graphicInfo);
  }
  
  protected void createDiagramInterchangeInformation(SequenceFlow sequenceFlow, int[] waypoints) {
    List<GraphicInfo> graphicInfoForWaypoints = new ArrayList<GraphicInfo>();
    for (int i = 0; i < waypoints.length; i += 2) {
      GraphicInfo graphicInfo = new GraphicInfo();
      graphicInfo.setElement(sequenceFlow);
      graphicInfo.setX(waypoints[i]);
      graphicInfo.setY(waypoints[i + 1]);
      graphicInfoForWaypoints.add(graphicInfo);
    }
    bpmnModel.addFlowGraphicInfoList(sequenceFlow.getId(), graphicInfoForWaypoints);
  }
  
  // Getters and Setters
  
  public int getEventSize() {
    return eventSize;
  }
  
  public void setEventSize(int eventSize) {
    this.eventSize = eventSize;
  }
  
  public int getGatewaySize() {
    return gatewaySize;
  }
  
  public void setGatewaySize(int gatewaySize) {
    this.gatewaySize = gatewaySize;
  }

  
  public int getTaskWidth() {
    return taskWidth;
  }

  public void setTaskWidth(int taskWidth) {
    this.taskWidth = taskWidth;
  }

  public int getTaskHeight() {
    return taskHeight;
  }
  
  public void setTaskHeight(int taskHeight) {
    this.taskHeight = taskHeight;
  } 
  
  // Due to a bug (see http://forum.jgraph.com/questions/5952/mxhierarchicallayout-not-correct-when-using-child-vertex)
  // We must extend the default hierarchical layout to tweak it a bit (see url link) otherwise the layouting crashes.
  //
  // Verify again with a later release if fixed (ie the mxHierarchicalLayout can be used directly)
  static class CustomLayout extends mxHierarchicalLayout {
    
    public CustomLayout(mxGraph graph, int orientation) {
      super(graph, orientation);
      this.traverseAncestors = false;
    }
    
  }
  
}
