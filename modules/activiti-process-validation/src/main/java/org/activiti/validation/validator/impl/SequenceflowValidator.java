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
package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.activiti.validation.validator.ProcessLevelValidator;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jbarrez
 */
public class SequenceflowValidator extends ProcessLevelValidator {

	@Override
	protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
		List<SequenceFlow> sequenceFlows = process.findFlowElementsOfType(SequenceFlow.class);
		for (SequenceFlow sequenceFlow : sequenceFlows) {
			
			String sourceRef = sequenceFlow.getSourceRef();
			String targetRef = sequenceFlow.getTargetRef();
			
			if (StringUtils.isEmpty(sourceRef)) {
				addError(errors, Problems.SEQ_FLOW_INVALID_SRC, process, sequenceFlow, "Invalid source for sequenceflow");
			}
			if (StringUtils.isEmpty(targetRef)) {
				addError(errors, Problems.SEQ_FLOW_INVALID_TARGET, process, sequenceFlow, "Invalid target for sequenceflow");
			}
			
		  // Implicit check: sequence flow cannot cross (sub) process boundaries, hence we check the parent and not the process
			// (could be subprocess for example)
			FlowElement source = process.getFlowElementRecursive(sourceRef);
			FlowElement target = process.getFlowElementRecursive(targetRef);
			
			// Src and target validation
			if (source == null) {
				addError(errors, Problems.SEQ_FLOW_INVALID_SRC, process, sequenceFlow, "Invalid source for sequenceflow");
			}
			if (target == null) {
				addError(errors, Problems.SEQ_FLOW_INVALID_TARGET, process, sequenceFlow, "Invalid target for sequenceflow");
			}
			
			if (source != null && target != null) {
  			FlowElementsContainer sourceContainer = process.getFlowElementsContainerRecursive(source.getId());
  			FlowElementsContainer targetContainer = process.getFlowElementsContainerRecursive(target.getId());
  			
  			if (sourceContainer == null) {
          addError(errors, Problems.SEQ_FLOW_INVALID_SRC, process, sequenceFlow, "Invalid source for sequenceflow");
        }
        if (targetContainer == null) {
          addError(errors, Problems.SEQ_FLOW_INVALID_TARGET, process, sequenceFlow, "Invalid target for sequenceflow");
        }
        if (sourceContainer != null && targetContainer != null && sourceContainer.equals(targetContainer) == false) {
          addError(errors, Problems.SEQ_FLOW_INVALID_TARGET, process, sequenceFlow, "Invalid target for sequenceflow, the target isn't defined in the same scope as the source");
        }
			}
		}
	}

}
