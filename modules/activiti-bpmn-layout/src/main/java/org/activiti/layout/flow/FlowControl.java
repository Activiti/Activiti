package org.activiti.layout.flow;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;

import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;

public class FlowControl {

	private static final String STYLE_EVENT = "styleEvent";
	private static final String STYLE_GATEWAY = "styleGateway";
	
	private Map<String, SequenceFlow> sequencesFlow;
	private FlowElement flowElement;
	private List<BoundaryEvent> boundaryEvents;
	private Map<String, Object> generatedVertices;
	private Object cellParent;
	private int eventSize;
	private mxGraph graph;
	private int gatewaySize;
	private int taskWidth;
	private int taskHeight;
	private int subProcessMargin;
	private BpmnAutoLayout bpmnAutoLayout;
	
	public FlowControl(FlowElement flowElement, Map<String, SequenceFlow> sequencesFlow) {
		this.sequencesFlow = sequencesFlow;
		this.flowElement = flowElement;
	}
	
	public FlowControl(FlowElement flowElement, List<BoundaryEvent> boundaryEvents, Map<String, Object> generatedVertices, Object cellParent, int eventSize, mxGraph graph) {
		this.flowElement = flowElement;
		this.boundaryEvents = boundaryEvents;
		this.generatedVertices = generatedVertices;
		this.cellParent = cellParent;
		this.eventSize = eventSize;
		this.graph = graph;
	}
	
	public FlowControl(FlowElement flowElement, Map<String, Object> generatedVertices, Object cellParent, mxGraph graph, int taskWidth, int taskHeight) {
		this.flowElement = flowElement;
		this.generatedVertices = generatedVertices;
		this.cellParent = cellParent;
		this.taskWidth = taskWidth;
		this.taskHeight = taskHeight;
		this.graph = graph;
	}
	
	public FlowControl(FlowElement flowElement, int gatewaySize, Object cellParent, mxGraph graph, Map<String, Object> generatedVertices) {
		this.flowElement = flowElement;
		this.gatewaySize = gatewaySize;
		this.cellParent = cellParent;
		this.graph = graph;
		this.generatedVertices = generatedVertices;
	}
	
	public FlowControl(FlowElement flowElement, BpmnAutoLayout bpmnAutoLayout, Object cellParent, mxGraph graph, Map<String, Object> generatedVertices, int subProcessMargin) {
		this.flowElement = flowElement;
		this.bpmnAutoLayout = bpmnAutoLayout;
		this.cellParent = cellParent;
		this.graph = graph;
		this.generatedVertices = generatedVertices;
		this.subProcessMargin = subProcessMargin;
	}
	
	public static FlowControl createFlowControlHandleSequenceFlow(FlowElement flowElement, Map<String, SequenceFlow> sequencesFlow) {
		return new FlowControl(flowElement, sequencesFlow);
	}
	
	public static FlowControl createFlowControlHandleEvent(FlowElement flowElement, List<BoundaryEvent> boundaryEvents, Map<String, Object> generatedVertices, Object cellParent, int eventSize, mxGraph graph) {
		return new FlowControl(flowElement, boundaryEvents, generatedVertices, cellParent, eventSize, graph);
	}
	
	public static FlowControl createFlowControlHandleGatewayVertext(FlowElement flowElement, int gatewaySize, Object cellParent, mxGraph graph, Map<String, Object> generatedVertices) {
		return new FlowControl(flowElement, gatewaySize, cellParent, graph, generatedVertices);
	}
	
	public static FlowControl createFlowControlHandleActivity(FlowElement flowElement, Map<String, Object> generatedVertices, Object cellParent, mxGraph graph, int taskWidth, int taskHeight) {
		return new FlowControl(flowElement, generatedVertices, cellParent, graph, taskWidth, taskHeight);
	}
	
	public static FlowControl createFlowControlHandleSubProcess(FlowElement flowElement, BpmnAutoLayout bpmnAutoLayout, Object cellParent, mxGraph graph, Map<String, Object> generatedVertices, int subProcessMargin) {
		return new FlowControl(flowElement, bpmnAutoLayout, cellParent, graph, generatedVertices, subProcessMargin);
	}
	
	private void ensureSequenceFlowIdSet(FlowElement sequenceFlow) {
		// We really must have ids for sequence flow to be able to generate
		// stuff
		if (sequenceFlow.getId() == null) {
			sequenceFlow.setId("sequenceFlow-" + UUID.randomUUID().toString());
		}
	}

	public void handleSequenceFlow() {
		ensureSequenceFlowIdSet(flowElement);
		sequencesFlow.put(flowElement.getId(), (SequenceFlow) flowElement);
	}

	public void handleEvent() {
		// Boundary events are an exception to the general way of drawing an
		// event
		if (flowElement instanceof BoundaryEvent) {
			boundaryEvents.add((BoundaryEvent) flowElement);
		} else {
			createEventVertex(flowElement);
		}
	}

	private void createEventVertex(FlowElement flowElement) {
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
	
	public void createGatewayVertex() {
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
	
	public void handleActivity(FlowElement flowElement) {
		Object activityVertex = graph.insertVertex(cellParent, flowElement.getId(), "", 0, 0, taskWidth, taskHeight);
		generatedVertices.put(flowElement.getId(), activityVertex);
	}
	
	public void handleSubProcess() {
		double subProcessWidth = bpmnAutoLayout.getGraph().getView().getGraphBounds().getWidth();
		double subProcessHeight = bpmnAutoLayout.getGraph().getView().getGraphBounds().getHeight();
		Object subProcessVertex = graph.insertVertex(cellParent, flowElement.getId(), "", 0, 0, subProcessWidth + 2 * subProcessMargin, subProcessHeight + 2 * subProcessMargin);
		generatedVertices.put(flowElement.getId(), subProcessVertex);
	}
	
}
