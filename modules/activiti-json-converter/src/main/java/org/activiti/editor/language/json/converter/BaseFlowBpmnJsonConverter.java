package org.activiti.editor.language.json.converter;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ConnectionFlow;
import org.activiti.bpmn.model.GraphicInfo;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

public abstract class BaseFlowBpmnJsonConverter extends BaseBpmnJsonConverter {
	/** Get graphic info for connection node
	 * 
	 * @param connectionFlow
	 * @param flowNode
	 */
	protected void getFlowGraphicInfo(ConnectionFlow connectionFlow, ObjectNode flowNode, BpmnModel bpmnModel, double subProcessX, double subProcessY) {
	    ArrayNode dockersArrayNode = objectMapper.createArrayNode();
	    ObjectNode dockNode = objectMapper.createObjectNode();
	    
	    GraphicInfo sourceGraphicInfo = bpmnModel.getGraphicInfo(connectionFlow.getSourceRef());
	    
	    dockNode.put(EDITOR_BOUNDS_X, sourceGraphicInfo.getWidth() / 2.0);
	    dockNode.put(EDITOR_BOUNDS_Y, sourceGraphicInfo.getHeight() / 2.0);
	    dockersArrayNode.add(dockNode);
	    
	    if (bpmnModel.getFlowLocationGraphicInfo(connectionFlow.getId()).size() > 2) {
	      for (int i = 1; i < bpmnModel.getFlowLocationGraphicInfo(connectionFlow.getId()).size() - 1; i++) {
	        GraphicInfo graphicInfo =  bpmnModel.getFlowLocationGraphicInfo(connectionFlow.getId()).get(i);
	        dockNode = objectMapper.createObjectNode();
	        dockNode.put(EDITOR_BOUNDS_X, graphicInfo.getX());
	        dockNode.put(EDITOR_BOUNDS_Y, graphicInfo.getY());
	        dockersArrayNode.add(dockNode);
	      }
	    }
	    
	    dockNode = objectMapper.createObjectNode();
	    dockNode.put(EDITOR_BOUNDS_X, bpmnModel.getGraphicInfo(connectionFlow.getTargetRef()).getWidth() / 2.0);
	    dockNode.put(EDITOR_BOUNDS_Y, bpmnModel.getGraphicInfo(connectionFlow.getTargetRef()).getHeight() / 2.0);
	    dockersArrayNode.add(dockNode);
	    flowNode.put(EDITOR_DOCKERS, dockersArrayNode);
		
	}
}
