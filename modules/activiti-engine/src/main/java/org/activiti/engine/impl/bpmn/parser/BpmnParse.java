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
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.DataAssociation;
import org.activiti.bpmn.model.DataSpec;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Import;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.Interface;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.TerminateEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.Transaction;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.parse.Problem;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventBasedGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.WebServiceActivityBehavior;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.bpmn.data.Assignment;
import org.activiti.engine.impl.bpmn.data.ClassStructureDefinition;
import org.activiti.engine.impl.bpmn.data.Data;
import org.activiti.engine.impl.bpmn.data.DataRef;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.bpmn.data.ItemDefinition;
import org.activiti.engine.impl.bpmn.data.ItemKind;
import org.activiti.engine.impl.bpmn.data.SimpleDataInputAssociation;
import org.activiti.engine.impl.bpmn.data.StructureDefinition;
import org.activiti.engine.impl.bpmn.data.TransformationDataOutputAssociation;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti.engine.impl.bpmn.webservice.BpmnInterface;
import org.activiti.engine.impl.bpmn.webservice.BpmnInterfaceImplementation;
import org.activiti.engine.impl.bpmn.webservice.MessageDefinition;
import org.activiti.engine.impl.bpmn.webservice.MessageImplicitDataInputAssociation;
import org.activiti.engine.impl.bpmn.webservice.MessageImplicitDataOutputAssociation;
import org.activiti.engine.impl.bpmn.webservice.Operation;
import org.activiti.engine.impl.bpmn.webservice.OperationImplementation;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.form.DefaultStartFormHandler;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationType;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.HasDIBounds;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.impl.util.io.ResourceStreamSource;
import org.activiti.engine.impl.util.io.StreamSource;
import org.activiti.engine.impl.util.io.StringStreamSource;
import org.activiti.engine.impl.util.io.UrlStreamSource;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specific parsing of one BPMN 2.0 XML file, created by the {@link BpmnParser}.
 * 
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class BpmnParse implements BpmnXMLConstants {

  protected static final Logger LOGGER = LoggerFactory.getLogger(BpmnParse.class);

  public static final String PROPERTYNAME_DOCUMENTATION = "documentation";
  public static final String PROPERTYNAME_INITIAL = "initial";
  public static final String PROPERTYNAME_INITIATOR_VARIABLE_NAME = "initiatorVariableName";
  public static final String PROPERTYNAME_CONDITION = "condition";
  public static final String PROPERTYNAME_CONDITION_TEXT = "conditionText";
  public static final String PROPERTYNAME_VARIABLE_DECLARATIONS = "variableDeclarations";
  public static final String PROPERTYNAME_TIMER_DECLARATION = "timerDeclarations";
  public static final String PROPERTYNAME_ISEXPANDED = "isExpanded";
  public static final String PROPERTYNAME_START_TIMER = "timerStart";
  public static final String PROPERTYNAME_COMPENSATION_HANDLER_ID = "compensationHandler";
  public static final String PROPERTYNAME_IS_FOR_COMPENSATION = "isForCompensation";
  public static final String PROPERTYNAME_ERROR_EVENT_DEFINITIONS = "errorEventDefinitions";
  public static final String PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION = "eventDefinitions";

  /* process start authorization specific finals */
  protected static final String POTENTIAL_STARTER = "potentialStarter";
  protected static final String CANDIDATE_STARTER_USERS_EXTENSION = "candidateStarterUsers";
  protected static final String CANDIDATE_STARTER_GROUPS_EXTENSION = "candidateStarterGroups";
  
  protected static final String ATTRIBUTEVALUE_T_FORMAL_EXPRESSION = BpmnParser.BPMN20_NS + ":tFormalExpression";
  
  protected String name;
  protected StreamSource streamSource;
  protected BpmnModel bpmnModel;
  protected String targetNamespace;

  /** The deployment to which the parsed process definitions will be added. */
  protected DeploymentEntity deployment;

  /** The end result of the parsing: a list of process definition. */
  protected List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();
  
  /** A map for storing sequence flow based on their id during parsing. */
  protected Map<String, TransitionImpl> sequenceFlows;

  /**
   * Mapping containing values stored during the first phase of parsing since
   * other elements can reference these messages.
   * 
   * All the map's elements are defined outside the process definition(s), which
   * means that this map doesn't need to be re-initialized for each new process
   * definition.
   */
  protected Map<String, MessageDefinition> messages = new HashMap<String, MessageDefinition>();
  protected Map<String, StructureDefinition> structures = new HashMap<String, StructureDefinition>();
  protected Map<String, BpmnInterfaceImplementation> interfaceImplementations = new HashMap<String, BpmnInterfaceImplementation>();
  protected Map<String, OperationImplementation> operationImplementations = new HashMap<String, OperationImplementation>();
  protected Map<String, ItemDefinition> itemDefinitions = new HashMap<String, ItemDefinition>();
  protected Map<String, BpmnInterface> bpmnInterfaces = new HashMap<String, BpmnInterface>();
  protected Map<String, Operation> operations = new HashMap<String, Operation>();
  protected Map<String, XMLImporter> importers = new HashMap<String, XMLImporter>();
  protected Map<String, String> prefixs = new HashMap<String, String>();

  // Members
  protected ExpressionManager expressionManager;
  protected List<BpmnParseListener> parseListeners;
  protected ActivityBehaviorFactory activityBehaviorFactory;
  protected ListenerFactory listenerFactory;

  /**
   * Constructor to be called by the {@link BpmnParser}.
   */
  public BpmnParse(BpmnParser parser) {
    this.expressionManager = parser.getExpressionManager();
    this.parseListeners = parser.getParseListeners();
    this.activityBehaviorFactory = parser.getActivityBehaviorFactory();
    this.listenerFactory = parser.getListenerFactory();
    this.initializeXSDItemDefinitions();
  }

  protected void initializeXSDItemDefinitions() {
    this.itemDefinitions.put("http://www.w3.org/2001/XMLSchema:string", new ItemDefinition("http://www.w3.org/2001/XMLSchema:string",
            new ClassStructureDefinition(String.class)));
  }

  public BpmnParse deployment(DeploymentEntity deployment) {
    this.deployment = deployment;
    return this;
  }

  public BpmnParse execute() {
    BpmnXMLConverter converter = new BpmnXMLConverter();
    XMLInputFactory xif = XMLInputFactory.newInstance();
    try {
      InputStreamReader in = new InputStreamReader(streamSource.getInputStream(), "UTF-8");
      XMLStreamReader xtr = xif.createXMLStreamReader(in);
      bpmnModel = converter.convertToBpmnModel(xtr);
      createImports();
      createItemDefinitions();
      createMessages();
      createOperations();
      transformProcessDefinitions();
    } catch (Exception e) {
      throw new ActivitiException("Error parsing XML", e);
    }
    
    if (bpmnModel.getProblems().size() > 0) {
      StringBuilder problemBuilder = new StringBuilder();
      for (Problem error : bpmnModel.getProblems()) {
        problemBuilder.append(error.toString());
        problemBuilder.append("\n");
      }
      throw new ActivitiException("Errors while parsing:\n" + problemBuilder.toString());
    }
    
    return this;
  }
  
  public BpmnParse name(String name) {
    this.name = name;
    return this;
  }
  
  public BpmnParse sourceInputStream(InputStream inputStream) {
    if (name==null) {
      name("inputStream");
    }
    setStreamSource(new InputStreamSource(inputStream)); 
    return this;
  }

  public BpmnParse sourceResource(String resource) {
    return sourceResource(resource, null);
  }

  public BpmnParse sourceUrl(URL url) {
    if (name==null) {
      name(url.toString());
    }
    setStreamSource(new UrlStreamSource(url));
    return this;
  }
  
  public BpmnParse sourceUrl(String url) {
    try {
      return sourceUrl(new URL(url));
    } catch (MalformedURLException e) {
      throw new ActivitiException("malformed url: "+url, e);
    }
  }
  
  public BpmnParse sourceResource(String resource, ClassLoader classLoader) {
    if (name==null) {
      name(resource);
    }
    setStreamSource(new ResourceStreamSource(resource, classLoader)); 
    return this;
  }

  public BpmnParse sourceString(String string) {
    if (name==null) {
      name("string");
    }
    setStreamSource(new StringStreamSource(string)); 
    return this;
  }

  protected void setStreamSource(StreamSource streamSource) {
    if (this.streamSource!=null) {
      throw new ActivitiException("invalid: multiple sources "+this.streamSource+" and "+streamSource);
    }
    this.streamSource = streamSource;
  }
  
  protected void createImports() {
    for (Import theImport : bpmnModel.getImports()) {
      XMLImporter importer = this.getImporter(theImport);
      if (importer == null) {
        bpmnModel.addProblem("Could not import item of type " + theImport.getImportType(), theImport);
      } else {
        importer.importFrom(theImport, this);
      }
    }
  }

  protected XMLImporter getImporter(Import theImport) {
    if (this.importers.containsKey(theImport.getImportType())) {
      return this.importers.get(theImport.getImportType());
    } else {
      if (theImport.getImportType().equals("http://schemas.xmlsoap.org/wsdl/")) {
        Class< ? > wsdlImporterClass;
        try {
          wsdlImporterClass = Class.forName("org.activiti.engine.impl.webservice.CxfWSDLImporter", true, Thread.currentThread().getContextClassLoader());
          XMLImporter newInstance = (XMLImporter) wsdlImporterClass.newInstance();
          this.importers.put(theImport.getImportType(), newInstance);
          return newInstance;
        } catch (Exception e) {
          bpmnModel.addProblem("Could not find importer for type " + theImport.getImportType(), theImport);
        }
      }
      return null;
    }
  }
  
  public void createMessages() {
    for (Message messageElement : bpmnModel.getMessages()) {
      MessageDefinition messageDefinition = new MessageDefinition(messageElement.getId(), name);
      if (StringUtils.isNotEmpty(messageElement.getItemRef())) {
        if (!this.itemDefinitions.containsKey(messageElement.getItemRef())) {
            bpmnModel.addProblem(messageElement.getItemRef() + " does not exist", messageElement);          
        } else {
            ItemDefinition itemDefinition = this.itemDefinitions.get(messageElement.getItemRef());
            messageDefinition.setItemDefinition(itemDefinition);
        }
      }
      this.messages.put(messageDefinition.getId(), messageDefinition);
      
    }
  }
  
  protected void createItemDefinitions() {
    for (org.activiti.bpmn.model.ItemDefinition itemDefinitionElement : bpmnModel.getItemDefinitions().values()) {
      StructureDefinition structure = null;

      try {
        // it is a class
        Class< ? > classStructure = ReflectUtil.loadClass(itemDefinitionElement.getStructureRef());
        structure = new ClassStructureDefinition(classStructure);
      } catch (ActivitiException e) {
        // it is a reference to a different structure
        structure = this.structures.get(itemDefinitionElement.getStructureRef());
      }

      ItemDefinition itemDefinition = new ItemDefinition(itemDefinitionElement.getId(), structure);
      if (StringUtils.isNotEmpty(itemDefinitionElement.getItemKind())) {
        itemDefinition.setItemKind(ItemKind.valueOf(itemDefinitionElement.getItemKind()));
      }
      itemDefinitions.put(itemDefinition.getId(), itemDefinition);
    }
  }
  
  protected void createOperations() {
    for (Interface interfaceObject : bpmnModel.getInterfaces()) {
      BpmnInterface bpmnInterface = new BpmnInterface(interfaceObject.getId(), interfaceObject.getName());
      bpmnInterface.setImplementation(this.interfaceImplementations.get(interfaceObject.getImplementationRef()));
      
      for (org.activiti.bpmn.model.Operation operationObject : interfaceObject.getOperations()) {
        if (!this.messages.containsKey(operationObject.getInMessageRef())) {
          bpmnModel.addProblem(operationObject.getInMessageRef() + " does not exist", operationObject);
        } else {
          MessageDefinition inMessage = this.messages.get(operationObject.getInMessageRef());
          Operation operation = new Operation(operationObject.getId(), operationObject.getName(), bpmnInterface, inMessage);
          operation.setImplementation(this.operationImplementations.get(operationObject.getImplementationRef()));
    
          if (StringUtils.isNotEmpty(operationObject.getOutMessageRef())) {
            if (this.messages.containsKey(operationObject.getOutMessageRef())) {
              MessageDefinition outMessage = this.messages.get(operationObject.getOutMessageRef());
              operation.setOutMessage(outMessage);
            }
          }
    
          operations.put(operation.getId(), operation);
        }
      }
    }
  }

  /**
   * Parses the 'definitions' root element
   */
  protected void transformProcessDefinitions() {
    sequenceFlows = new HashMap<String, TransitionImpl>();
    for (Process process : bpmnModel.getProcesses()) {
      if (process.isExecutable() == false) {
        LOGGER.info("Ignoring non-executable process with id='" + process.getId() + "'. Set the attribute isExecutable=\"true\" to deploy this process.");
      } else {
        processDefinitions.add(transformProcess(process));
      }
    }
    
    for (BpmnParseListener parseListener : parseListeners) {
      //parseListener.parseRootElement(rootElement, getProcessDefinitions());
    }
    
    if (processDefinitions.size() > 0) {
      processDI();
    }
  }
  
  /**
   * Parses one process (ie anything inside a &lt;process&gt; element).
   * 
   * @param process
   *          The 'process' object.
   * @return The parsed version of the XML: a {@link ProcessDefinitionImpl}
   *         object.
   */
  public ProcessDefinitionEntity transformProcess(Process process) {
    ProcessDefinitionEntity processDefinition = new ProcessDefinitionEntity();

    /*
     * Mapping object model - bpmn xml: processDefinition.id -> generated by
     * activiti engine processDefinition.key -> bpmn id (required)
     * processDefinition.name -> bpmn name (optional)
     */
    processDefinition.setKey(process.getId());
    processDefinition.setName(process.getName());
    processDefinition.setCategory(bpmnModel.getTargetNamespace());
    processDefinition.setDescription(process.getDocumentation()); 
    processDefinition.setProperty(PROPERTYNAME_DOCUMENTATION, process.getDocumentation()); // Kept for backwards compatibility. See ACT-1020
    processDefinition.setTaskDefinitions(new HashMap<String, TaskDefinition>());
    processDefinition.setDeploymentId(deployment.getId());
    createExecutionListenersOnScope(process.getExecutionListeners(), processDefinition);
    
    for (String candidateUser : process.getCandidateStarterUsers()) {
      processDefinition.addCandidateStarterUserIdExpression(expressionManager.createExpression(candidateUser));
    }
    
    for (String candidateGroup : process.getCandidateStarterGroups()) {
      processDefinition.addCandidateStarterGroupIdExpression(expressionManager.createExpression(candidateGroup));
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Parsing process {}", processDefinition.getKey());
    }
    
    processFlowElements(process.getFlowElements(), processDefinition, null);
    processArtifacts(process.getArtifacts(), processDefinition);
    
    if (process.getIoSpecification() != null) {
      IOSpecification ioSpecification = createIOSpecification(process.getIoSpecification());
      processDefinition.setIoSpecification(ioSpecification);
    }
    
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseProcess(process, processDefinition);
    }
    
    return processDefinition;
  }
  
  protected void processFlowElements(Collection<FlowElement> flowElements, ScopeImpl scope, SubProcess subProcess) {
    // activities
    for (FlowElement flowElement : flowElements) {
      
      if (flowElement instanceof SequenceFlow) continue;
      
      ActivityImpl activity = null;
      if (flowElement instanceof StartEvent) {
        createStartEvent((StartEvent) flowElement, scope, subProcess);
      } else if (flowElement instanceof EndEvent) {
        createEndEvent((EndEvent) flowElement, scope);
      } else if (flowElement instanceof ServiceTask) {
        activity = createServiceTask((ServiceTask) flowElement, scope);
      } else if (flowElement instanceof UserTask) {
        activity = createUserTask((UserTask) flowElement, scope);
      } else if (flowElement instanceof BusinessRuleTask) {
        activity = createBusinessRuleTask((BusinessRuleTask) flowElement, scope);
      } else if (flowElement instanceof ScriptTask) {
        activity = createScriptTask((ScriptTask) flowElement, scope);
      } else if (flowElement instanceof Transaction) {
        activity = createTransaction((Transaction) flowElement, scope);
      } else if (flowElement instanceof SubProcess) {
        activity = createSubProcess((SubProcess) flowElement, scope);
      } else if (flowElement instanceof CallActivity) {
        activity = createCallActivity((CallActivity) flowElement, scope);
      } else if (flowElement instanceof ManualTask) {
        activity = createManualTask((ManualTask) flowElement, scope);
      } else if (flowElement instanceof ReceiveTask) {
        activity = createReceiveTask((ReceiveTask) flowElement, scope);
      } else if (flowElement instanceof SendTask) {
        activity = createSendTask((SendTask) flowElement, scope);
      } else if (flowElement instanceof Task) {
        activity = createTask((Task) flowElement, scope);
      } else if (flowElement instanceof EventGateway) {
        activity = createEventBasedGateway((EventGateway) flowElement, scope);
      } else if (flowElement instanceof ExclusiveGateway) {
        activity = createExclusiveGateway((ExclusiveGateway) flowElement, scope);
      } else if (flowElement instanceof InclusiveGateway) {
        activity = createInclusiveGateway((InclusiveGateway) flowElement, scope);
      } else if (flowElement instanceof ParallelGateway) {
        activity = createParallelGateway((ParallelGateway) flowElement, scope);
      } else if (flowElement instanceof IntermediateCatchEvent) {
        activity = createIntermediateCatchEvent((IntermediateCatchEvent) flowElement, scope);
      } else if (flowElement instanceof ThrowEvent) {
        activity = createIntermediateThrowEvent((ThrowEvent) flowElement, scope);
      }
      
      if (flowElement instanceof Activity) {
        createMultiInstanceLoopCharacteristics((org.activiti.bpmn.model.Activity) flowElement, activity);      
      } 
    }
    
    // boundary events
    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof BoundaryEvent) {
        createBoundaryEvent((BoundaryEvent) flowElement, scope);
      }
    }
    
    // sequence flows
    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof SequenceFlow) {
        createSequenceFlow((SequenceFlow) flowElement, scope);
      }
    }
    
    // validations after complete model
    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof ExclusiveGateway) {
        ActivityImpl gatewayActivity = scope.findActivity(flowElement.getId());
        validateExclusiveGateway(gatewayActivity, (ExclusiveGateway) flowElement);
      }
    }
  }
  
  protected void processArtifacts(Collection<Artifact> artifacts, ScopeImpl scope) {
    // associations  
    for (Artifact artifact : artifacts) {
      if (artifact instanceof Association) {
        createAssociation((Association) artifact, scope);
      }
    }
  }

  /**
   * Parses the start events of a certain level in the process (process,
   * subprocess or another scope).
   * 
   * @param parentElement
   *          The 'parent' element that contains the start events (process,
   *          subprocess).
   * @param scope
   *          The {@link ScopeImpl} to which the start events must be added.
   */
  protected void createStartEvent(StartEvent startEvent, ScopeImpl scope, SubProcess subProcess) {
    
    ActivityImpl startEventActivity = createActivityOnScope(startEvent, ELEMENT_EVENT_START, scope);

    if (scope instanceof ProcessDefinitionEntity) {        
      createProcessDefinitionStartEvent(startEventActivity, startEvent, (ProcessDefinitionEntity) scope);
    } else {
      createScopeStartEvent(startEventActivity, startEvent, scope, subProcess);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseStartEvent(startEvent, scope, startEventActivity);
    }
    createExecutionListenersOnScope(startEvent.getExecutionListeners(), startEventActivity);      
    
    if(scope instanceof ProcessDefinitionEntity) {
      selectInitial(startEventActivity, startEvent, (ProcessDefinitionEntity) scope);
      createStartFormHandlers(startEvent, (ProcessDefinitionEntity) scope);
    }
  }

  protected void selectInitial(ActivityImpl startEventActivity, StartEvent startEvent, ProcessDefinitionEntity processDefinition) {
    
    if (processDefinition.getInitial() == null) {
      processDefinition.setInitial(startEventActivity);
    } else {
      // validate that there is s single none start event / timer start event:
      if (startEventActivity.getProperty("type").equals("messageStartEvent") == false) {
        String currentInitialType = (String) processDefinition.getInitial().getProperty("type");
        if (currentInitialType.equals("messageStartEvent")) {
          processDefinition.setInitial(startEventActivity);
        } else {
          bpmnModel.addProblem("multiple none start events or timer start events not supported on process definition.", startEvent);
        }
      }
    }
  }

  protected void createProcessDefinitionStartEvent(ActivityImpl startEventActivity, StartEvent startEvent, ProcessDefinitionEntity processDefinition) {
    if (StringUtils.isNotEmpty(startEvent.getInitiator())) {
      processDefinition.setProperty(PROPERTYNAME_INITIATOR_VARIABLE_NAME, startEvent.getInitiator());
    }

    // all start events share the same behavior:
    startEventActivity.setActivityBehavior(activityBehaviorFactory.createNoneStartEventActivityBehavior(startEvent));
    if (startEvent.getEventDefinitions().size() > 0) {
      EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
      if (eventDefinition instanceof TimerEventDefinition) {
        createTimerStartEventDefinition((TimerEventDefinition) eventDefinition, startEventActivity, processDefinition);
        
      } else if (eventDefinition instanceof MessageEventDefinition) {
        MessageEventDefinition messageDefinition = (MessageEventDefinition) eventDefinition;
        if (bpmnModel.containsMessageId(messageDefinition.getMessageRef())) {
          String messageName = bpmnModel.getMessage(messageDefinition.getMessageRef()).getName();
          if (StringUtils.isEmpty(messageName)) {
            bpmnModel.addProblem("messageName is required for a message event", startEvent);
          }
          messageDefinition.setMessageRef(messageName);
        }
        EventSubscriptionDeclaration eventSubscription = new EventSubscriptionDeclaration(messageDefinition.getMessageRef(), "message");
        startEventActivity.setProperty("type", "messageStartEvent");
        eventSubscription.setActivityId(startEventActivity.getId());
        // create message event subscription:      
        eventSubscription.setStartEvent(true);
        addEventSubscriptionDeclaration(eventSubscription, messageDefinition, processDefinition);
      }
    }
  }
  
  protected void createStartFormHandlers(StartEvent startEvent, ProcessDefinitionEntity processDefinition) {
    if (processDefinition.getInitial() != null) {
      if (startEvent.getId().equals(processDefinition.getInitial().getId())) {
        StartFormHandler startFormHandler = new DefaultStartFormHandler();
        startFormHandler.parseConfiguration(startEvent.getFormProperties(), startEvent.getFormKey(), deployment, processDefinition);
        processDefinition.setStartFormHandler(startFormHandler);
      }
    }
  }

  protected void createScopeStartEvent(ActivityImpl startEventActivity, StartEvent startEvent, ScopeImpl scope, SubProcess subProcess) {

    Object triggeredByEvent = scope.getProperty("triggeredByEvent");
    boolean isTriggeredByEvent = triggeredByEvent != null && ((Boolean) triggeredByEvent == true);
    
    if (isTriggeredByEvent) { // event subprocess
      
      // all start events of an event subprocess share common behavior
      EventSubProcessStartEventActivityBehavior activityBehavior = 
              activityBehaviorFactory.createEventSubProcessStartEventActivityBehavior(startEvent, startEventActivity.getId()); 
      startEventActivity.setActivityBehavior(activityBehavior);
      
      // the scope of the event subscription is the parent of the event
      // subprocess (subscription must be created when parent is initialized)
      ScopeImpl catchingScope = ((ActivityImpl) scope).getParent();
      
      if (startEvent.getEventDefinitions().size() > 0) {
        EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
        
        if (eventDefinition instanceof org.activiti.bpmn.model.ErrorEventDefinition) {
          
          if (scope.getProperty(PROPERTYNAME_INITIAL) == null) {
              scope.setProperty(PROPERTYNAME_INITIAL, startEventActivity);
              org.activiti.bpmn.model.ErrorEventDefinition modelErrorEvent = (org.activiti.bpmn.model.ErrorEventDefinition) eventDefinition;
              if (bpmnModel.containsErrorRef(modelErrorEvent.getErrorCode())) {
                String errorCode = bpmnModel.getErrors().get(modelErrorEvent.getErrorCode());
                if (StringUtils.isEmpty(errorCode)) {
                  bpmnModel.addProblem("errorCode is required for an error event", startEvent);
                }
                modelErrorEvent.setErrorCode(errorCode);
              }
              createErrorStartEventDefinition(modelErrorEvent, startEventActivity, catchingScope);
            } else {
              bpmnModel.addProblem("multiple start events not supported for subprocess", subProcess);
            }
          
        } else if (eventDefinition instanceof MessageEventDefinition) {
          MessageEventDefinition messageDefinition = (MessageEventDefinition) eventDefinition;
          if (bpmnModel.containsMessageId(messageDefinition.getMessageRef())) {
            String messageName = bpmnModel.getMessage(messageDefinition.getMessageRef()).getName();
            if (StringUtils.isEmpty(messageName)) {
              bpmnModel.addProblem("messageName is required for a message event", startEvent);
            }
            messageDefinition.setMessageRef(messageName);
          }
          EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(messageDefinition.getMessageRef(), "message");
          eventSubscriptionDeclaration.setActivityId(startEventActivity.getId());
          eventSubscriptionDeclaration.setStartEvent(false);
          addEventSubscriptionDeclaration(eventSubscriptionDeclaration, messageDefinition, catchingScope);
          
        } else if (eventDefinition instanceof SignalEventDefinition) {
          SignalEventDefinition signalDefinition = (SignalEventDefinition) eventDefinition;
          if (bpmnModel.containsSignalId(signalDefinition.getSignalRef())) {
            String signalName = bpmnModel.getSignal(signalDefinition.getSignalRef()).getName();
            if (StringUtils.isEmpty(signalName)) {
              bpmnModel.addProblem("signalName is required for a signal event", startEvent);
            }
            signalDefinition.setSignalRef(signalName);
          }
          EventSubscriptionDeclaration eventSubscriptionDeclaration = new EventSubscriptionDeclaration(signalDefinition.getSignalRef(), "signal");
          eventSubscriptionDeclaration.setActivityId(startEventActivity.getId());
          eventSubscriptionDeclaration.setStartEvent(false);
          addEventSubscriptionDeclaration(eventSubscriptionDeclaration, signalDefinition, catchingScope);
        
        } else {
          bpmnModel.addProblem("start event of event subprocess must be of type 'error', 'message' or 'signal' ", startEvent);
        }
      }
      
    } else { // "regular" subprocess
      
      if(startEvent.getEventDefinitions().size() > 0) {
        bpmnModel.addProblem("event definitions only allowed on start event if subprocess is an event subprocess", startEvent);
      }
      if (scope.getProperty(PROPERTYNAME_INITIAL) == null) {
        scope.setProperty(PROPERTYNAME_INITIAL, startEventActivity);
        startEventActivity.setActivityBehavior(activityBehaviorFactory.createNoneStartEventActivityBehavior(startEvent));
      } else {
        bpmnModel.addProblem("multiple start events not supported for subprocess", subProcess);
      }
    }

  }

  protected void createErrorStartEventDefinition(org.activiti.bpmn.model.ErrorEventDefinition errorEventDefinition, ActivityImpl startEventActivity, ScopeImpl scope) {  
    ErrorEventDefinition definition = new ErrorEventDefinition(startEventActivity.getId());
    if (StringUtils.isNotEmpty(errorEventDefinition.getErrorCode())) {
      definition.setErrorCode(errorEventDefinition.getErrorCode());
    }
    definition.setPrecedence(10);
    addErrorEventDefinition(definition, scope);
  }
 
  @SuppressWarnings("unchecked")
  protected void addEventSubscriptionDeclaration(EventSubscriptionDeclaration subscription, EventDefinition parsedEventDefinition, ScopeImpl scope) {
    List<EventSubscriptionDeclaration> eventDefinitions = (List<EventSubscriptionDeclaration>) scope.getProperty(PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION);
    if(eventDefinitions == null) {
      eventDefinitions = new ArrayList<EventSubscriptionDeclaration>();
      scope.setProperty(PROPERTYNAME_EVENT_SUBSCRIPTION_DECLARATION, eventDefinitions);
    } else {
      // if this is a message event, validate that it is the only one with the provided name for this scope
      if(subscription.getEventType().equals("message")) {
        for (EventSubscriptionDeclaration eventDefinition : eventDefinitions) {
          if(eventDefinition.getEventType().equals("message")
            && eventDefinition.getEventName().equals(subscription.getEventName()) 
            && eventDefinition.isStartEvent() == subscription.isStartEvent()) {
            
            bpmnModel.addProblem("Cannot have more than one message event subscription with name '" + subscription.getEventName() +
                "' for scope '"+scope.getId()+"'", parsedEventDefinition);
          }
        }
      }
    }  
    eventDefinitions.add(subscription);
  }

  public void validateExclusiveGateway(ActivityImpl activity, ExclusiveGateway exclusiveGateway) {
    if (activity.getOutgoingTransitions().size() == 0) {
      // TODO: double check if this is valid (I think in Activiti yes, since we need start events we will need an end event as well)
      bpmnModel.addProblem("Exclusive Gateway '" + activity.getId() + "' has no outgoing sequence flows.", exclusiveGateway);      
    } else if (activity.getOutgoingTransitions().size() == 1) {
      PvmTransition flow = activity.getOutgoingTransitions().get(0);
      Condition condition = (Condition) flow.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
      if (condition!=null) {
        bpmnModel.addProblem("Exclusive Gateway '" + activity.getId() + "' has only one outgoing sequence flow ('" + flow.getId() + "'). This is not allowed to have a condition.", exclusiveGateway);
      }
    } else {    
      String defaultSequenceFlow = (String) activity.getProperty("default");
      boolean hasDefaultFlow = StringUtils.isNotEmpty(defaultSequenceFlow);
      
      ArrayList<PvmTransition> flowsWithoutCondition = new ArrayList<PvmTransition>();
      for (PvmTransition flow : activity.getOutgoingTransitions()) {
        Condition condition = (Condition) flow.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
        boolean isDefaultFlow = flow.getId()!=null && flow.getId().equals(defaultSequenceFlow);
        boolean hasConditon = condition!=null;

        if (!hasConditon && !isDefaultFlow) {
          flowsWithoutCondition.add(flow);
        }
        if (hasConditon && isDefaultFlow) {
          bpmnModel.addProblem("Exclusive Gateway '" + activity.getId() + "' has outgoing sequence flow '" + flow.getId() + "' which is the default flow but has a condition too.", exclusiveGateway);
        }
      }
      if (hasDefaultFlow || flowsWithoutCondition.size()>1) {
        // if we either have a default flow (then no flows without conditions are valid at all) or if we have more than one flow without condition this is an error 
        for (PvmTransition flow : flowsWithoutCondition) {          
          bpmnModel.addProblem("Exclusive Gateway '" + activity.getId() + "' has outgoing sequence flow '" + flow.getId() + "' without condition which is not the default flow.", exclusiveGateway);
        }        
      } else if (flowsWithoutCondition.size() == 1) {
        // Havinf no default and exactly one flow without condition this is considered the default one now (to not break backward compatibility)
        PvmTransition flow = flowsWithoutCondition.get(0);
        bpmnModel.addWarning("Exclusive Gateway '" + activity.getId() + "' has outgoing sequence flow '" + flow.getId() + 
            "' without condition which is not the default flow. We assume it to be the default flow, but it is bad modeling practice, better set the default flow in your gateway.", exclusiveGateway);
      }
    }
  }

  public ActivityImpl createIntermediateCatchEvent(IntermediateCatchEvent event, ScopeImpl scopeElement) {
    ActivityImpl nestedActivity = null;
    EventDefinition eventDefinition = null;
    if (event.getEventDefinitions().size() > 0) {
      eventDefinition = event.getEventDefinitions().get(0);
    }
   
    if (eventDefinition == null) {
      bpmnModel.addProblem("No event definition for intermediate catch event " + event.getId(), event);
      nestedActivity = createActivityOnScope(event, ELEMENT_EVENT_CATCH, scopeElement);
    } else {
      
      boolean isAfterEventBasedGateway = false;
      String eventBasedGatewayId = null;
      for (SequenceFlow sequenceFlow : event.getIncomingFlows()) {
        FlowElement sourceElement = bpmnModel.getFlowElement(sequenceFlow.getSourceRef());
        if (sourceElement instanceof EventGateway) {
          isAfterEventBasedGateway = true;
          eventBasedGatewayId = sourceElement.getId();
          break;
        }
      }
      
      if (isAfterEventBasedGateway) {
        ActivityImpl gatewayActivity = scopeElement.findActivity(eventBasedGatewayId);
        nestedActivity = createActivityOnScope(event, ELEMENT_EVENT_CATCH, gatewayActivity);
      } else {
        nestedActivity = createActivityOnScope(event, ELEMENT_EVENT_CATCH, scopeElement);
      }
      
      // Catch event behavior is the same for all types
      nestedActivity.setActivityBehavior(activityBehaviorFactory.createIntermediateCatchEventActivityBehavior(event));
      
      if (eventDefinition instanceof TimerEventDefinition) {
        createIntermediateTimerEventDefinition((TimerEventDefinition) eventDefinition, nestedActivity, isAfterEventBasedGateway);
      } else if (eventDefinition instanceof SignalEventDefinition) {
        createIntermediateSignalEventDefinition((SignalEventDefinition) eventDefinition, nestedActivity, isAfterEventBasedGateway);
      } else if (eventDefinition instanceof MessageEventDefinition) {
        createIntermediateMessageEventDefinition((MessageEventDefinition) eventDefinition, nestedActivity, isAfterEventBasedGateway);
      } else {
        bpmnModel.addProblem("Unsupported intermediate catch event type.", event);
      }
    }
    
    createExecutionListenersOnScope(event.getExecutionListeners(), nestedActivity);
    
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateCatchEvent(event, scopeElement, nestedActivity);
    }
    
    return nestedActivity;
  }
  

  protected void createIntermediateMessageEventDefinition(MessageEventDefinition messageEventDefinition, ActivityImpl nestedActivity, boolean isAfterEventBasedGateway) {
    
    nestedActivity.setProperty("type", "intermediateMessageCatch");   
    
    if (bpmnModel.containsMessageId(messageEventDefinition.getMessageRef())) {
      String messageName = bpmnModel.getMessage(messageEventDefinition.getMessageRef()).getName();
      if (StringUtils.isEmpty(messageName)) {
        bpmnModel.addProblem("messageName is required for a message event", messageEventDefinition);
      }
      messageEventDefinition.setMessageRef(messageName);
    }
    
    EventSubscriptionDeclaration messageDefinition = new EventSubscriptionDeclaration(messageEventDefinition.getMessageRef(), "message");
    if(isAfterEventBasedGateway) {
      messageDefinition.setActivityId(nestedActivity.getId());
      addEventSubscriptionDeclaration(messageDefinition, messageEventDefinition, nestedActivity.getParent());      
    } else {
      nestedActivity.setScope(true);
      addEventSubscriptionDeclaration(messageDefinition, messageEventDefinition, nestedActivity);   
    }
    
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateMessageCatchEventDefinition(messageEventDefinition, nestedActivity);
    }
  }

  public ActivityImpl createIntermediateThrowEvent(ThrowEvent intermediateEvent, ScopeImpl scopeElement) {
    ActivityImpl nestedActivityImpl = createActivityOnScope(intermediateEvent, ELEMENT_EVENT_THROW, scopeElement);

    ActivityBehavior activityBehavior = null;
    EventDefinition eventDefinition = null;
    if (intermediateEvent.getEventDefinitions().size() > 0) {
      eventDefinition = intermediateEvent.getEventDefinitions().get(0);
    }
    
    if (eventDefinition instanceof SignalEventDefinition) {
      SignalEventDefinition signalEventDefinition = (SignalEventDefinition) eventDefinition;
      if (bpmnModel.containsSignalId(signalEventDefinition.getSignalRef())) {
        String signalName = bpmnModel.getSignal(signalEventDefinition.getSignalRef()).getName();
        if (StringUtils.isEmpty(signalName)) {
          bpmnModel.addProblem("signalName is required for a signal event", intermediateEvent);
        }
        signalEventDefinition.setSignalRef(signalName);
      }
      nestedActivityImpl.setProperty("type", "intermediateSignalThrow");  
      EventSubscriptionDeclaration signalDefinition = new EventSubscriptionDeclaration(signalEventDefinition.getSignalRef(), "signal");
      signalDefinition.setAsync(signalEventDefinition.isAsync());
      activityBehavior = activityBehaviorFactory.createIntermediateThrowSignalEventActivityBehavior(intermediateEvent, signalDefinition); 
    } else if (eventDefinition instanceof org.activiti.bpmn.model.CompensateEventDefinition) {
      CompensateEventDefinition compensateEventDefinition = createCompensateEventDefinition((org.activiti.bpmn.model.CompensateEventDefinition) eventDefinition, scopeElement);
      activityBehavior = activityBehaviorFactory.createIntermediateThrowCompensationEventActivityBehavior(intermediateEvent,compensateEventDefinition); 
      
    } else if (eventDefinition == null) {
      activityBehavior = activityBehaviorFactory.createIntermediateThrowNoneEventActivityBehavior(intermediateEvent); 
    } else { 
      bpmnModel.addProblem("Unsupported intermediate throw event type " + eventDefinition, intermediateEvent);
    }
    
    nestedActivityImpl.setActivityBehavior(activityBehavior);
    
    createExecutionListenersOnScope(intermediateEvent.getExecutionListeners(), nestedActivityImpl);
    
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateThrowEvent(intermediateEvent, scopeElement, nestedActivityImpl);
    }
    
    return nestedActivityImpl;
  }


  protected CompensateEventDefinition createCompensateEventDefinition(org.activiti.bpmn.model.CompensateEventDefinition eventDefinition, ScopeImpl scopeElement) {
    if(StringUtils.isNotEmpty(eventDefinition.getActivityRef())) {
      if(scopeElement.findActivity(eventDefinition.getActivityRef()) == null) {
        bpmnModel.addProblem("Invalid attribute value for 'activityRef': no activity with id '" + eventDefinition.getActivityRef() +
            "' in current scope " + scopeElement.getId(), eventDefinition);
      }
    }
    
    CompensateEventDefinition compensateEventDefinition =  new CompensateEventDefinition();
    compensateEventDefinition.setActivityRef(eventDefinition.getActivityRef());
    compensateEventDefinition.setWaitForCompletion(eventDefinition.isWaitForCompletion());
    
    return compensateEventDefinition;
  }

  protected void createCatchCompensateEventDefinition(org.activiti.bpmn.model.CompensateEventDefinition compensateEventDefinition, ActivityImpl activity) {   
    activity.setProperty("type", "compensationBoundaryCatch");
    
    ScopeImpl parent = activity.getParent();
    for (ActivityImpl child : parent.getActivities()) {
      if (child.getProperty("type").equals("compensationBoundaryCatch") && child != activity ) {
        bpmnModel.addProblem("multiple boundary events with compensateEventDefinition not supported on same activity.", compensateEventDefinition);        
      }
    }
  }
  
  protected ActivityBehavior createBoundaryCancelEventDefinition(CancelEventDefinition cancelEventDefinition, ActivityImpl activity) {
    activity.setProperty("type", "cancelBoundaryCatch");
    
    ActivityImpl parent = (ActivityImpl) activity.getParent();
    if(!parent.getProperty("type").equals("transaction")) {
      bpmnModel.addProblem("boundary event with cancelEventDefinition only supported on transaction subprocesses.", cancelEventDefinition);
    }
    
    for (ActivityImpl child : parent.getActivities()) {
      if(child.getProperty("type").equals("cancelBoundaryCatch")
        && child != activity ) {
        bpmnModel.addProblem("multiple boundary events with cancelEventDefinition not supported on same transaction subprocess.", cancelEventDefinition);        
      }
    }
    
    return activityBehaviorFactory.createCancelBoundaryEventActivityBehavior(cancelEventDefinition);
  }

  /**
   * Parses loopCharacteristics (standardLoop/Multi-instance) of an activity, if
   * any is defined.
   */
  public void createMultiInstanceLoopCharacteristics(org.activiti.bpmn.model.Activity modelActivity, ActivityImpl activity) {
    
    MultiInstanceActivityBehavior miActivityBehavior = null;
    
    MultiInstanceLoopCharacteristics loopCharacteristics = modelActivity.getLoopCharacteristics();
    if (loopCharacteristics == null) {
      // nothing to do
      return;
    }
    
    // Activity Behavior
    if (loopCharacteristics.isSequential()) {
      miActivityBehavior = activityBehaviorFactory.createSequentialMultiInstanceBehavior(
              activity, (AbstractBpmnActivityBehavior) activity.getActivityBehavior()); 
    } else {
      miActivityBehavior = activityBehaviorFactory.createParallelMultiInstanceBehavior(
              activity, (AbstractBpmnActivityBehavior) activity.getActivityBehavior());
    }
    
    // ActivityImpl settings
    activity.setScope(true);
    activity.setProperty("multiInstance", loopCharacteristics.isSequential() ? "sequential" : "parallel");
    activity.setActivityBehavior(miActivityBehavior);
    
    
    // loopcardinality
    if (StringUtils.isNotEmpty(loopCharacteristics.getLoopCardinality())) {
      miActivityBehavior.setLoopCardinalityExpression(expressionManager.createExpression(loopCharacteristics.getLoopCardinality()));
    }
    
    // completion condition
    if (StringUtils.isNotEmpty(loopCharacteristics.getCompletionCondition())) {
      miActivityBehavior.setCompletionConditionExpression(expressionManager.createExpression(loopCharacteristics.getCompletionCondition()));
    }
    
    // activiti:collection
    if (StringUtils.isNotEmpty(loopCharacteristics.getInputDataItem())) {
      if (loopCharacteristics.getInputDataItem().contains("{")) {
        miActivityBehavior.setCollectionExpression(expressionManager.createExpression(loopCharacteristics.getInputDataItem()));
      } else {
        miActivityBehavior.setCollectionVariable(loopCharacteristics.getInputDataItem());
      }
    }

    // activiti:elementVariable
    if (StringUtils.isNotEmpty(loopCharacteristics.getElementVariable())) {
      miActivityBehavior.setCollectionElementVariable(loopCharacteristics.getElementVariable());
    }

    // Validation
    if (miActivityBehavior.getLoopCardinalityExpression() == null && miActivityBehavior.getCollectionExpression() == null
            && miActivityBehavior.getCollectionVariable() == null) {
      bpmnModel.addProblem("Either loopCardinality or loopDataInputRef/activiti:collection must been set.", loopCharacteristics);
    }

    // Validation
    if (miActivityBehavior.getCollectionExpression() == null && miActivityBehavior.getCollectionVariable() == null
            && miActivityBehavior.getCollectionElementVariable() != null) {
      bpmnModel.addProblem("LoopDataInputRef/activiti:collection must be set when using inputDataItem or activiti:elementVariable.", loopCharacteristics);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseMultiInstanceLoopCharacteristics(modelActivity, loopCharacteristics, activity);
    }
  }

  /**
   * Parses the generic information of an activity element (id, name,
   * documentation, etc.), and creates a new {@link ActivityImpl} on the given
   * scope element.
   */
  public ActivityImpl createActivityOnScope(FlowElement flowElement, String xmlLocalName, ScopeImpl scopeElement) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Parsing activity {}", flowElement.getId());
    }
    
    ActivityImpl activity = scopeElement.createActivity(flowElement.getId());

    activity.setProperty("name", flowElement.getName());
    activity.setProperty("documentation", flowElement.getDocumentation());
    if (flowElement instanceof Activity) {
      Activity modelActivity = (Activity) flowElement;
      activity.setProperty("default", modelActivity.getDefaultFlow());
      if(modelActivity.isForCompensation()) {
        activity.setProperty(PROPERTYNAME_IS_FOR_COMPENSATION, true);        
      }
    } else if (flowElement instanceof Gateway) {
      activity.setProperty("default", ((Gateway) flowElement).getDefaultFlow());
    }
    activity.setProperty("type", xmlLocalName);
    
    return activity;
  }

  public String parseDocumentation(Element element) {
    List<Element> docElements = element.elements("documentation");
    if (docElements.isEmpty()) {
      return null;
    }
    
    StringBuilder builder = new StringBuilder();
    for (Element e: docElements) {
      if (builder.length() != 0) {
        builder.append("\n\n");
      }
      
      builder.append(e.getText().trim());
    }
    
    return builder.toString();
  }

  /**
   * Parses an exclusive gateway declaration.
   */
  public ActivityImpl createExclusiveGateway(ExclusiveGateway gateway, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(gateway, ELEMENT_GATEWAY_EXCLUSIVE, scope);
    activity.setActivityBehavior(activityBehaviorFactory.createExclusiveGatewayActivityBehavior(gateway));

    createExecutionListenersOnScope(gateway.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseExclusiveGateway(gateway, scope, activity);
    }
    return activity;
  }

  /**
   * Parses an inclusive gateway declaration.
   */
  public ActivityImpl createInclusiveGateway(InclusiveGateway gateway, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(gateway, ELEMENT_GATEWAY_INCLUSIVE, scope);
    activity.setActivityBehavior(activityBehaviorFactory.createInclusiveGatewayActivityBehavior(gateway));

    createExecutionListenersOnScope(gateway.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseInclusiveGateway(gateway, scope, activity);
    }
    return activity;
  }
  
  public ActivityImpl createEventBasedGateway(EventGateway gateway, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(gateway, ELEMENT_GATEWAY_EVENT, scope);   
    activity.setActivityBehavior(activityBehaviorFactory.createEventBasedGatewayActivityBehavior(gateway));
    activity.setScope(true);

    createExecutionListenersOnScope(gateway.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseEventBasedGateway(gateway, scope, activity);
    }
    
    // find all outgoing sequence flows
    for (SequenceFlow sequenceFlow : gateway.getOutgoingFlows()) {
      FlowElement flowElement = bpmnModel.getFlowElement(sequenceFlow.getTargetRef());
      if (flowElement != null && flowElement instanceof IntermediateCatchEvent == false) {
        bpmnModel.addProblem("Event based gateway can only be connected to elements of type intermediateCatchEvent.", flowElement);
      }
    }
    
    return activity;
  }
  

  /**
   * Parses a parallel gateway declaration.
   */
  public ActivityImpl createParallelGateway(ParallelGateway gateway, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(gateway, ELEMENT_GATEWAY_PARALLEL, scope);
    activity.setActivityBehavior(activityBehaviorFactory.createParallelGatewayActivityBehavior(gateway));

    createExecutionListenersOnScope(gateway.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseParallelGateway(gateway, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a scriptTask declaration.
   */
  public ActivityImpl createScriptTask(ScriptTask scriptTask, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(scriptTask, ELEMENT_TASK_SCRIPT, scope);
    
    activity.setAsync(scriptTask.isAsynchronous());
    activity.setExclusive(!scriptTask.isNotExclusive());

    activity.setActivityBehavior(activityBehaviorFactory.createScriptTaskActivityBehavior(scriptTask));

    createExecutionListenersOnScope(scriptTask.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseScriptTask(scriptTask, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a serviceTask declaration.
   */
  public ActivityImpl createServiceTask(ServiceTask serviceTask, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(serviceTask, ELEMENT_TASK_SERVICE, scope);
    activity.setAsync(serviceTask.isAsynchronous());
    activity.setExclusive(!serviceTask.isNotExclusive());

    // Email, Mule and Shell service tasks
    if (StringUtils.isNotEmpty(serviceTask.getType())) {
      
      if (serviceTask.getType().equalsIgnoreCase("mail")) {
        validateFieldDeclarationsForEmail(serviceTask, serviceTask.getFieldExtensions());
        activity.setActivityBehavior(activityBehaviorFactory.createMailActivityBehavior(serviceTask));
        
      } else if (serviceTask.getType().equalsIgnoreCase("mule")) {
        activity.setActivityBehavior(activityBehaviorFactory.createMuleActivityBehavior(serviceTask, bpmnModel));
        
      } else if (serviceTask.getType().equalsIgnoreCase("shell")) {
        validateFieldDeclarationsForShell(serviceTask, serviceTask.getFieldExtensions());
        activity.setActivityBehavior(activityBehaviorFactory.createShellActivityBehavior(serviceTask));
        
      } else {
        bpmnModel.addProblem("Invalid usage of type attribute: '" + serviceTask.getType() + "'.", serviceTask);
      }

    // activiti:class
    } else if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(serviceTask.getImplementationType())) {
      activity.setActivityBehavior(activityBehaviorFactory.createClassDelegateServiceTask(serviceTask));
      
    // activiti:delegateExpression
    } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(serviceTask.getImplementationType())) {
      activity.setActivityBehavior(activityBehaviorFactory.createServiceTaskDelegateExpressionActivityBehavior(serviceTask));

    // activiti:expression      
    } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(serviceTask.getImplementationType())) {
      activity.setActivityBehavior(activityBehaviorFactory.createServiceTaskExpressionActivityBehavior(serviceTask));

    // Webservice   
    } else if (ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE.equalsIgnoreCase(serviceTask.getImplementationType()) && 
        StringUtils.isNotEmpty(serviceTask.getOperationRef())) {
      
      if (!this.operations.containsKey(serviceTask.getOperationRef())) {
        bpmnModel.addProblem(serviceTask.getOperationRef() + " does not exist", serviceTask);
      } else {
        
        WebServiceActivityBehavior webServiceActivityBehavior = activityBehaviorFactory.createWebServiceActivityBehavior(serviceTask);
        webServiceActivityBehavior.setOperation(this.operations.get(serviceTask.getOperationRef()));

        if (serviceTask.getIoSpecification() != null) {
          IOSpecification ioSpecification = this.createIOSpecification(serviceTask.getIoSpecification());
          webServiceActivityBehavior.setIoSpecification(ioSpecification);
        }

        for (DataAssociation dataAssociationElement : serviceTask.getDataInputAssociations()) {
          AbstractDataAssociation dataAssociation = this.createDataInputAssociation(dataAssociationElement);
          webServiceActivityBehavior.addDataInputAssociation(dataAssociation);
        }

        for (DataAssociation dataAssociationElement : serviceTask.getDataOutputAssociations()) {
          AbstractDataAssociation dataAssociation = this.createDataOutputAssociation(dataAssociationElement);
          webServiceActivityBehavior.addDataOutputAssociation(dataAssociation);
        }

        activity.setActivityBehavior(webServiceActivityBehavior);
      }
    } else {
      bpmnModel.addProblem("One of the attributes 'class', 'delegateExpression', 'type', 'operation', or 'expression' is mandatory on serviceTask.", serviceTask);
    }

    createExecutionListenersOnScope(serviceTask.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseServiceTask(serviceTask, scope, activity);
    }
    return activity;
  }
  
  protected IOSpecification createIOSpecification(org.activiti.bpmn.model.IOSpecification specificationModel) {
    IOSpecification ioSpecification = new IOSpecification();

    for (DataSpec dataInputElement : specificationModel.getDataInputs()) {
      ItemDefinition itemDefinition = this.itemDefinitions.get(dataInputElement.getItemSubjectRef());
      Data dataInput = new Data(this.targetNamespace + ":" + dataInputElement.getId(), dataInputElement.getId(), itemDefinition);
      ioSpecification.addInput(dataInput);
    }

    for (DataSpec dataOutputElement : specificationModel.getDataOutputs()) {
      ItemDefinition itemDefinition = this.itemDefinitions.get(dataOutputElement.getItemSubjectRef());
      Data dataOutput = new Data(this.targetNamespace + ":" + dataOutputElement.getId(), dataOutputElement.getId(), itemDefinition);
      ioSpecification.addOutput(dataOutput);
    }

    for (String dataInputRef : specificationModel.getDataInputRefs()) {
      DataRef dataRef = new DataRef(dataInputRef);
      ioSpecification.addInputRef(dataRef);
    }

    for (String dataOutputRef : specificationModel.getDataOutputRefs()) {
      DataRef dataRef = new DataRef(dataOutputRef);
      ioSpecification.addOutputRef(dataRef);
    }

    return ioSpecification;
  }
  
  protected AbstractDataAssociation createDataInputAssociation(DataAssociation dataAssociationElement) {
    if (StringUtils.isEmpty(dataAssociationElement.getTargetRef())) {
      bpmnModel.addProblem("targetRef is required", dataAssociationElement);
    }
    
    if (dataAssociationElement.getAssignments().isEmpty()) {
      return new MessageImplicitDataInputAssociation(dataAssociationElement.getSourceRef(), dataAssociationElement.getTargetRef());
    } else {
      SimpleDataInputAssociation dataAssociation = new SimpleDataInputAssociation(
          dataAssociationElement.getSourceRef(), dataAssociationElement.getTargetRef());

      for (org.activiti.bpmn.model.Assignment assigmentElement : dataAssociationElement.getAssignments()) {
        if (StringUtils.isNotEmpty(assigmentElement.getFrom()) && StringUtils.isNotEmpty(assigmentElement.getTo())) {
          Expression from = this.expressionManager.createExpression(assigmentElement.getFrom());
          Expression to = this.expressionManager.createExpression(assigmentElement.getTo());
          Assignment assignment = new Assignment(from, to);
          dataAssociation.addAssignment(assignment);
        }
      }
      return dataAssociation;
    }
  }
  
  protected AbstractDataAssociation createDataOutputAssociation(DataAssociation dataAssociationElement) {
    if (StringUtils.isNotEmpty(dataAssociationElement.getSourceRef())) {
      return new MessageImplicitDataOutputAssociation(dataAssociationElement.getTargetRef(), dataAssociationElement.getSourceRef());
    } else {
      Expression transformation = this.expressionManager.createExpression(dataAssociationElement.getTransformation());
      AbstractDataAssociation dataOutputAssociation = new TransformationDataOutputAssociation(null, dataAssociationElement.getTargetRef(), transformation);
      return dataOutputAssociation;
    }
  }

  /**
   * Parses a businessRuleTask declaration.
   */
  public ActivityImpl createBusinessRuleTask(BusinessRuleTask businessRuleTask, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(businessRuleTask, ELEMENT_TASK_BUSINESSRULE, scope);
      
    activity.setAsync(businessRuleTask.isAsynchronous());
    activity.setExclusive(!businessRuleTask.isNotExclusive());
    activity.setActivityBehavior(activityBehaviorFactory.createBusinessRuleTaskActivityBehavior(businessRuleTask));
    
    createExecutionListenersOnScope(businessRuleTask.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBusinessRuleTask(businessRuleTask, scope, activity);
    }
    
    return activity;
  }

  /**
   * Parses a sendTask declaration.
   */
  public ActivityImpl createSendTask(SendTask sendTask, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(sendTask, ELEMENT_TASK_SEND, scope);
    
    activity.setAsync(sendTask.isAsynchronous());
    activity.setExclusive(!sendTask.isNotExclusive());

    // for e-mail
    if (StringUtils.isNotEmpty(sendTask.getType())) {
      if (sendTask.getType().equalsIgnoreCase("mail")) {
        validateFieldDeclarationsForEmail(sendTask, sendTask.getFieldExtensions());
        activity.setActivityBehavior(activityBehaviorFactory.createMailActivityBehavior(sendTask));
      } else if (sendTask.getType().equalsIgnoreCase("mule")) {
        activity.setActivityBehavior(activityBehaviorFactory.createMuleActivityBehavior(sendTask, bpmnModel));
      } else {
        bpmnModel.addProblem("Invalid usage of type attribute: '" + sendTask.getType() + "'.", sendTask);
      }

      // for web service
    } else if (ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE.equalsIgnoreCase(sendTask.getImplementationType()) && 
        StringUtils.isNotEmpty(sendTask.getOperationRef())) {
      
      if (!this.operations.containsKey(sendTask.getOperationRef())) {
        bpmnModel.addProblem(sendTask.getOperationRef() + " does not exist", sendTask);
      } else {
        WebServiceActivityBehavior webServiceActivityBehavior = activityBehaviorFactory.createWebServiceActivityBehavior(sendTask);
        Operation operation = this.operations.get(sendTask.getOperationRef());
        webServiceActivityBehavior.setOperation(operation);

        if (sendTask.getIoSpecification() != null) {
          IOSpecification ioSpecification = this.createIOSpecification(sendTask.getIoSpecification());
          webServiceActivityBehavior.setIoSpecification(ioSpecification);
        }

        for (DataAssociation dataAssociationElement : sendTask.getDataInputAssociations()) {
          AbstractDataAssociation dataAssociation = this.createDataInputAssociation(dataAssociationElement);
          webServiceActivityBehavior.addDataInputAssociation(dataAssociation);
        }

        for (DataAssociation dataAssociationElement : sendTask.getDataOutputAssociations()) {
          AbstractDataAssociation dataAssociation = this.createDataOutputAssociation(dataAssociationElement);
          webServiceActivityBehavior.addDataOutputAssociation(dataAssociation);
        }

        activity.setActivityBehavior(webServiceActivityBehavior);
      }
    } else {
      bpmnModel.addProblem("One of the attributes 'type' or 'operation' is mandatory on sendTask.", sendTask);
    }

    createExecutionListenersOnScope(sendTask.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseSendTask(sendTask, scope, activity);
    }
    return activity;
  }

  protected AbstractDataAssociation parseDataOutputAssociation(Element dataAssociationElement) {
    String targetRef = dataAssociationElement.element("targetRef").getText();

    if (dataAssociationElement.element("sourceRef") != null) {
      String sourceRef = dataAssociationElement.element("sourceRef").getText();
      return new MessageImplicitDataOutputAssociation(targetRef, sourceRef);
    } else {
      Expression transformation = this.expressionManager.createExpression(dataAssociationElement.element("transformation").getText());
      AbstractDataAssociation dataOutputAssociation = new TransformationDataOutputAssociation(null, targetRef, transformation);
      return dataOutputAssociation;
    }
  }
  
  protected void validateFieldDeclarationsForEmail(Task task, List<FieldExtension> fieldExtensions) {
    boolean toDefined = false;
    boolean textOrHtmlDefined = false;
    
    for (FieldExtension fieldExtension : fieldExtensions) {
      if (fieldExtension.getFieldName().equals("to")) {
        toDefined = true;
      }
      if (fieldExtension.getFieldName().equals("html")) {
        textOrHtmlDefined = true;
      }
      if (fieldExtension.getFieldName().equals("text")) {
        textOrHtmlDefined = true;
      }
    }

    if (!toDefined) {
      bpmnModel.addProblem("No recipient is defined on the mail activity", task);
    }
    if (!textOrHtmlDefined) {
      bpmnModel.addProblem("Text or html field should be provided", task);
    }
  }

  protected void validateFieldDeclarationsForShell(Task task, List<FieldExtension> fieldExtensions) {
    boolean shellCommandDefined = false;

    for (FieldExtension fieldExtension : fieldExtensions) {
      String fieldName = fieldExtension.getFieldName();
      String fieldValue = fieldExtension.getStringValue();

      shellCommandDefined |= fieldName.equals("command");

      if ((fieldName.equals("wait") || fieldName.equals("redirectError") || fieldName.equals("cleanEnv")) && !fieldValue.toLowerCase().equals("true")
              && !fieldValue.toLowerCase().equals("false")) {
        bpmnModel.addProblem("undefined value for shell " + fieldName + " parameter :" + fieldValue.toString() + ".", task);
      }

    }

    if (!shellCommandDefined) {
      bpmnModel.addProblem("No shell command is defined on the shell activity", task);
    }
  }


  /**
   * Parses a task with no specific type (behaves as passthrough).
   */
  public ActivityImpl createTask(Task task, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(task, ELEMENT_TASK, scope);
    activity.setActivityBehavior(activityBehaviorFactory.createTaskActivityBehavior(task));
    
    activity.setAsync(task.isAsynchronous());
    activity.setExclusive(!task.isNotExclusive());

    createExecutionListenersOnScope(task.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseTask(task, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a manual task.
   */
  public ActivityImpl createManualTask(ManualTask manualTask, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(manualTask, ELEMENT_TASK_MANUAL, scope);
    activity.setActivityBehavior(activityBehaviorFactory.createManualTaskActivityBehavior(manualTask));
    
    activity.setAsync(manualTask.isAsynchronous());
    activity.setExclusive(!manualTask.isNotExclusive());

    createExecutionListenersOnScope(manualTask.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseManualTask(manualTask, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a receive task.
   */
  public ActivityImpl createReceiveTask(ReceiveTask receiveTask, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(receiveTask, ELEMENT_TASK_RECEIVE, scope);
    activity.setActivityBehavior(activityBehaviorFactory.createReceiveTaskActivityBehavior(receiveTask));
    
    activity.setAsync(receiveTask.isAsynchronous());
    activity.setExclusive(!receiveTask.isNotExclusive());

    createExecutionListenersOnScope(receiveTask.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseReceiveTask(receiveTask, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a userTask declaration.
   */
  public ActivityImpl createUserTask(UserTask userTask, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(userTask, ELEMENT_TASK_USER, scope);
    
    activity.setAsync(userTask.isAsynchronous());
    activity.setExclusive(!userTask.isNotExclusive()); 
    
    TaskDefinition taskDefinition = parseTaskDefinition(userTask, userTask.getId(), (ProcessDefinitionEntity) scope.getProcessDefinition());
    activity.setActivityBehavior(activityBehaviorFactory.createUserTaskActivityBehavior(userTask, taskDefinition));

    //parseProperties(userTaskElement, activity);
    createExecutionListenersOnScope(userTask.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseUserTask(userTask, scope, activity);
    }
    return activity;
  }

  public TaskDefinition parseTaskDefinition(UserTask userTask, String taskDefinitionKey, ProcessDefinitionEntity processDefinition) {
    TaskFormHandler taskFormHandler = new DefaultTaskFormHandler();
    taskFormHandler.parseConfiguration(userTask.getFormProperties(), userTask.getFormKey(), deployment, processDefinition);

    TaskDefinition taskDefinition = new TaskDefinition(taskFormHandler);

    taskDefinition.setKey(taskDefinitionKey);
    processDefinition.getTaskDefinitions().put(taskDefinitionKey, taskDefinition);

    if (StringUtils.isNotEmpty(userTask.getName())) {
      taskDefinition.setNameExpression(expressionManager.createExpression(userTask.getName()));
    }

    if (StringUtils.isNotEmpty(userTask.getDocumentation())) {
      taskDefinition.setDescriptionExpression(expressionManager.createExpression(userTask.getDocumentation()));
    }

    if (StringUtils.isNotEmpty(userTask.getAssignee())) {
      taskDefinition.setAssigneeExpression(expressionManager.createExpression(userTask.getAssignee()));
    }
    for (String candidateUser : userTask.getCandidateUsers()) {
      taskDefinition.addCandidateUserIdExpression(expressionManager.createExpression(candidateUser));
    }
    for (String candidateGroup : userTask.getCandidateGroups()) {
      taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(candidateGroup));
    }
    
    // Activiti custom extension
    
    // Task listeners
    for (ActivitiListener taskListener : userTask.getTaskListeners()) {
      taskDefinition.addTaskListener(taskListener.getEvent(), createTaskListener(taskListener, userTask.getId()));
    }

    // Due date
    if (StringUtils.isNotEmpty(userTask.getDueDate())) {
      taskDefinition.setDueDateExpression(expressionManager.createExpression(userTask.getDueDate()));
    }
    
    // Priority
    if (StringUtils.isNotEmpty(userTask.getPriority())) {
      taskDefinition.setPriorityExpression(expressionManager.createExpression(userTask.getPriority()));
    }

    return taskDefinition;
  }

  protected TaskListener createTaskListener(ActivitiListener activitiListener, String taskId) {
    TaskListener taskListener = null;

    if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
      taskListener = listenerFactory.createClassDelegateTaskListener(activitiListener); 
    } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
      taskListener = listenerFactory.createExpressionTaskListener(activitiListener);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
      taskListener = listenerFactory.createDelegateExpressionTaskListener(activitiListener);
    } else {
      bpmnModel.addProblem("Element 'class', 'expression' or 'delegateExpression' is mandatory on taskListener for task", activitiListener);
    }
    return taskListener;
  }

  /**
   * Parses the end events of a certain level in the process (process,
   * subprocess or another scope).
   * 
   * @param parentElement
   *          The 'parent' element that contains the end events (process,
   *          subprocess).
   * @param scope
   *          The {@link ScopeImpl} to which the end events must be added.
   */
  public void createEndEvent(EndEvent endEvent, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(endEvent, ELEMENT_EVENT_END, scope);
    EventDefinition eventDefinition = null;
    if (endEvent.getEventDefinitions().size() > 0) {
      eventDefinition = endEvent.getEventDefinitions().get(0);
    }
    
    // Error end event
    if (eventDefinition instanceof org.activiti.bpmn.model.ErrorEventDefinition) {
      org.activiti.bpmn.model.ErrorEventDefinition errorDefinition = (org.activiti.bpmn.model.ErrorEventDefinition) eventDefinition;
      if (bpmnModel.containsErrorRef(errorDefinition.getErrorCode())) {
        String errorCode = bpmnModel.getErrors().get(errorDefinition.getErrorCode());
        if (StringUtils.isEmpty(errorCode)) {
          bpmnModel.addProblem("errorCode is required for an error event", errorDefinition);
        }
        activity.setProperty("type", "errorEndEvent");
        errorDefinition.setErrorCode(errorCode);
      }
      activity.setActivityBehavior(activityBehaviorFactory.createErrorEndEventActivityBehavior(endEvent, errorDefinition));
      
    // Cancel end event      
    } else if (eventDefinition instanceof CancelEventDefinition) {
      if (scope.getProperty("type")==null || !scope.getProperty("type").equals("transaction")) {
        bpmnModel.addProblem("end event with cancelEventDefinition only supported inside transaction subprocess", endEvent);
      } else {
        activity.setProperty("type", "cancelEndEvent");
        activity.setActivityBehavior(activityBehaviorFactory.createCancelEndEventActivityBehavior(endEvent));
      }
    
    // Terminate end event  
    } else if (eventDefinition instanceof TerminateEventDefinition) {
      activity.setActivityBehavior(activityBehaviorFactory.createTerminateEndEventActivityBehavior(endEvent));
      
    // None end event  
    } else if (eventDefinition == null) {
      activity.setActivityBehavior(activityBehaviorFactory.createNoneEndEventActivityBehavior(endEvent));
    }
    
    createExecutionListenersOnScope(endEvent.getExecutionListeners(), activity);
    
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseEndEvent(endEvent, scope, activity);
    }
  }

  /**
   * Parses the boundary events of a certain 'level' (process, subprocess or
   * other scope).
   * 
   * Note that the boundary events are not parsed during the parsing of the bpmn
   * activities, since the semantics are different (boundaryEvent needs to be
   * added as nested activity to the reference activity on PVM level).
   * 
   * @param parentElement
   *          The 'parent' element that contains the activities (process,
   *          subprocess).
   * @param scopeElement
   *          The {@link ScopeImpl} to which the activities must be added.
   */
  public void createBoundaryEvent(BoundaryEvent boundaryEvent, ScopeImpl scopeElement) {
    ActivityImpl parentActivity = scopeElement.findActivity(boundaryEvent.getAttachedToRefId());
    if (parentActivity == null) {
      bpmnModel.addProblem("Invalid reference in boundary event. Make sure that the referenced activity is defined in the same scope as the boundary event", 
              boundaryEvent);
      return;
    }
   
    ActivityImpl nestedActivity = createActivityOnScope(boundaryEvent, ELEMENT_EVENT_BOUNDARY, parentActivity);

    EventDefinition eventDefinition = null;
    if (boundaryEvent.getEventDefinitions().size() > 0) {
      eventDefinition = boundaryEvent.getEventDefinitions().get(0);
    }
    
    boolean interrupting = boundaryEvent.isCancelActivity();
    ActivityBehavior behavior = null;
    
    if (eventDefinition instanceof TimerEventDefinition) {
      behavior = activityBehaviorFactory.createBoundaryEventActivityBehavior(boundaryEvent, interrupting, nestedActivity); 
      createBoundaryTimerEventDefinition((TimerEventDefinition) eventDefinition, interrupting, nestedActivity);
    } else if (eventDefinition instanceof org.activiti.bpmn.model.ErrorEventDefinition) {
      interrupting = true; // non-interrupting not yet supported
      behavior = activityBehaviorFactory.createBoundaryEventActivityBehavior(boundaryEvent, interrupting, nestedActivity);
      org.activiti.bpmn.model.ErrorEventDefinition modelErrorEvent = (org.activiti.bpmn.model.ErrorEventDefinition) eventDefinition;
      if (bpmnModel.containsErrorRef(modelErrorEvent.getErrorCode())) {
        String errorCode = bpmnModel.getErrors().get(modelErrorEvent.getErrorCode());
        if (StringUtils.isEmpty(errorCode)) {
          bpmnModel.addProblem("errorCode is required for an error event", boundaryEvent);
        }
        modelErrorEvent.setErrorCode(errorCode);
      }
      createBoundaryErrorEventDefinition(modelErrorEvent, interrupting, parentActivity, nestedActivity);
    } else if (eventDefinition instanceof SignalEventDefinition) {
      behavior = activityBehaviorFactory.createBoundaryEventActivityBehavior(boundaryEvent, interrupting, nestedActivity);
      createBoundarySignalEventDefinition((SignalEventDefinition) eventDefinition, interrupting, nestedActivity);
    } else if (eventDefinition instanceof CancelEventDefinition) {
      // always interrupting
      behavior = createBoundaryCancelEventDefinition((CancelEventDefinition) eventDefinition, nestedActivity);
    } else if (eventDefinition instanceof org.activiti.bpmn.model.CompensateEventDefinition) {
      behavior = activityBehaviorFactory.createBoundaryEventActivityBehavior(boundaryEvent, interrupting, nestedActivity);
      createCatchCompensateEventDefinition((org.activiti.bpmn.model.CompensateEventDefinition) eventDefinition, nestedActivity);      
    } else if (eventDefinition instanceof MessageEventDefinition) {
      behavior = activityBehaviorFactory.createBoundaryEventActivityBehavior(boundaryEvent, interrupting, nestedActivity);
      MessageEventDefinition modelMessageEvent = (MessageEventDefinition) eventDefinition;
      if (bpmnModel.containsMessageId(modelMessageEvent.getMessageRef())) {
        String messageName = bpmnModel.getMessage(modelMessageEvent.getMessageRef()).getName();
        if (StringUtils.isEmpty(messageName)) {
          bpmnModel.addProblem("messageName is required for a message event", boundaryEvent);
        }
        modelMessageEvent.setMessageRef(messageName);
      }
      createBoundaryMessageEventDefinition((MessageEventDefinition) eventDefinition, interrupting, nestedActivity);
    } else {
      bpmnModel.addProblem("Unsupported boundary event type", boundaryEvent);
    }
    
    nestedActivity.setActivityBehavior(behavior);
    
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryEvent(boundaryEvent, scopeElement, nestedActivity);
    }
  }

  /**
   * Parses a boundary timer event. The end-result will be that the given nested
   * activity will get the appropriate {@link ActivityBehavior}.
   * 
   * @param timerEventDefinition
   *          The XML element corresponding with the timer event details
   * @param interrupting
   *          Indicates whether this timer is interrupting.
   * @param timerActivity
   *          The activity which maps to the structure of the timer event on the
   *          boundary of another activity. Note that this is NOT the activity
   *          onto which the boundary event is attached, but a nested activity
   *          inside this activity, specifically created for this event.
   */
  public void createBoundaryTimerEventDefinition(TimerEventDefinition timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
    timerActivity.setProperty("type", "boundaryTimer");
    TimerDeclarationImpl timerDeclaration = createTimer(timerEventDefinition, timerActivity, TimerExecuteNestedActivityJobHandler.TYPE);
    
    // ACT-1427
    if (interrupting) {
      timerDeclaration.setInterruptingTimer(true);
    }
    
    addTimerDeclaration(timerActivity.getParent(), timerDeclaration);

    if (timerActivity.getParent() instanceof ActivityImpl) {
      ((ActivityImpl) timerActivity.getParent()).setScope(true);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryTimerEventDefinition(timerEventDefinition, interrupting, timerActivity);
    }
  }
  
  public void createBoundarySignalEventDefinition(SignalEventDefinition signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
    signalActivity.setProperty("type", "boundarySignal");
    
    if (bpmnModel.containsSignalId(signalEventDefinition.getSignalRef())) {
      String signalName = bpmnModel.getSignal(signalEventDefinition.getSignalRef()).getName();
      if (StringUtils.isEmpty(signalName)) {
        bpmnModel.addProblem("signalName is required for a signal event", signalEventDefinition);
      }
      signalEventDefinition.setSignalRef(signalName);
    }
    
    EventSubscriptionDeclaration signalDefinition = new EventSubscriptionDeclaration(signalEventDefinition.getSignalRef(), "signal");
    signalDefinition.setActivityId(signalActivity.getId());
    addEventSubscriptionDeclaration(signalDefinition, signalEventDefinition, signalActivity.getParent());
    
    if (signalActivity.getParent() instanceof ActivityImpl) {     
      ((ActivityImpl) signalActivity.getParent()).setScope(true);
    }
    
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundarySignalEventDefinition(signalEventDefinition, interrupting, signalActivity);
    }
    
  }
  
  public void createBoundaryMessageEventDefinition(MessageEventDefinition messageEventDefinition, boolean interrupting, ActivityImpl messageActivity) {
    messageActivity.setProperty("type", "boundaryMessage");
    
    EventSubscriptionDeclaration messageDefinition = new EventSubscriptionDeclaration(messageEventDefinition.getMessageRef(), "message");
    messageDefinition.setActivityId(messageActivity.getId());
    addEventSubscriptionDeclaration(messageDefinition, messageEventDefinition, messageActivity.getParent());
    
    if (messageActivity.getParent() instanceof ActivityImpl) {     
      ((ActivityImpl) messageActivity.getParent()).setScope(true);
    }
    
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryMessageEventDefinition(messageEventDefinition, interrupting, messageActivity);
    }
    
  }  

  @SuppressWarnings("unchecked")
  protected void createTimerStartEventDefinition(TimerEventDefinition timerEventDefinition, ActivityImpl timerActivity, ProcessDefinitionEntity processDefinition) {
    timerActivity.setProperty("type", "startTimerEvent");
    TimerDeclarationImpl timerDeclaration = createTimer(timerEventDefinition, timerActivity, TimerStartEventJobHandler.TYPE);
    timerDeclaration.setJobHandlerConfiguration(processDefinition.getKey());    

    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(PROPERTYNAME_START_TIMER);
    if (timerDeclarations == null) {
      timerDeclarations = new ArrayList<TimerDeclarationImpl>();
      processDefinition.setProperty(PROPERTYNAME_START_TIMER, timerDeclarations);
    }
    timerDeclarations.add(timerDeclaration);
  }
  
  protected void createIntermediateSignalEventDefinition(SignalEventDefinition signalEventDefinition, ActivityImpl signalActivity, boolean isAfterEventBasedGateway) {
    signalActivity.setProperty("type", "intermediateSignalCatch");   
  
    if (bpmnModel.containsSignalId(signalEventDefinition.getSignalRef())) {
      String signalName = bpmnModel.getSignal(signalEventDefinition.getSignalRef()).getName();
      if (StringUtils.isEmpty(signalName)) {
        bpmnModel.addProblem("signalName is required for a signal event", signalEventDefinition);
      }
      signalEventDefinition.setSignalRef(signalName);
    }
    
    EventSubscriptionDeclaration signalDefinition = new EventSubscriptionDeclaration(signalEventDefinition.getSignalRef(), "signal");
    if (isAfterEventBasedGateway) {
      signalDefinition.setActivityId(signalActivity.getId());
      addEventSubscriptionDeclaration(signalDefinition, signalEventDefinition, signalActivity.getParent());      
    } else {
      signalActivity.setScope(true);
      addEventSubscriptionDeclaration(signalDefinition, signalEventDefinition, signalActivity);   
    }
    
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateSignalCatchEventDefinition(signalEventDefinition, signalActivity);
    }
  }
  
  protected void createIntermediateTimerEventDefinition(TimerEventDefinition timerEventDefinition, ActivityImpl timerActivity, boolean isAfterEventBasedGateway) {
    timerActivity.setProperty("type", "intermediateTimer");
    TimerDeclarationImpl timerDeclaration = createTimer(timerEventDefinition, timerActivity, TimerCatchIntermediateEventJobHandler.TYPE);
    if (isAfterEventBasedGateway) {
      addTimerDeclaration(timerActivity.getParent(), timerDeclaration);
    } else {
      addTimerDeclaration(timerActivity, timerDeclaration);
      timerActivity.setScope(true);
    }
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateTimerEventDefinition(timerEventDefinition, timerActivity);
    }
  }

  protected TimerDeclarationImpl createTimer(TimerEventDefinition timerEventDefinition, ScopeImpl timerActivity, String jobHandlerType) {
    TimerDeclarationType type = null;
    Expression expression = null;
    if (StringUtils.isNotEmpty(timerEventDefinition.getTimeDate())) {
      // TimeDate
      type = TimerDeclarationType.DATE;
      expression = expressionManager.createExpression(timerEventDefinition.getTimeDate());
    } else if (StringUtils.isNotEmpty(timerEventDefinition.getTimeCycle())) {
      // TimeCycle
      type = TimerDeclarationType.CYCLE;
      expression = expressionManager.createExpression(timerEventDefinition.getTimeCycle());
    } else if (StringUtils.isNotEmpty(timerEventDefinition.getTimeDuration())) {
      // TimeDuration
      type = TimerDeclarationType.DURATION;
      expression = expressionManager.createExpression(timerEventDefinition.getTimeDuration());
    }    
    
    // neither date, cycle or duration configured!
    if (expression == null) {
      bpmnModel.addProblem("Timer needs configuration (either timeDate, timeCycle or timeDuration is needed).", timerEventDefinition);      
    }    

    // Parse the timer declaration
    // TODO move the timer declaration into the bpmn activity or next to the
    // TimerSession
    TimerDeclarationImpl timerDeclaration = new TimerDeclarationImpl(expression, type, jobHandlerType);
    timerDeclaration.setJobHandlerConfiguration(timerActivity.getId());
    timerDeclaration.setExclusive(true);
    return timerDeclaration;
  }

  public void createBoundaryErrorEventDefinition(org.activiti.bpmn.model.ErrorEventDefinition errorEventDefinition, 
      boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {

    nestedErrorEventActivity.setProperty("type", "boundaryError");
    ScopeImpl catchingScope = nestedErrorEventActivity.getParent();
    ((ActivityImpl) catchingScope).setScope(true);

    ErrorEventDefinition definition = new ErrorEventDefinition(nestedErrorEventActivity.getId());
    definition.setErrorCode(errorEventDefinition.getErrorCode());
    
    addErrorEventDefinition(definition, catchingScope);    
  
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryErrorEventDefinition(errorEventDefinition, interrupting, activity, nestedErrorEventActivity);
    }
  }

  /**
   * Checks if the given activity is a child activity of the
   * possibleParentActivity.
   */
  protected boolean isChildActivity(ActivityImpl activityToCheck, ActivityImpl possibleParentActivity) {
    for (ActivityImpl child : possibleParentActivity.getActivities()) {
      if (child.getId().equals(activityToCheck.getId()) || isChildActivity(activityToCheck, child)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  protected void addTimerDeclaration(ScopeImpl scope, TimerDeclarationImpl timerDeclaration) {
    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) scope.getProperty(PROPERTYNAME_TIMER_DECLARATION);
    if (timerDeclarations == null) {
      timerDeclarations = new ArrayList<TimerDeclarationImpl>();
      scope.setProperty(PROPERTYNAME_TIMER_DECLARATION, timerDeclarations);
    }
    timerDeclarations.add(timerDeclaration);
  }

  @SuppressWarnings("unchecked")
  protected void addVariableDeclaration(ScopeImpl scope, VariableDeclaration variableDeclaration) {
    List<VariableDeclaration> variableDeclarations = (List<VariableDeclaration>) scope.getProperty(PROPERTYNAME_VARIABLE_DECLARATIONS);
    if (variableDeclarations == null) {
      variableDeclarations = new ArrayList<VariableDeclaration>();
      scope.setProperty(PROPERTYNAME_VARIABLE_DECLARATIONS, variableDeclarations);
    }
    variableDeclarations.add(variableDeclaration);
  }

  /**
   * Parses a subprocess (formally known as an embedded subprocess): a subprocess
   * defined within another process definition.
   * 
   * @param subProcessElement
   *          The XML element corresponding with the subprocess definition
   * @param scope
   *          The current scope on which the subprocess is defined.
   */
  public ActivityImpl createSubProcess(SubProcess subProcess, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(subProcess, ELEMENT_SUBPROCESS, scope);
    
    activity.setAsync(subProcess.isAsynchronous());
    activity.setExclusive(!subProcess.isNotExclusive());

    boolean triggeredByEvent = false;
    if (subProcess instanceof EventSubProcess) {
      triggeredByEvent = true;
    }
    activity.setProperty("triggeredByEvent", triggeredByEvent);
    
    // event subprocesses are not scopes
    activity.setScope(!triggeredByEvent);
    activity.setActivityBehavior(activityBehaviorFactory.createSubprocActivityBehavior(subProcess));
    
    processFlowElements(subProcess.getFlowElements(), activity, subProcess);
    processArtifacts(subProcess.getArtifacts(), activity);
    
    if (subProcess.getIoSpecification() != null) {
      IOSpecification ioSpecification = createIOSpecification(subProcess.getIoSpecification());
      activity.setIoSpecification(ioSpecification);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseSubProcess(subProcess, scope, activity);
    }
    return activity;
  }
  
  protected ActivityImpl createTransaction(Transaction transaction, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(transaction, ELEMENT_TRANSACTION, scope);
    
    activity.setAsync(transaction.isAsynchronous());
    activity.setExclusive(!transaction.isNotExclusive());
    
    activity.setScope(true);
    activity.setActivityBehavior(activityBehaviorFactory.createTransactionActivityBehavior(transaction));
    
    processFlowElements(transaction.getFlowElements(), activity, transaction);
    processArtifacts(transaction.getArtifacts(), activity);
    
    if (transaction.getIoSpecification() != null) {
      IOSpecification ioSpecification = createIOSpecification(transaction.getIoSpecification());
      activity.setIoSpecification(ioSpecification);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseTransaction(transaction, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a call activity (currently only supporting calling subprocesses).
   * 
   * @param callActivityElement
   *          The XML element defining the call activity
   * @param scope
   *          The current scope on which the call activity is defined.
   */
  public ActivityImpl createCallActivity(CallActivity callActivity, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(callActivity, ELEMENT_CALL_ACTIVITY, scope);
    activity.setScope(true);
    activity.setActivityBehavior(activityBehaviorFactory.createCallActivityBehavior(callActivity));

    createExecutionListenersOnScope(callActivity.getExecutionListeners(), activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseCallActivity(callActivity, scope, activity);
    }
    return activity;
  }

  /**
   * Parses all sequence flow of a scope.
   * 
   * @param processElement
   *          The 'process' element wherein the sequence flow are defined.
   * @param scope
   *          The scope to which the sequence flow must be added.
   */
  public void createSequenceFlow(SequenceFlow sequenceFlow, ScopeImpl scope) {
    // Implicit check: sequence flow cannot cross (sub) process boundaries: we
    // don't do a processDefinition.findActivity here
    ActivityImpl sourceActivity = scope.findActivity(sequenceFlow.getSourceRef());
    ActivityImpl destinationActivity = scope.findActivity(sequenceFlow.getTargetRef());
    
    if (sourceActivity == null) {
      bpmnModel.addProblem("Invalid source '" + sequenceFlow.getSourceRef() + "' of sequence flow '" + sequenceFlow.getId() + "'", sequenceFlow);
    } else if (destinationActivity == null) {
      throw new ActivitiException("Invalid destination '" + sequenceFlow.getTargetRef() + "' of sequence flow '" + sequenceFlow.getId() + "'");
    } else if(!(sourceActivity.getActivityBehavior() instanceof EventBasedGatewayActivityBehavior)
            && destinationActivity.getActivityBehavior() instanceof IntermediateCatchEventActivityBehavior
            && (destinationActivity.getParentActivity() != null)
            && (destinationActivity.getParentActivity().getActivityBehavior() instanceof EventBasedGatewayActivityBehavior)) {
      
      bpmnModel.addProblem("Invalid incoming sequenceflow " + sequenceFlow.getId() + " for intermediateCatchEvent with id '"
            +destinationActivity.getId()+"' connected to an event-based gateway.", sequenceFlow);        
    } else {       
      
      TransitionImpl transition = sourceActivity.createOutgoingTransition(sequenceFlow.getId());
      sequenceFlows.put(sequenceFlow.getId(), transition);
      transition.setProperty("name", sequenceFlow.getName());
      transition.setProperty("documentation", sequenceFlow.getDocumentation());
      transition.setDestination(destinationActivity);
      
      if (StringUtils.isNotEmpty(sequenceFlow.getConditionExpression())) {
        Condition expressionCondition = new UelExpressionCondition(expressionManager.createExpression(sequenceFlow.getConditionExpression()));
        transition.setProperty(PROPERTYNAME_CONDITION_TEXT, sequenceFlow.getConditionExpression());
        transition.setProperty(PROPERTYNAME_CONDITION, expressionCondition);
      }
      
      createExecutionListenersOnTransition(sequenceFlow.getExecutionListeners(), transition);

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseSequenceFlow(sequenceFlow, scope, transition);
      }
    }
  }
  
  protected void createAssociation(Association association, ScopeImpl parentScope) {
    if (bpmnModel.getArtifact(association.getSourceRef()) != null ||
        bpmnModel.getArtifact(association.getTargetRef()) != null) {
      
      // connected to a text annotation so skipping it
      return;
    }
    
    ActivityImpl sourceActivity = parentScope.findActivity(association.getSourceRef());
    ActivityImpl targetActivity = parentScope.findActivity(association.getTargetRef());
    
    // an association may reference elements that are not parsed as activities (like for instance 
    // text annotations so do not throw an exception if sourceActivity or targetActivity are null)
    // However, we make sure they reference 'something':
    if(sourceActivity == null) {
      //bpmnModel.addProblem("Invalid reference sourceRef '" + association.getSourceRef() + "' of association element ", association.getId());
    } else if(targetActivity == null) {
      //bpmnModel.addProblem("Invalid reference targetRef '" + association.getTargetRef() + "' of association element ", association.getId());
    } else {      
      if(sourceActivity != null && sourceActivity.getProperty("type").equals("compensationBoundaryCatch")) {
        Object isForCompensation = targetActivity.getProperty(PROPERTYNAME_IS_FOR_COMPENSATION);          
        if(isForCompensation == null || !(Boolean) isForCompensation) {
          bpmnModel.addProblem("compensation boundary catch must be connected to element with isForCompensation=true", association);
        } else {            
          ActivityImpl compensatedActivity = sourceActivity.getParentActivity();
          compensatedActivity.setProperty(PROPERTYNAME_COMPENSATION_HANDLER_ID, targetActivity.getId());            
        }
      }
    }
  }
  
  public void createExecutionListenersOnScope(List<ActivitiListener> activitiListenerList, ScopeImpl scope) {
    for (ActivitiListener activitiListener : activitiListenerList) {
      scope.addExecutionListener(activitiListener.getEvent(), createExecutionListener(activitiListener));
    }
  }
  
  public void createExecutionListenersOnTransition(List<ActivitiListener> activitiListenerList, TransitionImpl transition) {
    for (ActivitiListener activitiListener : activitiListenerList) {
      transition.addExecutionListener(createExecutionListener(activitiListener));
    }
  }

  /**
   * Parses an {@link ExecutionListener} implementation for the given
   * executionListener element.
   * 
   * @param executionListenerElement
   *          the XML element containing the executionListener definition.
   */
  public ExecutionListener createExecutionListener(ActivitiListener activitiListener) {
    ExecutionListener executionListener = null;
  
    if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(activitiListener.getImplementationType())) {
      executionListener = listenerFactory.createClassDelegateExecutionListener(activitiListener);  
    } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
      executionListener = listenerFactory.createExpressionExecutionListener(activitiListener);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(activitiListener.getImplementationType())) {
      executionListener = listenerFactory.createDelegateExpressionExecutionListener(activitiListener);
    }
    return executionListener;
  }
  
  protected void addErrorEventDefinition(ErrorEventDefinition errorEventDefinition, ScopeImpl catchingScope) {
    List<ErrorEventDefinition> errorEventDefinitions = (List<ErrorEventDefinition>) catchingScope.getProperty(PROPERTYNAME_ERROR_EVENT_DEFINITIONS);
    if(errorEventDefinitions == null) {
      errorEventDefinitions = new ArrayList<ErrorEventDefinition>();
      catchingScope.setProperty(PROPERTYNAME_ERROR_EVENT_DEFINITIONS, errorEventDefinitions);
    }
    errorEventDefinitions.add(errorEventDefinition);
    Collections.sort(errorEventDefinitions, ErrorEventDefinition.comparator);
  }

  //Diagram interchange
  // /////////////////////////////////////////////////////////////////

  public void processDI() {
    if (bpmnModel.getLocationMap().size() > 0) {
      for (Process process : bpmnModel.getProcesses()) {
        if (process.isExecutable() == false) continue;
        ProcessDefinitionEntity processDefinition = getProcessDefinition(process.getId());
        if (processDefinition != null) {
          processDefinition.setGraphicalNotationDefined(true);
          for (String shapeId : bpmnModel.getLocationMap().keySet()) {
            if (processDefinition.findActivity(shapeId) != null) {
              createBPMNShape(shapeId, bpmnModel.getGraphicInfo(shapeId), processDefinition);
            }
          }
      
          for (String edgeId : bpmnModel.getFlowLocationMap().keySet()) {
            if (bpmnModel.getFlowElement(edgeId) != null) {
              createBPMNEdge(edgeId, bpmnModel.getFlowLocationGraphicInfo(edgeId));
            }
          }
        }
      }
    }
  }

  public void createBPMNShape(String key, GraphicInfo graphicInfo, ProcessDefinitionEntity processDefinition) {
    ActivityImpl activity = processDefinition.findActivity(key);
    if (activity != null) {
      createDIBounds(graphicInfo, activity);
      
    } else {
      org.activiti.engine.impl.pvm.process.Lane lane = processDefinition.getLaneForId(key);
     
      if(lane != null) {
        // The shape represents a lane
        createDIBounds(graphicInfo, lane);
      } else {
        bpmnModel.addProblem("Invalid reference in 'bpmnElement' attribute, activity " + key + " not found", graphicInfo);
      }
    }
  }
 
  protected void createDIBounds(GraphicInfo graphicInfo, HasDIBounds target) {
    target.setX((int) graphicInfo.getX());
    target.setY((int) graphicInfo.getY());
    target.setWidth((int) graphicInfo.getWidth());
    target.setHeight((int) graphicInfo.getHeight());
  }

  public void createBPMNEdge(String key, List<GraphicInfo> graphicList) {
    FlowElement flowElement = bpmnModel.getFlowElement(key);
    if (flowElement != null && sequenceFlows.containsKey(key)) {
      TransitionImpl sequenceFlow = sequenceFlows.get(key);
      List<Integer> waypoints = new ArrayList<Integer>();
      for (GraphicInfo waypointInfo : graphicList) {
        waypoints.add((int) waypointInfo.getX());
        waypoints.add((int) waypointInfo.getY());
      }
      sequenceFlow.setWaypoints(waypoints);
    } else if (bpmnModel.getArtifact(key) != null) {
      // it's an association, so nothing to do
    } else { 
      GraphicInfo graphicInfo = null;
      if (graphicList != null && graphicList.size() > 0) {
        graphicInfo = graphicList.get(0);
      } else {
        graphicInfo = new GraphicInfo();
      }
      bpmnModel.addProblem("Invalid reference in 'bpmnElement' attribute, sequenceFlow " + key + " not found", graphicInfo);
    }
  }

  // Getters, setters and Parser overriden operations
  // ////////////////////////////////////////

  public List<ProcessDefinitionEntity> getProcessDefinitions() {
    return processDefinitions;
  }

  public ProcessDefinitionEntity getProcessDefinition(String processDefinitionKey) {
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      if (processDefinition.getKey().equals(processDefinitionKey)) {
        return processDefinition;
      }
    }
    return null;
  }

  public void addStructure(StructureDefinition structure) {
    this.structures.put(structure.getId(), structure);
  }

  public void addService(BpmnInterfaceImplementation bpmnInterfaceImplementation) {
    this.interfaceImplementations.put(bpmnInterfaceImplementation.getName(), bpmnInterfaceImplementation);
  }

  public void addOperation(OperationImplementation operationImplementation) {
    this.operationImplementations.put(operationImplementation.getId(), operationImplementation);
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
  
}
