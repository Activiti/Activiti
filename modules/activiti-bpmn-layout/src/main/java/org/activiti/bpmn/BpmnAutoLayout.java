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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.layout.flow.FlowCommand;
import org.activiti.layout.flow.FlowControl;
import org.activiti.layout.flow.HandleActivity;
import org.activiti.layout.flow.HandleEventFlow;
import org.activiti.layout.flow.HandleGatewayVertex;
import org.activiti.layout.flow.HandleSequenceFlow;
import org.activiti.layout.flow.HandleSubProcess;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;

/**
 * Auto layouts a {@link BpmnModel}.
 * 
 * @author Joram Barrez
 */
public class BpmnAutoLayout {
  
  private static final String STYLE_SEQUENCEFLOW = "styleSequenceFlow";
  private static final String STYLE_BOUNDARY_SEQUENCEFLOW = "styleBoundarySequenceFlow";
  
  protected BpmnModel bpmnModel;
  
  protected int eventSize = 30;
  protected int gatewaySize = 40;
  protected int taskWidth = 100;
  protected int taskHeight = 60;
  protected int subProcessMargin = 20;
  
  protected mxGraph graph;
  protected Object cellParent;
  protected Map<String, SequenceFlow> sequenceFlows;
  protected List<BoundaryEvent> boundaryEvents;
  protected Map<String, FlowElement> handledFlowElements;
  protected Map<String, Object> generatedVertices;
  protected Map<String, Object> generatedEdges;
  
  public BpmnAutoLayout(BpmnModel bpmnModel) {
    this.bpmnModel = bpmnModel;
  }
  
  public void execute() {
    // Reset any previous DI information
    bpmnModel.getLocationMap().clear();
    bpmnModel.getFlowLocationMap().clear();
    
    // Generate DI for each process
    for (Process process : bpmnModel.getProcesses()) {
      layout(process);
    }
  }

	public void layout(FlowElementsContainer flowElementsContainer) {
		graph = new mxGraph();
		cellParent = graph.getDefaultParent();
		graph.getModel().beginUpdate();

		handledFlowElements = new HashMap<String, FlowElement>();
		generatedVertices = new HashMap<String, Object>();
		generatedEdges = new HashMap<String, Object>();

		sequenceFlows = new HashMap<String, SequenceFlow>();
		boundaryEvents = new ArrayList<BoundaryEvent>(); 

		// Process all elements
		for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
			handleFlowElement(flowElement);
		}

		// Process gathered elements
		handleBoundaryEvents();
		handleSequenceFlow();

		// All elements are now put in the graph. Let's layout them!
		customLayout();

		graph.getModel().endUpdate();

		generateDiagramInterchangeElements();
	}
	
	private void handleFlowElement(FlowElement flowElement) {
		FlowCommand flowCommand = null;
		if (flowElement instanceof SequenceFlow) {
			flowCommand = new HandleSequenceFlow(FlowControl.createFlowControlHandleSequenceFlow(flowElement, sequenceFlows));
		} else if (flowElement instanceof Event) {
			flowCommand = new HandleEventFlow(FlowControl.createFlowControlHandleEvent(flowElement, boundaryEvents, generatedVertices, flowCommand, eventSize, graph));
		} else if (flowElement instanceof Gateway) {
			flowCommand = new HandleGatewayVertex(FlowControl.createFlowControlHandleGatewayVertext(flowElement, gatewaySize, flowCommand, graph, generatedVertices));
		} else if (flowElement instanceof Task || flowElement instanceof CallActivity) {
			flowCommand = new HandleActivity(FlowControl.createFlowControlHandleActivity(flowElement, generatedVertices, flowCommand, graph, taskWidth, taskHeight));
		} else if (flowElement instanceof SubProcess) {
			BpmnAutoLayout bpmnAutoLayout = new BpmnAutoLayout(bpmnModel);
			bpmnAutoLayout.layout((SubProcess) flowElement);
			flowCommand = new HandleSubProcess(FlowControl.createFlowControlHandleSubProcess(flowElement, bpmnAutoLayout, bpmnAutoLayout, graph, generatedVertices, subProcessMargin));
		}
		
		if(flowCommand != null) {
			flowCommand.execute();
		}
		
		handledFlowElements.put(flowElement.getId(), flowElement);
	}
	
	private void customLayout() {
		CustomLayout layout = new CustomLayout(graph, SwingConstants.WEST);
		layout.setIntraCellSpacing(100.0);
		layout.setResizeParent(true);
		layout.setFineTuning(true);
		layout.setParentBorder(20);
		layout.setMoveParent(true);
		layout.setDisableEdgeStyle(false);
		layout.setUseBoundingBox(true);
		layout.execute(graph.getDefaultParent());
	}
	
  protected void handleBoundaryEvents() {
    for (BoundaryEvent boundaryEvent : boundaryEvents) {
      mxGeometry geometry = new mxGeometry(0.8, 1.0, eventSize, eventSize);
      geometry.setOffset(new mxPoint(-(eventSize/2), -(eventSize/2)));
      geometry.setRelative(true);
      mxCell boundaryPort = new mxCell(null, geometry, "shape=ellipse;perimter=ellipsePerimeter");
      boundaryPort.setId("boundary-event-" + boundaryEvent.getId());
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
    edgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.ElbowConnector);
    edgeStyle.put(mxConstants.STYLE_ENTRY_X, 0.0);
    edgeStyle.put(mxConstants.STYLE_ENTRY_Y, 0.5);
    graph.getStylesheet().putCellStyle(STYLE_SEQUENCEFLOW, edgeStyle);
    
    Hashtable<String, Object> boundaryEdgeStyle = new Hashtable<String, Object>();
    boundaryEdgeStyle.put(mxConstants.STYLE_EXIT_X, 0.5);
    boundaryEdgeStyle.put(mxConstants.STYLE_EXIT_Y, 1.0);
    boundaryEdgeStyle.put(mxConstants.STYLE_ENTRY_X, 0.5);
    boundaryEdgeStyle.put(mxConstants.STYLE_ENTRY_Y, 1.0);
    boundaryEdgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.orthConnector);
    graph.getStylesheet().putCellStyle(STYLE_BOUNDARY_SEQUENCEFLOW, boundaryEdgeStyle);
    
    for (SequenceFlow sequenceFlow : sequenceFlows.values()) {
      Object sourceVertex = generatedVertices.get(sequenceFlow.getSourceRef());
      Object targertVertex = generatedVertices.get(sequenceFlow.getTargetRef());
      
      String style = null;
     
      if (handledFlowElements.get(sequenceFlow.getSourceRef()) instanceof BoundaryEvent) {
        // Sequence flow out of boundary events are handled in a different way,
        // to make them visually appealing for the eye of the dear end user.
        style = STYLE_BOUNDARY_SEQUENCEFLOW;
      } else {
        style = STYLE_SEQUENCEFLOW;
      }
      
      Object sequenceFlowEdge = graph.insertEdge(cellParent, sequenceFlow.getId(), "", sourceVertex, targertVertex, style);
      generatedEdges.put(sequenceFlow.getId(), sequenceFlowEdge);
    }
  }
  
  // Diagram interchange generation
  
  protected void generateDiagramInterchangeElements() {
    generateActivityDiagramInterchangeElements();
    generateSequenceFlowDiagramInterchangeElements();
  }
  
  protected void generateActivityDiagramInterchangeElements() {
    for (String flowElementId : generatedVertices.keySet()) {
      Object vertex = generatedVertices.get(flowElementId);
      mxCellState cellState = graph.getView().getState(vertex);
      GraphicInfo subProcessGraphicInfo = createDiagramInterchangeInformation(handledFlowElements.get(flowElementId), 
              (int) cellState.getX(), (int) cellState.getY(), (int) cellState.getWidth(), (int) cellState.getHeight());
      
      // The DI for the elements of a subprocess are generated without knowledge of the rest of the graph
      // So we must translate all it's elements with the x and y of the subprocess itself
      if (handledFlowElements.get(flowElementId) instanceof SubProcess) {
        SubProcess subProcess =(SubProcess) handledFlowElements.get(flowElementId);
        
        // Always expanded when auto layouting
        subProcessGraphicInfo.setExpanded(true);
        
        // Translate
        double subProcessX = cellState.getX();
        double subProcessY = cellState.getY();
        double translationX = subProcessX + subProcessMargin;
        double translationY = subProcessY + subProcessMargin;
        for (FlowElement subProcessElement : subProcess.getFlowElements()) {
          if (subProcessElement instanceof SequenceFlow) {
            List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(subProcessElement.getId());
            for (GraphicInfo graphicInfo : graphicInfoList) {
              graphicInfo.setX(graphicInfo.getX() + translationX);
              graphicInfo.setY(graphicInfo.getY() + translationY);
            }
          } else {
            GraphicInfo graphicInfo = bpmnModel.getLocationMap().get(subProcessElement.getId());
            graphicInfo.setX(graphicInfo.getX() + translationX);
            graphicInfo.setY(graphicInfo.getY() + translationY);
          }
        }
      }
    }
  }

  protected void generateSequenceFlowDiagramInterchangeElements() {
    for (String sequenceFlowId : generatedEdges.keySet()) {
      Object edge = generatedEdges.get(sequenceFlowId);
      List<mxPoint> points = graph.getView().getState(edge).getAbsolutePoints();
      
      // JGraphX has this funny way of generating the outgoing sequence flow of a gateway
      // Visually, we'd like them to originate from one of the corners of the rhombus,
      // hence we force the starting point of the sequence flow to the closest rhombus corner point.
      FlowElement sourceElement = handledFlowElements.get(sequenceFlows.get(sequenceFlowId).getSourceRef()); 
      if (sourceElement instanceof Gateway && ((Gateway) sourceElement).getOutgoingFlows().size() > 1) {
        mxPoint startPoint = points.get(0);
        Object gatewayVertex = generatedVertices.get(sourceElement.getId());
        mxCellState gatewayState = graph.getView().getState(gatewayVertex);
        
        mxPoint northPoint = new mxPoint(gatewayState.getX() + (gatewayState.getWidth()) / 2, gatewayState.getY());
        mxPoint southPoint = new mxPoint(gatewayState.getX() + (gatewayState.getWidth()) / 2, gatewayState.getY() + gatewayState.getHeight());
        mxPoint eastPoint = new mxPoint(gatewayState.getX() + gatewayState.getWidth(), gatewayState.getY() + (gatewayState.getHeight()) / 2);
        mxPoint westPoint = new mxPoint(gatewayState.getX(), gatewayState.getY() + (gatewayState.getHeight()) / 2);
        
        double closestDistance = Double.MAX_VALUE;
        mxPoint closestPoint = null;
        for (mxPoint rhombusPoint : Arrays.asList(northPoint, southPoint, eastPoint, westPoint)) {
          double distance = euclidianDistance(startPoint, rhombusPoint);
          if (distance < closestDistance) {
            closestDistance = distance;
            closestPoint = rhombusPoint;
          }
        }
        startPoint.setX(closestPoint.getX());
        startPoint.setY(closestPoint.getY());
        
        // We also need to move the second point.
        // Since we know the layout is from left to right, this is not a problem
        if (points.size() > 1) {
          mxPoint nextPoint = points.get(1);
          nextPoint.setY(closestPoint.getY());
        }
        
      }
      
      createDiagramInterchangeInformation((SequenceFlow) handledFlowElements.get(sequenceFlowId), optimizeEdgePoints(points));
    }
  }

  protected double euclidianDistance(mxPoint point1, mxPoint point2) {
    return Math.sqrt( ( (point2.getX() - point1.getX())*(point2.getX() - point1.getX()) 
            + (point2.getY() - point1.getY())*(point2.getY() - point1.getY()) ) );
  }
  
  // JGraphX sometime generates points that visually are not really necessary.
  // This method will remove any such points.
  protected List<mxPoint> optimizeEdgePoints(List<mxPoint> unoptimizedPointsList) {
    List<mxPoint> optimizedPointsList = new ArrayList<mxPoint>();
    for (int i=0; i<unoptimizedPointsList.size(); i++) {

      boolean keepPoint = true;
      mxPoint currentPoint = unoptimizedPointsList.get(i);
      
      // When three points are on the same x-axis with same y value, the middle point can be removed
      if (i > 0 && i != unoptimizedPointsList.size() - 1) {
        
        mxPoint previousPoint = unoptimizedPointsList.get(i - 1);
        mxPoint nextPoint = unoptimizedPointsList.get(i + 1);
        
        if (currentPoint.getX() >= previousPoint.getX() 
                && currentPoint.getX() <= nextPoint.getX()
                && currentPoint.getY() == previousPoint.getY()
                && currentPoint.getY() == nextPoint.getY()) {
          keepPoint = false;
        } else if (currentPoint.getY() >= previousPoint.getY()
                && currentPoint.getY() <= nextPoint.getY()
                && currentPoint.getX() == previousPoint.getX()
                && currentPoint.getX() == nextPoint.getX()) {
          keepPoint = false;
        }
        
      }
      
      if (keepPoint) {
        optimizedPointsList.add(currentPoint);
      }
      
    }
    
    return optimizedPointsList;
  }
  
  protected GraphicInfo createDiagramInterchangeInformation(FlowElement flowElement, int x, int y, int width, int height) {
    GraphicInfo graphicInfo = new GraphicInfo();
    graphicInfo.setX(x);
    graphicInfo.setY(y);
    graphicInfo.setWidth(width);
    graphicInfo.setHeight(height);
    graphicInfo.setElement(flowElement);
    bpmnModel.addGraphicInfo(flowElement.getId(), graphicInfo);
    
    return graphicInfo;
  }
  
  protected void createDiagramInterchangeInformation(SequenceFlow sequenceFlow, List<mxPoint> waypoints) {
    List<GraphicInfo> graphicInfoForWaypoints = new ArrayList<GraphicInfo>();
    for (mxPoint waypoint : waypoints) {
      GraphicInfo graphicInfo = new GraphicInfo();
      graphicInfo.setElement(sequenceFlow);
      graphicInfo.setX(waypoint.getX());
      graphicInfo.setY(waypoint.getY());
      graphicInfoForWaypoints.add(graphicInfo);
    }
    bpmnModel.addFlowGraphicInfoList(sequenceFlow.getId(), graphicInfoForWaypoints);
  }
  
  // Getters and Setters
  
  
  public mxGraph getGraph() {
    return graph;
  }

  public void setGraph(mxGraph graph) {
    this.graph = graph;
  }
  
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
  
  public int getSubProcessMargin() {
    return subProcessMargin;
  }
  
  public void setSubProcessMargin(int subProcessMargin) {
    this.subProcessMargin = subProcessMargin;
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
