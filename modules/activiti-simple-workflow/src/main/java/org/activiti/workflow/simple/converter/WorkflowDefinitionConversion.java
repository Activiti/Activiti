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
package org.activiti.workflow.simple.converter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ActivitiException;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.activiti.workflow.simple.diagram.WorkflowDIGenerator;

/**
 * Context that holds all artifacts and meta-data required when converting
 * {@link WorkflowDefinition}s into required artifacts for deployment.
 * 
 * @see StepDefinitionConverter
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class WorkflowDefinitionConversion {

  // Input
  protected WorkflowDefinition workflowDefinition;

  // Artifacts of the conversion
  protected BpmnModel bpmnModel;
  protected Process process;
  protected Map<String, Object> additionalArtifacts;

  // Helper members
  protected WorkflowDefinitionConversionFactory conversionFactory;
  protected String lastActivityId;
  protected HashMap<String, Integer> incrementalIdMapping;
  
  protected boolean sequenceflowGenerationEnabled = true;
  protected boolean updateLastActivityEnabled = true;

  /* package */WorkflowDefinitionConversion(WorkflowDefinitionConversionFactory factory) {
    this.conversionFactory = factory;
  }
  
  /* package */
  public WorkflowDefinitionConversion(WorkflowDefinitionConversionFactory factory, WorkflowDefinition workflowDefinition) {
    this(factory);
    this.workflowDefinition = workflowDefinition;
  }

  public void convert() {
    
    if (workflowDefinition == null) {
      throw new ActivitiException("Cannot start conversion: need to set a WorkflowDefinition first!");
    }
    
    this.incrementalIdMapping = new HashMap<String, Integer>();
    this.additionalArtifacts = new HashMap<String, Object>();
    
    // Create new process
    bpmnModel = new BpmnModel();
    process = new Process();
    bpmnModel.addProcess(process);

    // Let conversion listeners know initialization is finished
    for (WorkflowDefinitionConversionListener conversionListener : conversionFactory.getWorkflowDefinitionConversionListeners()) {
      conversionListener.beforeStepsConversion(this);
    }

    // Convert each step
   convertSteps(workflowDefinition.getSteps());

    // Let conversion listeners know step conversion is done
    for (WorkflowDefinitionConversionListener conversionListener : conversionFactory.getWorkflowDefinitionConversionListeners()) {
      conversionListener.afterStepsConversion(this);
    }
    
    // Add DI information to bpmn model
    WorkflowDIGenerator workflowDIGenerator = new WorkflowDIGenerator(workflowDefinition, bpmnModel);
    workflowDIGenerator.generateDI();
  }
  
  public void convertSteps(List<StepDefinition> stepDefinitions) {
    for (StepDefinition step : stepDefinitions) {
      conversionFactory.getStepConverterFor(step).convertStepDefinition(step, this);
    }
  }
  
  /**
   * @param baseName
   *          base name of the unique identifier
   * @return a string that can be used as a unique id. Eg. if a baseName with
   *         value "userTask" is passed, the first time "userTask1" will be
   *         returned. When called agian with the same baseName, "userTask2" is
   *         returned. Counts are incremented for each baseName independently
   *         withing this context instance.
   */
  public String getUniqueNumberedId(String baseName) {
    Integer index = incrementalIdMapping.get(baseName);
    if (index == null) {
      index = 1;
      incrementalIdMapping.put(baseName, index);
    } else {
      index = index + 1;
      incrementalIdMapping.put(baseName, index);
    }
    return baseName + index;
  }
  
  /**
   * @return id of the activity that is at the end of the current process. Used
   *         to add additional steps and sequence-flows to the process.
   */
  public String getLastActivityId() {
    return this.lastActivityId;
  }

  /**
   * @param lastActivityId
   *          id of the activity that is at the end of the current process. Used
   *          to add additional steps and sequence-flows to the process.
   */
  public void setLastActivityId(String lastActivityId) {
    this.lastActivityId = lastActivityId;
  }
  
  public BpmnModel getBpmnModel() {
    return bpmnModel;
  }
  
  public void setBpmnModel(BpmnModel bpmnModel) {
    this.bpmnModel = bpmnModel;
  }

  public Process getProcess() {
    return process;
  }

  public void setProcess(Process process) {
    this.process = process;
  }
  
  public Object getArtifact(String artifactKey) {
    return additionalArtifacts.get(artifactKey);
  }

  public void setArtifact(String artifactKey, Object artifact) {
    additionalArtifacts.put(artifactKey, artifact);
  }

  public WorkflowDefinition getWorkflowDefinition() {
    return workflowDefinition;
  }
  
  public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
    this.workflowDefinition = workflowDefinition;
  }
  
  public boolean isSequenceflowGenerationEnabled() {
    return sequenceflowGenerationEnabled;
  }
  
  public void setSequenceflowGenerationEnabled(boolean sequenceflowGenerationEnabled) {
    this.sequenceflowGenerationEnabled = sequenceflowGenerationEnabled;
  }
  
  public boolean isUpdateLastActivityEnabled() {
    return updateLastActivityEnabled;
  }
  
  public void setUpdateLastActivityEnabled(boolean updateLastActivityEnabled) {
    this.updateLastActivityEnabled = updateLastActivityEnabled;
  }

  public String getbpm20Xml() {
    BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
    return new String(bpmnXMLConverter.convertToXML(bpmnModel));
  }
  
  public InputStream getWorkflowDiagramImage() {
    WorkflowDIGenerator workflowDIGenerator = new WorkflowDIGenerator(workflowDefinition, bpmnModel);
    return workflowDIGenerator.generateDiagram();
  }
  
}
