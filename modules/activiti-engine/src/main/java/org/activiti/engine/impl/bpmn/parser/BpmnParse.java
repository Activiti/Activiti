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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataAssociation;
import org.activiti.bpmn.model.DataSpec;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Import;
import org.activiti.bpmn.model.Interface;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.parse.Problem;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.Condition;
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
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.HasDIBounds;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.impl.util.io.ResourceStreamSource;
import org.activiti.engine.impl.util.io.StreamSource;
import org.activiti.engine.impl.util.io.StringStreamSource;
import org.activiti.engine.impl.util.io.UrlStreamSource;
import org.activiti.engine.impl.util.xml.Element;
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
  
  // NEW
  protected BpmnParseHandlers bpmnParserHandlers;
  protected ProcessDefinitionEntity currentProcessDefinition;
  protected FlowElement currentFlowElement;
  protected ActivityImpl currentActivity;
  protected LinkedList<SubProcess> currentSubprocessStack = new LinkedList<SubProcess>();
  protected LinkedList<ScopeImpl> currentScopeStack = new LinkedList<ScopeImpl>();
  
  // NEW
  
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
  protected ActivityBehaviorFactory activityBehaviorFactory;
  protected ListenerFactory listenerFactory;

  /**
   * Constructor to be called by the {@link BpmnParser}.
   */
  public BpmnParse(BpmnParser parser) {
    this.expressionManager = parser.getExpressionManager();
    this.activityBehaviorFactory = parser.getActivityBehaviorFactory();
    this.listenerFactory = parser.getListenerFactory();
    this.bpmnParserHandlers = parser.getBpmnParserHandlers();
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
    InputStreamReader in = null;
    try {
      converter.validateModel(streamSource.getInputStream());
      streamSource.getInputStream().reset();
      in = new InputStreamReader(streamSource.getInputStream(), "UTF-8");
      XMLStreamReader xtr = xif.createXMLStreamReader(in);
      bpmnModel = converter.convertToBpmnModel(xtr);
      createImports();
      createItemDefinitions();
      createMessages();
      createOperations();
      transformProcessDefinitions();
    } catch (Exception e) {
      throw new ActivitiException("Error parsing XML", e);
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception e) {
        LOGGER.info("Problem closing BPMN input stream", e);
      }
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
      bpmnParserHandlers.parse(this, process);
    }
    
    if (processDefinitions.size() > 0) {
      processDI();
    }
  }
  
  public void processFlowElements(Collection<FlowElement> flowElements) {
    
    // Parsing the elements is done in a strict order of types, 
    // as otherwise certain information might not be available when parsing a certain type.
    
    // Using lists as we want to keep the order in which they are defined
    List<SequenceFlow> sequenceFlowToParse = new ArrayList<SequenceFlow>();
    List<BoundaryEvent> boundaryEventsToParse = new ArrayList<BoundaryEvent>();
    
    // Activities are parsed first
    for (FlowElement flowElement : flowElements) {
      
      // Sequence flow are also flow elements, but are only parsed once every activity is found
      if (flowElement instanceof SequenceFlow) { 
        sequenceFlowToParse.add((SequenceFlow) flowElement);
      } else if (flowElement instanceof BoundaryEvent) {
        boundaryEventsToParse.add((BoundaryEvent) flowElement);
      } else {
        bpmnParserHandlers.parse(this, flowElement);
      }
      
    }
    
    // Boundary events are parsed after all the regular activities are parsed
    for (BoundaryEvent boundaryEvent : boundaryEventsToParse) {
      bpmnParserHandlers.parse(this, boundaryEvent);
    }
    
    // sequence flows
    for (SequenceFlow sequenceFlow : sequenceFlowToParse) {
      bpmnParserHandlers.parse(this, sequenceFlow);
    }
    
    // validations after complete model
    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof ExclusiveGateway) {
        ActivityImpl gatewayActivity = getCurrentScope().findActivity(flowElement.getId());
        validateExclusiveGateway(gatewayActivity, (ExclusiveGateway) flowElement);
      }
    }
  }
  
  public void processArtifacts(Collection<Artifact> artifacts, ScopeImpl scope) {
    // associations  
    for (Artifact artifact : artifacts) {
      if (artifact instanceof Association) {
        createAssociation((Association) artifact, scope);
      }
    }
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

  public IOSpecification createIOSpecification(org.activiti.bpmn.model.IOSpecification specificationModel) {
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
  
  public AbstractDataAssociation createDataInputAssociation(DataAssociation dataAssociationElement) {
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
  
  public AbstractDataAssociation createDataOutputAssociation(DataAssociation dataAssociationElement) {
    if (StringUtils.isNotEmpty(dataAssociationElement.getSourceRef())) {
      return new MessageImplicitDataOutputAssociation(dataAssociationElement.getTargetRef(), dataAssociationElement.getSourceRef());
    } else {
      Expression transformation = this.expressionManager.createExpression(dataAssociationElement.getTransformation());
      AbstractDataAssociation dataOutputAssociation = new TransformationDataOutputAssociation(null, dataAssociationElement.getTargetRef(), transformation);
      return dataOutputAssociation;
    }
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

  public BpmnParseHandlers getBpmnParserHandlers() {
    return bpmnParserHandlers;
  }
  
  public void setBpmnParserHandlers(BpmnParseHandlers bpmnParserHandlers) {
    this.bpmnParserHandlers = bpmnParserHandlers;
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

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }
  
  public Map<String, TransitionImpl> getSequenceFlows() {
    return sequenceFlows;
  }
  
  public void setSequenceFlows(Map<String, TransitionImpl> sequenceFlows) {
    this.sequenceFlows = sequenceFlows;
  }
  
  public Map<String, Operation> getOperations() {
    return operations;
  }

  public void setOperations(Map<String, Operation> operations) {
    this.operations = operations;
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

  public ActivityImpl getCurrentActivity() {
    return currentActivity;
  }

  public void setCurrentActivity(ActivityImpl currentActivity) {
    this.currentActivity = currentActivity;
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
  
  public void setCurrentScope(ScopeImpl scope) {
    currentScopeStack.push(scope);
  }
  
  public ScopeImpl getCurrentScope() {
    return currentScopeStack.peek();
  }
  
  public void removeCurrentScope() {
    currentScopeStack.pop();
  }
  
}
