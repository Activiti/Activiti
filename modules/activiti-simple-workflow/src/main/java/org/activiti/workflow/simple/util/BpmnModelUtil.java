package org.activiti.workflow.simple.util;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;


public class BpmnModelUtil {
  
  public static List<FlowElement> findSucessorFlowElementsFor(Process process, FlowElement sourceFlowElement) {
    List<FlowElement> successors = new ArrayList<FlowElement>();
    for (SequenceFlow sequenceFlow : findFlowElementsOfType(process, SequenceFlow.class)) {
      if (sequenceFlow.getSourceRef().equals(sourceFlowElement.getId())) {
        successors.add(process.getFlowElement(sequenceFlow.getTargetRef()));
      }
    }
    return successors;
  }
  
  @SuppressWarnings("unchecked")
  public static <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsOfType(Process process, Class<FlowElementType> type) {
    List<FlowElementType> flowElements = new ArrayList<FlowElementType>();
    for (FlowElement flowElement : process.getFlowElements()) {
      if (type.isInstance(flowElement)) {
        flowElements.add((FlowElementType) flowElement);
      }
    }
    return flowElements;
  }

}
