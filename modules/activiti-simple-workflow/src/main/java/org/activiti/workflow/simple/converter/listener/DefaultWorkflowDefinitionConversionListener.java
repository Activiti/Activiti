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
package org.activiti.workflow.simple.converter.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversionFactory;
import org.activiti.workflow.simple.definition.WorkflowDefinition;

/**
 * Default listener for {@link WorkflowDefinitionConversion} lifecycle events.
 * 
 * When added to a {@link WorkflowDefinitionConversionFactory}, this class will
 * make sure a start event and end event is created for the {@link Process}.
 * Further, it will generate a correct incoming and outgoing sequence flow list
 * for each {@link Process} element, as required by some toolings (eg Modeler).
 * 
 * @author Joram Barrez
 */
public class DefaultWorkflowDefinitionConversionListener implements WorkflowDefinitionConversionListener {

  private static final long serialVersionUID = -145978383684020118L;

  private static final String START_EVENT_ID = "start";

  private static final String END_EVENT_ID = "end";

  public void beforeStepsConversion(WorkflowDefinitionConversion conversion) {
    initializeProcess(conversion);
  }

  protected void initializeProcess(WorkflowDefinitionConversion conversion) {
    WorkflowDefinition workflowDefinition = conversion.getWorkflowDefinition();

    // Create new process
    Process process = conversion.getProcess();
    process.setId(generateProcessId(workflowDefinition));
    process.setName(workflowDefinition.getName());
    process.setDocumentation(workflowDefinition.getDescription());
    
    if (workflowDefinition.getCategory() != null) {
    	conversion.getBpmnModel().setTargetNamespace(workflowDefinition.getCategory());
    }

    conversion.setProcess(process);

    // Add start-event
    StartEvent startEvent = new StartEvent();
    startEvent.setId(START_EVENT_ID);
    
    if(workflowDefinition.getStartFormDefinition() != null && workflowDefinition.getStartFormDefinition().getFormKey() != null) {
    	startEvent.setFormKey(workflowDefinition.getStartFormDefinition().getFormKey());
    }
    
    process.addFlowElement(startEvent);
    conversion.setLastActivityId(startEvent.getId());
  }

  /**
   * @param workflowDefinition
   * @return process definition id that is randomized, to avoid name clashes
   *         (eg. amongst differnt tenants)
   */
  protected String generateProcessId(WorkflowDefinition workflowDefinition) {
  	String processId = null;
  	if(workflowDefinition.getId() != null) {
  		processId = workflowDefinition.getId();
  	} else {
  		// Revert to using the name of the process
  		if(workflowDefinition.getName() != null) {
  			processId = workflowDefinition.getName().replace(" ", "_");
  		}
  	}
  	return processId;
  }

  public void afterStepsConversion(WorkflowDefinitionConversion conversion) {
    // Add end-event to process
    Process process = conversion.getProcess();

    EndEvent endEvent = new EndEvent();
    endEvent.setId(END_EVENT_ID);
    process.addFlowElement(endEvent);

    // Sequence flow from last created activity to end
    SequenceFlow sequenceFlow = new SequenceFlow();
    sequenceFlow.setId(conversion.getUniqueNumberedId(ConversionConstants.DEFAULT_SEQUENCEFLOW_PREFIX));
    sequenceFlow.setSourceRef(conversion.getLastActivityId());
    sequenceFlow.setTargetRef(END_EVENT_ID);
    process.addFlowElement(sequenceFlow);

    // To make the generated workflow compatible with some tools (eg the
    // Modeler, but also others),
    // We must add the ingoing and outgoing sequence flow to each of the flow
    // nodes
    SequenceFlowMapping sequenceFlowMapping = generateSequenceflowMappings(process);

    for (FlowNode flowNode : process.findFlowElementsOfType(FlowNode.class)) {
      List<SequenceFlow> incomingSequenceFlow = sequenceFlowMapping.getIncomingSequenceFlowMapping().get(flowNode.getId());
      if (incomingSequenceFlow != null) {
        flowNode.setIncomingFlows(incomingSequenceFlow);
      }

      List<SequenceFlow> outgoingSequenceFlow = sequenceFlowMapping.getOutgoingSequenceFlowMapping().get(flowNode.getId());
      if (outgoingSequenceFlow != null) {
        flowNode.setOutgoingFlows(outgoingSequenceFlow);
      }
    }
  }

  protected SequenceFlowMapping generateSequenceflowMappings(Process process) {
    HashMap<String, List<SequenceFlow>> incomingSequenceFlowMapping = new HashMap<String, List<SequenceFlow>>();
    HashMap<String, List<SequenceFlow>> outgoingSequenceFlowMapping = new HashMap<String, List<SequenceFlow>>();

    for (FlowElement flowElement : process.findFlowElementsOfType(SequenceFlow.class)) {
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

    SequenceFlowMapping mapping = new SequenceFlowMapping();
    mapping.setIncomingSequenceFlowMapping(incomingSequenceFlowMapping);
    mapping.setOutgoingSequenceFlowMapping(outgoingSequenceFlowMapping);
    return mapping;
  }

  static class SequenceFlowMapping {

    protected HashMap<String, List<SequenceFlow>> incomingSequenceFlowMapping;
    protected HashMap<String, List<SequenceFlow>> outgoingSequenceFlowMapping;

    public HashMap<String, List<SequenceFlow>> getIncomingSequenceFlowMapping() {
      return incomingSequenceFlowMapping;
    }

    public void setIncomingSequenceFlowMapping(HashMap<String, List<SequenceFlow>> incomingSequenceFlowMapping) {
      if (incomingSequenceFlowMapping != null) {
        this.incomingSequenceFlowMapping = incomingSequenceFlowMapping;
      } else {
        this.incomingSequenceFlowMapping = new HashMap<String, List<SequenceFlow>>();
      }
    }

    public HashMap<String, List<SequenceFlow>> getOutgoingSequenceFlowMapping() {
      return outgoingSequenceFlowMapping;
    }

    public void setOutgoingSequenceFlowMapping(HashMap<String, List<SequenceFlow>> outgoingSequenceFlowMapping) {
      if (outgoingSequenceFlowMapping != null) {
        this.outgoingSequenceFlowMapping = outgoingSequenceFlowMapping;
      } else {
        this.outgoingSequenceFlowMapping = new HashMap<String, List<SequenceFlow>>();
      }
    }

  }

}
