package org.activiti.workflow.simple.util;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;


public class BpmnModelUtil {
  
  public static List<FlowElement> findSucessorFlowElementsFor(Process process, FlowElement sourceFlowElement) {
    List<FlowElement> successors = new ArrayList<FlowElement>();
    for (SequenceFlow sequenceFlow : process.findFlowElementsOfType(SequenceFlow.class)) {
      if (sequenceFlow.getSourceRef().equals(sourceFlowElement.getId())) {
        successors.add(process.getFlowElement(sequenceFlow.getTargetRef()));
      }
    }
    return successors;
  }
}
