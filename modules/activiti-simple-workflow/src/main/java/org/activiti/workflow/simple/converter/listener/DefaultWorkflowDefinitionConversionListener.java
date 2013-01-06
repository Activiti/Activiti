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

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.WorkflowDefinition;

/**
 * @author Joram Barrez
 */
public class DefaultWorkflowDefinitionConversionListener implements WorkflowDefinitionConversionListener
{
    
    private static final String START_EVENT_ID = "start";
    
    private static final String END_EVENT_ID = "end";
    
    public void beforeStepsConversion(WorkflowDefinitionConversion conversion)
    {
        initializeProcess(conversion);
    }
    
    protected void initializeProcess(WorkflowDefinitionConversion conversion)
    {
        WorkflowDefinition workflowDefinition = conversion.getWorkflowDefinition();
        
        // Create new process
        Process process = conversion.getProcess();
        process.setId(generateProcessId(workflowDefinition));
        process.setName(workflowDefinition.getName());
        process.setDocumentation(workflowDefinition.getDescription());

        conversion.setProcess(process);
        
        // Add start-event
        StartEvent startEvent = new StartEvent();
        startEvent.setId(START_EVENT_ID);
        process.addFlowElement(startEvent);
        conversion.setLastActivityId(startEvent.getId());
    }
    
    /**
     * @param workflowDefinition
     * @return process definition id that is randomized, to avoid name clashes (eg. amongst differnt tenants)
     */
    protected String generateProcessId(WorkflowDefinition workflowDefinition) 
    {
        return workflowDefinition.getName().replace(" ", "_");
    }
    
    public void afterStepsConversion(WorkflowDefinitionConversion conversion)
    {
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
    }

}
