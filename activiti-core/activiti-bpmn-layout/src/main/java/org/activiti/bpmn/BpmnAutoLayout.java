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
package org.activiti.bpmn;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.SwingConstants;

import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.DataObject;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
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
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import org.activiti.bpmn.model.TextAnnotation;

/**
 * Auto layouts a {@link BpmnModel}.
 *
 */
public class BpmnAutoLayout {

  private static final String STYLE_EVENT = "styleEvent";
  private static final String STYLE_GATEWAY = "styleGateway";
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
  protected Map<String, Association> associations;
  protected Map<String, TextAnnotation> textAnnotations;

  protected Map<String, SequenceFlow> sequenceFlows;
  protected List<BoundaryEvent> boundaryEvents;
  protected Map<String, FlowElement> handledFlowElements;

  protected Map<String, Artifact> handledArtifacts;

  protected Map<String, Object> generatedVertices;
  protected Map<String, Object> generatedSequenceFlowEdges;
  protected Map<String, Object> generatedAssociationEdges;

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

      // Operations that can only be done after all elements have received
      // DI
      translateNestedSubprocesses(process);
    }
  }

  protected void layout(FlowElementsContainer flowElementsContainer) {
    graph = new mxGraph();
    cellParent = graph.getDefaultParent();
    graph.getModel().beginUpdate();

 // Subprocesses are handled in a new instance of BpmnAutoLayout, hence they instantiations of new maps here.

    handledFlowElements = new HashMap<String, FlowElement>();
    handledArtifacts = new HashMap<String, Artifact>();
    generatedVertices = new HashMap<String, Object>();
    generatedSequenceFlowEdges = new HashMap<String, Object>();
    generatedAssociationEdges = new HashMap<String, Object>();

    associations = new HashMap<String, Association>(); //Associations are gathered and processed afterwards, because we must be sure we already found source and target
    textAnnotations = new HashMap<String, TextAnnotation>(); // Text Annotations are gathered and processed afterwards, because we must be sure we already found the parent.

    sequenceFlows = new HashMap<String, SequenceFlow>(); // Sequence flow are gathered and processed afterwards,because we mustbe sure we already found source and target
    boundaryEvents = new ArrayList<BoundaryEvent>(); // Boundary events are gathered and processed afterwards, because we must be sure we have its parent

    // Process all elements
    for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {

      if (flowElement instanceof SequenceFlow) {
        handleSequenceFlow((SequenceFlow) flowElement);
      } else if (flowElement instanceof Event) {
        handleEvent(flowElement);
      } else if (flowElement instanceof Gateway) {
        createGatewayVertex(flowElement);
      } else if (flowElement instanceof Task || flowElement instanceof CallActivity) {
        handleActivity(flowElement);
      } else if (flowElement instanceof SubProcess) {
        handleSubProcess(flowElement);
      }

      handledFlowElements.put(flowElement.getId(), flowElement);
    }

    // process artifacts
    for (Artifact artifact : flowElementsContainer.getArtifacts()) {

      if (artifact instanceof Association) {
        handleAssociation((Association) artifact);
      } else if (artifact instanceof TextAnnotation) {
        handleTextAnnotation((TextAnnotation) artifact);
      }

      handledArtifacts.put(artifact.getId(), artifact);
    }

    // Process gathered elements
    handleBoundaryEvents();
    handleSequenceFlow();
    handleAssociations();

    // All elements are now put in the graph. Let's layout them!
    CustomLayout layout = new CustomLayout(graph, SwingConstants.WEST);
    layout.setIntraCellSpacing(100.0);
    layout.setResizeParent(true);
    layout.setFineTuning(true);
    layout.setParentBorder(20);
    layout.setMoveParent(true);
    layout.setDisableEdgeStyle(false);
    layout.setUseBoundingBox(true);
    layout.execute(graph.getDefaultParent());

    graph.getModel().endUpdate();

    generateDiagramInterchangeElements();
  }

  private void handleTextAnnotation(TextAnnotation artifact) {
    ensureArtifactIdSet(artifact);
    textAnnotations.put(artifact.getId(), artifact);
  }

  // BPMN element handling

  protected void ensureSequenceFlowIdSet(SequenceFlow sequenceFlow) {
    // We really must have ids for sequence flow to be able to generate
    // stuff
    if (sequenceFlow.getId() == null) {
      sequenceFlow.setId("sequenceFlow-" + UUID.randomUUID().toString());
    }
  }

  protected void ensureArtifactIdSet(Artifact artifact) {
    // We really must have ids for sequence flow to be able to generate stuff
    if (artifact.getId() == null) {
      artifact.setId("artifact-" + UUID.randomUUID().toString());
    }
  }

  protected void handleAssociation(Association association) {
    ensureArtifactIdSet(association);
    associations.put(association.getId(), association);
  }

  protected void handleSequenceFlow(SequenceFlow sequenceFlow) {
    ensureSequenceFlowIdSet(sequenceFlow);
    sequenceFlows.put(sequenceFlow.getId(), sequenceFlow);
  }

  protected void handleEvent(FlowElement flowElement) {
    // Boundary events are an exception to the general way of drawing an
    // event
    if (flowElement instanceof BoundaryEvent) {
      boundaryEvents.add((BoundaryEvent) flowElement);
    } else {
      createEventVertex(flowElement);
    }
  }

  protected void handleActivity(FlowElement flowElement) {
    Object activityVertex = graph.insertVertex(cellParent, flowElement.getId(), "", 0, 0, taskWidth, taskHeight);
    generatedVertices.put(flowElement.getId(), activityVertex);
  }

  protected void handleSubProcess(FlowElement flowElement) {
    BpmnAutoLayout bpmnAutoLayout = new BpmnAutoLayout(bpmnModel);
    bpmnAutoLayout.layout((SubProcess) flowElement);

    double subProcessWidth = bpmnAutoLayout.getGraph().getView().getGraphBounds().getWidth();
    double subProcessHeight = bpmnAutoLayout.getGraph().getView().getGraphBounds().getHeight();
    Object subProcessVertex = graph.insertVertex(cellParent, flowElement.getId(), "", 0, 0, subProcessWidth + 2 * subProcessMargin, subProcessHeight + 2 * subProcessMargin);
    generatedVertices.put(flowElement.getId(), subProcessVertex);
  }

  protected void handleBoundaryEvents() {
    for (BoundaryEvent boundaryEvent : boundaryEvents) {
      mxGeometry geometry = new mxGeometry(0.8, 1.0, eventSize, eventSize);
      geometry.setOffset(new mxPoint(-(eventSize / 2), -(eventSize / 2)));
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
    boundaryEdgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.OrthConnector);
    graph.getStylesheet().putCellStyle(STYLE_BOUNDARY_SEQUENCEFLOW, boundaryEdgeStyle);

    for (SequenceFlow sequenceFlow : sequenceFlows.values()) {
      Object sourceVertex = generatedVertices.get(sequenceFlow.getSourceRef());
      Object targetVertex = generatedVertices.get(sequenceFlow.getTargetRef());

      String style = null;

      if (handledFlowElements.get(sequenceFlow.getSourceRef()) instanceof BoundaryEvent) {
        // Sequence flow out of boundary events are handled in a
        // different way,
        // to make them visually appealing for the eye of the dear end
        // user.
        style = STYLE_BOUNDARY_SEQUENCEFLOW;
      } else {
        style = STYLE_SEQUENCEFLOW;
      }

      Object sequenceFlowEdge = graph.insertEdge(cellParent, sequenceFlow.getId(), "", sourceVertex, targetVertex, style);
      generatedSequenceFlowEdges.put(sequenceFlow.getId(), sequenceFlowEdge);
    }
  }

  protected void handleAssociations() {

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
    boundaryEdgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.OrthConnector);
    graph.getStylesheet().putCellStyle(STYLE_BOUNDARY_SEQUENCEFLOW, boundaryEdgeStyle);

    for (Association association : associations.values()) {
      Object sourceVertex = generatedVertices.get(association.getSourceRef());
      Object targetVertex = generatedVertices.get(association.getTargetRef());

      String style = null;

      if (handledFlowElements.get(association.getSourceRef()) instanceof BoundaryEvent) {
        // Sequence flow out of boundary events are handled in a different way,
        // to make them visually appealing for the eye of the dear end user.
        style = STYLE_BOUNDARY_SEQUENCEFLOW;
      } else {
        style = STYLE_SEQUENCEFLOW;
      }

      Object associationEdge = graph.insertEdge(cellParent, association.getId(), "", sourceVertex, targetVertex, style);
      generatedAssociationEdges.put(association.getId(), associationEdge);
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
    generateActivityDiagramInterchangeElements();
    generateSequenceFlowDiagramInterchangeElements();
    generateAssociationDiagramInterchangeElements();
  }

  protected void generateActivityDiagramInterchangeElements() {
    for (String flowElementId : generatedVertices.keySet()) {
      Object vertex = generatedVertices.get(flowElementId);
      mxCellState cellState = graph.getView().getState(vertex);
      GraphicInfo subProcessGraphicInfo = createDiagramInterchangeInformation(handledFlowElements.get(flowElementId), (int) cellState.getX(), (int) cellState.getY(), (int) cellState.getWidth(),
          (int) cellState.getHeight());

      // The DI for the elements of a subprocess are generated without
      // knowledge of the rest of the graph
      // So we must translate all it's elements with the x and y of the
      // subprocess itself
      if (handledFlowElements.get(flowElementId) instanceof SubProcess) {

        // Always expanded when auto layouting
        subProcessGraphicInfo.setExpanded(true);
      }
    }
  }

  protected void generateSequenceFlowDiagramInterchangeElements() {
    for (String sequenceFlowId : generatedSequenceFlowEdges.keySet()) {
      Object edge = generatedSequenceFlowEdges.get(sequenceFlowId);
      List<mxPoint> points = graph.getView().getState(edge).getAbsolutePoints();

      // JGraphX has this funny way of generating the outgoing sequence
      // flow of a gateway
      // Visually, we'd like them to originate from one of the corners of
      // the rhombus,
      // hence we force the starting point of the sequence flow to the
      // closest rhombus corner point.
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
        for (mxPoint rhombusPoint : asList(northPoint, southPoint, eastPoint, westPoint)) {
          double distance = euclidianDistance(startPoint, rhombusPoint);
          if (distance < closestDistance) {
            closestDistance = distance;
            closestPoint = rhombusPoint;
          }
        }
        startPoint.setX(closestPoint.getX());
        startPoint.setY(closestPoint.getY());

        // We also need to move the second point.
        // Since we know the layout is from left to right, this is not a
        // problem
        if (points.size() > 1) {
          mxPoint nextPoint = points.get(1);
          nextPoint.setY(closestPoint.getY());
        }

      }

      createDiagramInterchangeInformation(handledFlowElements.get(sequenceFlowId), optimizeEdgePoints(points));
    }
  }

  protected void generateAssociationDiagramInterchangeElements() {
    for (String associationId : generatedAssociationEdges.keySet()) {

      Object edge = generatedAssociationEdges.get(associationId);
      List<mxPoint> points = graph.getView().getState(edge).getAbsolutePoints();

      createDiagramInterchangeInformation(handledArtifacts.get(associationId), optimizeEdgePoints(points));

    }
  }

  protected double euclidianDistance(mxPoint point1, mxPoint point2) {
    return Math.sqrt(((point2.getX() - point1.getX()) * (point2.getX() - point1.getX()) + (point2.getY() - point1.getY()) * (point2.getY() - point1.getY())));
  }

  // JGraphX sometime generates points that visually are not really necessary.
  // This method will remove any such points.
  protected List<mxPoint> optimizeEdgePoints(List<mxPoint> unoptimizedPointsList) {
    List<mxPoint> optimizedPointsList = new ArrayList<mxPoint>();
    for (int i = 0; i < unoptimizedPointsList.size(); i++) {

      boolean keepPoint = true;
      mxPoint currentPoint = unoptimizedPointsList.get(i);

      // When three points are on the same x-axis with same y value, the
      // middle point can be removed
      if (i > 0 && i != unoptimizedPointsList.size() - 1) {

        mxPoint previousPoint = unoptimizedPointsList.get(i - 1);
        mxPoint nextPoint = unoptimizedPointsList.get(i + 1);

        if (currentPoint.getX() >= previousPoint.getX() && currentPoint.getX() <= nextPoint.getX() && currentPoint.getY() == previousPoint.getY() && currentPoint.getY() == nextPoint.getY()) {
          keepPoint = false;
        } else if (currentPoint.getY() >= previousPoint.getY() && currentPoint.getY() <= nextPoint.getY() && currentPoint.getX() == previousPoint.getX() && currentPoint.getX() == nextPoint.getX()) {
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

  protected void createDiagramInterchangeInformation(BaseElement element, List<mxPoint> waypoints) {
    List<GraphicInfo> graphicInfoForWaypoints = new ArrayList<GraphicInfo>();
    for (mxPoint waypoint : waypoints) {
      GraphicInfo graphicInfo = new GraphicInfo();
      graphicInfo.setElement(element);
      graphicInfo.setX(waypoint.getX());
      graphicInfo.setY(waypoint.getY());
      graphicInfoForWaypoints.add(graphicInfo);
    }
    bpmnModel.addFlowGraphicInfoList(element.getId(), graphicInfoForWaypoints);
  }

  /**
   * Since subprocesses are autolayouted independently (see {@link #handleSubProcess(FlowElement)}), the elements have x and y coordinates relative to the bounds of the subprocess (thinking the
   * subprocess is on (0,0). This however, does not work for nested subprocesses, as they need to take in account the x and y coordinates for each of the parent subproceses.
   *
   * This method is to be called after fully layouting one process, since ALL elements need to have x and y.
   */
  protected void translateNestedSubprocesses(Process process) {
       for (FlowElement flowElement : process.getFlowElements()) {
            if (flowElement instanceof SubProcess) {
              translateNestedSubprocessElements((SubProcess) flowElement);
            }
       }
  }

  protected void translateNestedSubprocessElements(SubProcess subProcess) {

    GraphicInfo subProcessGraphicInfo = bpmnModel.getLocationMap().get(subProcess.getId());
    double subProcessX = subProcessGraphicInfo.getX();
    double subProcessY = subProcessGraphicInfo.getY();

    List<SubProcess> nestedSubProcesses = new ArrayList<SubProcess>();
    for (FlowElement flowElement : subProcess.getFlowElements()) {

      if (flowElement instanceof SequenceFlow) {
        List<GraphicInfo> graphicInfos = bpmnModel.getFlowLocationMap().get(flowElement.getId());
        for (GraphicInfo graphicInfo : graphicInfos) {
          graphicInfo.setX(graphicInfo.getX() + subProcessX + subProcessMargin);
          graphicInfo.setY(graphicInfo.getY() + subProcessY + subProcessMargin);
        }
      } else if (flowElement instanceof DataObject == false) {

        // Regular element
        GraphicInfo graphicInfo = bpmnModel.getLocationMap().get(flowElement.getId());
        graphicInfo.setX(graphicInfo.getX() + subProcessX + subProcessMargin);
        graphicInfo.setY(graphicInfo.getY() + subProcessY + subProcessMargin);
      }

      if (flowElement instanceof SubProcess) {
        nestedSubProcesses.add((SubProcess) flowElement);
      }

    }

    // Continue for next level of nested subprocesses
    for (SubProcess nestedSubProcess : nestedSubProcesses) {
      translateNestedSubprocessElements(nestedSubProcess);
    }
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

  // Due to a bug (see
  // http://forum.jgraph.com/questions/5952/mxhierarchicallayout-not-correct-when-using-child-vertex)
  // We must extend the default hierarchical layout to tweak it a bit (see url
  // link) otherwise the layouting crashes.
  //
  // Verify again with a later release if fixed (ie the mxHierarchicalLayout
  // can be used directly)
  static class CustomLayout extends mxHierarchicalLayout {

    public CustomLayout(mxGraph graph, int orientation) {
      super(graph, orientation);
      this.traverseAncestors = false;
    }

  }

}
