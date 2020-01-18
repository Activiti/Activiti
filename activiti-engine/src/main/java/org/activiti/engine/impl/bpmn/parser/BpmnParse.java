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
package org.activiti.engine.impl.bpmn.parser;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.impl.ActivitiEventSupport;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.impl.util.io.ResourceStreamSource;
import org.activiti.engine.impl.util.io.StreamSource;
import org.activiti.engine.impl.util.io.StringStreamSource;
import org.activiti.engine.impl.util.io.UrlStreamSource;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specific parsing of one BPMN 2.0 XML file, created by the {@link BpmnParser}.
 * 


 */
public class BpmnParse implements BpmnXMLConstants {

  protected static final Logger LOGGER = LoggerFactory.getLogger(BpmnParse.class);

  public static final String PROPERTYNAME_INITIAL = "initial";
  public static final String PROPERTYNAME_INITIATOR_VARIABLE_NAME = "initiatorVariableName";
  public static final String PROPERTYNAME_CONDITION = "condition";
  public static final String PROPERTYNAME_CONDITION_TEXT = "conditionText";
  public static final String PROPERTYNAME_TIMER_DECLARATION = "timerDeclarations";
  public static final String PROPERTYNAME_ISEXPANDED = "isExpanded";
  public static final String PROPERTYNAME_START_TIMER = "timerStart";
  public static final String PROPERTYNAME_COMPENSATION_HANDLER_ID = "compensationHandler";
  public static final String PROPERTYNAME_IS_FOR_COMPENSATION = "isForCompensation";
  public static final String PROPERTYNAME_ERROR_EVENT_DEFINITIONS = "errorEventDefinitions";
  public static final String PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION = "eventDefinitions";

  protected String name;

  protected boolean validateSchema = true;
  protected boolean validateProcess = true;

  protected StreamSource streamSource;
  protected String sourceSystemId;

  protected BpmnModel bpmnModel;

  protected String targetNamespace;

  /** The deployment to which the parsed process definitions will be added. */
  protected DeploymentEntity deployment;

  /** The end result of the parsing: a list of process definition. */
  protected List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();

  /** A map for storing sequence flow based on their id during parsing. */
  protected Map<String, SequenceFlow> sequenceFlows;

  protected BpmnParseHandlers bpmnParserHandlers;

  protected ProcessDefinitionEntity currentProcessDefinition;

  protected Process currentProcess;

  protected FlowElement currentFlowElement;

  protected LinkedList<SubProcess> currentSubprocessStack = new LinkedList<SubProcess>();

  /**
   * Mapping containing values stored during the first phase of parsing since other elements can reference these messages.
   * 
   * All the map's elements are defined outside the process definition(s), which means that this map doesn't need to be re-initialized for each new process definition.
   */
  protected Map<String, String> prefixs = new HashMap<String, String>();

  // Factories
  protected ActivityBehaviorFactory activityBehaviorFactory;
  protected ListenerFactory listenerFactory;

  /**
   * Constructor to be called by the {@link BpmnParser}.
   */
  public BpmnParse(BpmnParser parser) {
    this.activityBehaviorFactory = parser.getActivityBehaviorFactory();
    this.listenerFactory = parser.getListenerFactory();
    this.bpmnParserHandlers = parser.getBpmnParserHandlers();
  }

  public BpmnParse deployment(DeploymentEntity deployment) {
    this.deployment = deployment;
    return this;
  }

  public BpmnParse execute() {
    try {

      ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      BpmnXMLConverter converter = new BpmnXMLConverter();

      boolean enableSafeBpmnXml = false;
      String encoding = null;
      if (processEngineConfiguration != null) {
        enableSafeBpmnXml = processEngineConfiguration.isEnableSafeBpmnXml();
        encoding = processEngineConfiguration.getXmlEncoding();
      }

      if (encoding != null) {
        bpmnModel = converter.convertToBpmnModel(streamSource, validateSchema, enableSafeBpmnXml, encoding);
      } else {
        bpmnModel = converter.convertToBpmnModel(streamSource, validateSchema, enableSafeBpmnXml);
      }

      // XSD validation goes first, then process/semantic validation
      if (validateProcess) {
        ProcessValidator processValidator = processEngineConfiguration.getProcessValidator();
        if (processValidator == null) {
          LOGGER.warn("Process should be validated, but no process validator is configured on the process engine configuration!");
        } else {
          List<ValidationError> validationErrors = processValidator.validate(bpmnModel);
          if (validationErrors != null && !validationErrors.isEmpty()) {

            StringBuilder warningBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();

            for (ValidationError error : validationErrors) {
              if (error.isWarning()) {
                warningBuilder.append(error.toString());
                warningBuilder.append("\n");
              } else {
                errorBuilder.append(error.toString());
                errorBuilder.append("\n");
              }
            }

            // Throw exception if there is any error
            if (errorBuilder.length() > 0) {
              throw new ActivitiException("Errors while parsing:\n" + errorBuilder.toString());
            }

            // Write out warnings (if any)
            if (warningBuilder.length() > 0) {
              LOGGER.warn("Following warnings encountered during process validation: " + warningBuilder.toString());
            }

          }
        }
      }
      
      bpmnModel.setSourceSystemId(sourceSystemId);
      bpmnModel.setEventSupport(new ActivitiEventSupport());

      // Validation successful (or no validation)

      // Attach logic to the processes (eg. map ActivityBehaviors to bpmn model elements)
      applyParseHandlers();

      // Finally, process the diagram interchange info
      processDI();

    } catch (Exception e) {
      if (e instanceof ActivitiException) {
        throw (ActivitiException) e;
      } else if (e instanceof XMLException) {
        throw (XMLException) e;
      } else {
        throw new ActivitiException("Error parsing XML", e);
      }
    }

    return this;
  }

  public BpmnParse name(String name) {
    this.name = name;
    return this;
  }

  public BpmnParse sourceInputStream(InputStream inputStream) {
    if (name == null) {
      name("inputStream");
    }
    setStreamSource(new InputStreamSource(inputStream));
    return this;
  }

  public BpmnParse sourceResource(String resource) {
    return sourceResource(resource, null);
  }

  public BpmnParse sourceUrl(URL url) {
    if (name == null) {
      name(url.toString());
    }
    setStreamSource(new UrlStreamSource(url));
    return this;
  }

  public BpmnParse sourceUrl(String url) {
    try {
      return sourceUrl(new URL(url));
    } catch (MalformedURLException e) {
      throw new ActivitiIllegalArgumentException("malformed url: " + url, e);
    }
  }

  public BpmnParse sourceResource(String resource, ClassLoader classLoader) {
    if (name == null) {
      name(resource);
    }
    setStreamSource(new ResourceStreamSource(resource, classLoader));
    return this;
  }

  public BpmnParse sourceString(String string) {
    if (name == null) {
      name("string");
    }
    setStreamSource(new StringStreamSource(string));
    return this;
  }

  protected void setStreamSource(StreamSource streamSource) {
    if (this.streamSource != null) {
      throw new ActivitiIllegalArgumentException("invalid: multiple sources " + this.streamSource + " and " + streamSource);
    }
    this.streamSource = streamSource;
  }
  
  public BpmnParse setSourceSystemId(String sourceSystemId) {
    this.sourceSystemId = sourceSystemId;
    return this;
  }

  /**
   * Parses the 'definitions' root element
   */
  protected void applyParseHandlers() {
    sequenceFlows = new HashMap<String, SequenceFlow>();
    for (Process process : bpmnModel.getProcesses()) {
      currentProcess = process;
      if (process.isExecutable()) {
        bpmnParserHandlers.parseElement(this, process);
      }
    }
  }

  public void processFlowElements(Collection<FlowElement> flowElements) {

    // Parsing the elements is done in a strict order of types,
    // as otherwise certain information might not be available when parsing
    // a certain type.

    // Using lists as we want to keep the order in which they are defined
    List<SequenceFlow> sequenceFlowToParse = new ArrayList<SequenceFlow>();
    List<BoundaryEvent> boundaryEventsToParse = new ArrayList<BoundaryEvent>();

    // Flow elements that depend on other elements are parse after the first run-through
    List<FlowElement> defferedFlowElementsToParse = new ArrayList<FlowElement>();

    // Activities are parsed first
    for (FlowElement flowElement : flowElements) {

      // Sequence flow are also flow elements, but are only parsed once every activity is found
      if (flowElement instanceof SequenceFlow) {
        sequenceFlowToParse.add((SequenceFlow) flowElement);
      } else if (flowElement instanceof BoundaryEvent) {
        boundaryEventsToParse.add((BoundaryEvent) flowElement);
      } else if (flowElement instanceof Event) {
        defferedFlowElementsToParse.add(flowElement);
      } else {
        bpmnParserHandlers.parseElement(this, flowElement);
      }

    }

    // Deferred elements
    for (FlowElement flowElement : defferedFlowElementsToParse) {
      bpmnParserHandlers.parseElement(this, flowElement);
    }

    // Boundary events are parsed after all the regular activities are parsed
    for (BoundaryEvent boundaryEvent : boundaryEventsToParse) {
      bpmnParserHandlers.parseElement(this, boundaryEvent);
    }

    // sequence flows
    for (SequenceFlow sequenceFlow : sequenceFlowToParse) {
      bpmnParserHandlers.parseElement(this, sequenceFlow);
    }

  }

  // Diagram interchange
  // /////////////////////////////////////////////////////////////////

  public void processDI() {

    if (processDefinitions.isEmpty()) {
      return;
    }

    if (!bpmnModel.getLocationMap().isEmpty()) {

      // Verify if all referenced elements exist
      for (String bpmnReference : bpmnModel.getLocationMap().keySet()) {
        if (bpmnModel.getFlowElement(bpmnReference) == null) {
          // ACT-1625: don't warn when artifacts are referenced from DI
          if (bpmnModel.getArtifact(bpmnReference) == null) {
            // Check if it's a Pool or Lane, then DI is ok
            if (bpmnModel.getPool(bpmnReference) == null && bpmnModel.getLane(bpmnReference) == null) {
              LOGGER.warn("Invalid reference in diagram interchange definition: could not find " + bpmnReference);
            }
          }
        } else if (!(bpmnModel.getFlowElement(bpmnReference) instanceof FlowNode)) {
          LOGGER.warn("Invalid reference in diagram interchange definition: " + bpmnReference + " does not reference a flow node");
        }
      }
      
      for (String bpmnReference : bpmnModel.getFlowLocationMap().keySet()) {
        if (bpmnModel.getFlowElement(bpmnReference) == null) {
          // ACT-1625: don't warn when artifacts are referenced from DI
          if (bpmnModel.getArtifact(bpmnReference) == null) {
            LOGGER.warn("Invalid reference in diagram interchange definition: could not find " + bpmnReference);
          }
        } else if (!(bpmnModel.getFlowElement(bpmnReference) instanceof SequenceFlow)) {
          LOGGER.warn("Invalid reference in diagram interchange definition: " + bpmnReference + " does not reference a sequence flow");
        }
      }

      for (Process process : bpmnModel.getProcesses()) {
        if (!process.isExecutable()) {
          continue;
        }

        // Parse diagram interchange information
        ProcessDefinitionEntity processDefinition = getProcessDefinition(process.getId());
        if (processDefinition != null) {
          processDefinition.setGraphicalNotationDefined(true);
          
          for (String edgeId : bpmnModel.getFlowLocationMap().keySet()) {
            if (bpmnModel.getFlowElement(edgeId) != null) {
              createBPMNEdge(edgeId, bpmnModel.getFlowLocationGraphicInfo(edgeId));
            }
          }
        }
      }
    }
  }

  public void createBPMNEdge(String key, List<GraphicInfo> graphicList) {
    FlowElement flowElement = bpmnModel.getFlowElement(key);
    if (flowElement instanceof SequenceFlow) {
      SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
      List<Integer> waypoints = new ArrayList<Integer>();
      for (GraphicInfo waypointInfo : graphicList) {
        waypoints.add((int) waypointInfo.getX());
        waypoints.add((int) waypointInfo.getY());
      }
      sequenceFlow.setWaypoints(waypoints);
      
    } else if (bpmnModel.getArtifact(key) != null) {
      // it's an association, so nothing to do
    } else {
      LOGGER.warn("Invalid reference in 'bpmnElement' attribute, sequenceFlow " + key + " not found");
    }
  }

  public ProcessDefinitionEntity getProcessDefinition(String processDefinitionKey) {
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      if (processDefinition.getKey().equals(processDefinitionKey)) {
        return processDefinition;
      }
    }
    return null;
  }

  /*
   * ------------------- GETTERS AND SETTERS -------------------
   */

  public boolean isValidateSchema() {
    return validateSchema;
  }

  public void setValidateSchema(boolean validateSchema) {
    this.validateSchema = validateSchema;
  }

  public boolean isValidateProcess() {
    return validateProcess;
  }

  public void setValidateProcess(boolean validateProcess) {
    this.validateProcess = validateProcess;
  }

  public List<ProcessDefinitionEntity> getProcessDefinitions() {
    return processDefinitions;
  }

  public String getTargetNamespace() {
    return targetNamespace;
  }

  public BpmnParseHandlers getBpmnParserHandlers() {
    return bpmnParserHandlers;
  }

  public void setBpmnParserHandlers(BpmnParseHandlers bpmnParserHandlers) {
    this.bpmnParserHandlers = bpmnParserHandlers;
  }

  public DeploymentEntity getDeployment() {
    return deployment;
  }

  public void setDeployment(DeploymentEntity deployment) {
    this.deployment = deployment;
  }

  public BpmnModel getBpmnModel() {
    return bpmnModel;
  }

  public void setBpmnModel(BpmnModel bpmnModel) {
    this.bpmnModel = bpmnModel;
  }

  public ActivityBehaviorFactory getActivityBehaviorFactory() {
    return activityBehaviorFactory;
  }

  public void setActivityBehaviorFactory(ActivityBehaviorFactory activityBehaviorFactory) {
    this.activityBehaviorFactory = activityBehaviorFactory;
  }

  public ListenerFactory getListenerFactory() {
    return listenerFactory;
  }

  public void setListenerFactory(ListenerFactory listenerFactory) {
    this.listenerFactory = listenerFactory;
  }

  public Map<String, SequenceFlow> getSequenceFlows() {
    return sequenceFlows;
  }

  public ProcessDefinitionEntity getCurrentProcessDefinition() {
    return currentProcessDefinition;
  }

  public void setCurrentProcessDefinition(ProcessDefinitionEntity currentProcessDefinition) {
    this.currentProcessDefinition = currentProcessDefinition;
  }

  public FlowElement getCurrentFlowElement() {
    return currentFlowElement;
  }

  public void setCurrentFlowElement(FlowElement currentFlowElement) {
    this.currentFlowElement = currentFlowElement;
  }

  public Process getCurrentProcess() {
    return currentProcess;
  }

  public void setCurrentProcess(Process currentProcess) {
    this.currentProcess = currentProcess;
  }

  public void setCurrentSubProcess(SubProcess subProcess) {
    currentSubprocessStack.push(subProcess);
  }

  public SubProcess getCurrentSubProcess() {
    return currentSubprocessStack.peek();
  }

  public void removeCurrentSubProcess() {
    currentSubprocessStack.pop();
  }
}
