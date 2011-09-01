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
import java.net.URL;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BusinessRuleTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.InclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchEventActivitiBehaviour;
import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ManualTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
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
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionExecutionListener;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionTaskListener;
import org.activiti.engine.impl.bpmn.listener.ExpressionExecutionListener;
import org.activiti.engine.impl.bpmn.listener.ExpressionTaskListener;
import org.activiti.engine.impl.bpmn.webservice.BpmnInterface;
import org.activiti.engine.impl.bpmn.webservice.BpmnInterfaceImplementation;
import org.activiti.engine.impl.bpmn.webservice.MessageDefinition;
import org.activiti.engine.impl.bpmn.webservice.MessageImplicitDataInputAssociation;
import org.activiti.engine.impl.bpmn.webservice.MessageImplicitDataOutputAssociation;
import org.activiti.engine.impl.bpmn.webservice.Operation;
import org.activiti.engine.impl.bpmn.webservice.OperationImplementation;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.el.FixedValue;
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
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.util.xml.Parse;
import org.activiti.engine.impl.variable.VariableDeclaration;

/**
 * Specific parsing of one BPMN 2.0 XML file, created by the {@link BpmnParser}.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Christian Stettler
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Esteban Robles
 * @author Daniel Meyer
 */
public class BpmnParse extends Parse {

  protected static final Logger LOGGER = Logger.getLogger(BpmnParse.class.getName());

  public static final String PROPERTYNAME_DOCUMENTATION = "documentation";
  public static final String PROPERTYNAME_INITIAL = "initial";
  public static final String PROPERTYNAME_INITIATOR_VARIABLE_NAME = "initiatorVariableName";
  public static final String PROPERTYNAME_CONDITION = "condition";
  public static final String PROPERTYNAME_CONDITION_TEXT = "conditionText";
  public static final String PROPERTYNAME_VARIABLE_DECLARATIONS = "variableDeclarations";
  public static final String PROPERTYNAME_TIMER_DECLARATION = "timerDeclarations";
  public static final String PROPERTYNAME_ISEXPANDED = "isExpanded";
  public static final String PROPERTYNAME_START_TIMER = "timerStart";

  /** The deployment to which the parsed process definitions will be added. */
  protected DeploymentEntity deployment;

  /** The end result of the parsing: a list of process definition. */
  protected List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();

  /** Mapping of found errors in BPMN 2.0 file */
  protected Map<String, Error> errors = new HashMap<String, Error>();

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

  // Members
  protected ExpressionManager expressionManager;
  protected List<BpmnParseListener> parseListeners;
  protected Map<String, XMLImporter> importers = new HashMap<String, XMLImporter>();
  protected Map<String, String> prefixs = new HashMap<String, String>();
  protected String targetNamespace;

  /**
   * Constructor to be called by the {@link BpmnParser}.
   * 
   * Note the package modifier here: only the {@link BpmnParser} is allowed to
   * create instances.
   */
  BpmnParse(BpmnParser parser) {
    super(parser);
    this.expressionManager = parser.getExpressionManager();
    this.parseListeners = parser.getParseListeners();
    setSchemaResource(ReflectUtil.getResource(BpmnParser.BPMN_20_SCHEMA_LOCATION).toString());
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

  @Override
  public BpmnParse execute() {
    super.execute(); // schema validation

    try {
      parseRootElement();

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Uknown exception", e);
    } finally {
      if (hasWarnings()) {
        logWarnings();
      }
      if (hasErrors()) {
        throwActivitiExceptionForErrors();
      }
    }

    return this;
  }

  /**
   * Parses the 'definitions' root element
   */
  protected void parseRootElement() {
    parseDefinitionsAttributes();
    parseImports();
    parseItemDefinitions();
    parseMessages();
    parseInterfaces();
    parseErrors();
    parseProcessDefinitions();

    // Diagram interchange parsing must be after parseProcessDefinitions,
    // since it depends and sets values on existing process definition objects
    parseDiagramInterchangeElements();
  }

  protected void parseDefinitionsAttributes() {
    String typeLanguage = rootElement.attribute("typeLanguage");
    String expressionLanguage = rootElement.attribute("expressionLanguage");
    this.targetNamespace = rootElement.attribute("targetNamespace");

    if (typeLanguage != null) {
      if (typeLanguage.contains("XMLSchema")) {
        LOGGER.info("XMLSchema currently not supported as typeLanguage");
      }
    }

    if (expressionLanguage != null) {
      if (expressionLanguage.contains("XPath")) {
        LOGGER.info("XPath currently not supported as expressionLanguage");
      }
    }

    for (String attribute : rootElement.attributes()) {
      if (attribute.startsWith("xmlns:")) {
        String prefixValue = rootElement.attribute(attribute);
        String prefixName = attribute.substring(6);
        this.prefixs.put(prefixName, prefixValue);
      }
    }
  }

  protected String resolveName(String name) {
    if (name == null) {
      return null;
    }
    int indexOfP = name.indexOf(':');
    if (indexOfP != -1) {
      String prefix = name.substring(0, indexOfP);
      String resolvedPrefix = this.prefixs.get(prefix);
      return resolvedPrefix + ":" + name.substring(indexOfP + 1);
    } else {
      return name;
    }
  }

  /**
   * Parses the rootElement importing structures
   * 
   * @param rootElement
   *          The root element of the XML file.
   */
  protected void parseImports() {
    List<Element> imports = rootElement.elements("import");
    for (Element theImport : imports) {
      String importType = theImport.attribute("importType");
      XMLImporter importer = this.getImporter(importType, theImport);
      if (importer == null) {
        addError("Could not import item of type " + importType, theImport);
      } else {
        importer.importFrom(theImport, this);
      }
    }
  }

  protected XMLImporter getImporter(String importType, Element theImport) {
    if (this.importers.containsKey(importType)) {
      return this.importers.get(importType);
    } else {
      if (importType.equals("http://schemas.xmlsoap.org/wsdl/")) {
        Class< ? > wsdlImporterClass;
        try {
          wsdlImporterClass = Class.forName("org.activiti.engine.impl.webservice.CxfWSDLImporter", true, Thread.currentThread().getContextClassLoader());
          XMLImporter newInstance = (XMLImporter) wsdlImporterClass.newInstance();
          this.importers.put(importType, newInstance);
          return newInstance;
        } catch (Exception e) {
          addError("Could not find importer for type " + importType, theImport);
        }
      }
      return null;
    }
  }

  /**
   * Parses the itemDefinitions of the given definitions file. Item definitions
   * are not contained within a process element, but they can be referenced from
   * inner process elements.
   * 
   * @param definitionsElement
   *          The root element of the XML file.
   */
  public void parseItemDefinitions() {
    for (Element itemDefinitionElement : rootElement.elements("itemDefinition")) {
      String id = itemDefinitionElement.attribute("id");
      String structureRef = this.resolveName(itemDefinitionElement.attribute("structureRef"));
      String itemKind = itemDefinitionElement.attribute("itemKind");
      StructureDefinition structure = null;

      try {
        // it is a class
        Class< ? > classStructure = ReflectUtil.loadClass(structureRef);
        structure = new ClassStructureDefinition(classStructure);
      } catch (ActivitiException e) {
        // it is a reference to a different structure
        structure = this.structures.get(structureRef);
      }

      ItemDefinition itemDefinition = new ItemDefinition(this.targetNamespace + ":" + id, structure);
      if (itemKind != null) {
        itemDefinition.setItemKind(ItemKind.valueOf(itemKind));
      }
      itemDefinitions.put(itemDefinition.getId(), itemDefinition);
    }
  }

  /**
   * Parses the messages of the given definitions file. Messages are not
   * contained within a process element, but they can be referenced from inner
   * process elements.
   * 
   * @param definitionsElement
   *          The root element of the XML file/
   */
  public void parseMessages() {
    for (Element messageElement : rootElement.elements("message")) {
      String id = messageElement.attribute("id");
      String itemRef = this.resolveName(messageElement.attribute("itemRef"));

      if (!this.itemDefinitions.containsKey(itemRef)) {
        addError(itemRef + " does not exist", messageElement);
      } else {
        ItemDefinition itemDefinition = this.itemDefinitions.get(itemRef);
        MessageDefinition message = new MessageDefinition(this.targetNamespace + ":" + id, itemDefinition);
        this.messages.put(message.getId(), message);
      }
    }
  }

  /**
   * Parses the interfaces and operations defined withing the root element.
   * 
   * @param definitionsElement
   *          The root element of the XML file/
   */
  public void parseInterfaces() {
    for (Element interfaceElement : rootElement.elements("interface")) {

      // Create the interface
      String id = interfaceElement.attribute("id");
      String name = interfaceElement.attribute("name");
      String implementationRef = this.resolveName(interfaceElement.attribute("implementationRef"));
      BpmnInterface bpmnInterface = new BpmnInterface(this.targetNamespace + ":" + id, name);
      bpmnInterface.setImplementation(this.interfaceImplementations.get(implementationRef));

      // Handle all its operations
      for (Element operationElement : interfaceElement.elements("operation")) {
        Operation operation = parseOperation(operationElement, bpmnInterface);
        bpmnInterface.addOperation(operation);
      }

      bpmnInterfaces.put(bpmnInterface.getId(), bpmnInterface);
    }
  }

  public Operation parseOperation(Element operationElement, BpmnInterface bpmnInterface) {
    Element inMessageRefElement = operationElement.element("inMessageRef");
    String inMessageRef = this.resolveName(inMessageRefElement.getText());

    if (!this.messages.containsKey(inMessageRef)) {
      addError(inMessageRef + " does not exist", inMessageRefElement);
      return null;
    } else {
      MessageDefinition inMessage = this.messages.get(inMessageRef);
      String id = operationElement.attribute("id");
      String name = operationElement.attribute("name");
      String implementationRef = this.resolveName(operationElement.attribute("implementationRef"));
      Operation operation = new Operation(this.targetNamespace + ":" + id, name, bpmnInterface, inMessage);
      operation.setImplementation(this.operationImplementations.get(implementationRef));

      Element outMessageRefElement = operationElement.element("outMessageRef");
      if (outMessageRefElement != null) {
        String outMessageRef = this.resolveName(outMessageRefElement.getText());
        if (this.messages.containsKey(outMessageRef)) {
          MessageDefinition outMessage = this.messages.get(outMessageRef);
          operation.setOutMessage(outMessage);
        }
      }

      operations.put(operation.getId(), operation);
      return operation;
    }
  }

  public void parseErrors() {
    for (Element errorElement : rootElement.elements("error")) {
      Error error = new Error();

      String id = errorElement.attribute("id");
      if (id == null) {
        addError("'id' is mandatory on error definition", errorElement);
      }
      error.setId(id);

      String errorCode = errorElement.attribute("errorCode");
      if (errorCode == null) {
        addError("'errorCode' is mandatory on error definition", errorElement);
      }
      error.setErrorCode(errorCode);

      errors.put(id, error);
    }
  }

  /**
   * Parses all the process definitions defined within the 'definitions' root
   * element.
   * 
   * @param definitionsElement
   *          The root element of the XML file.
   */
  public void parseProcessDefinitions() {
    for (Element processElement : rootElement.elements("process")) {
      processDefinitions.add(parseProcess(processElement));
    }
  }

  /**
   * Parses one process (ie anything inside a &lt;process&gt; element).
   * 
   * @param processElement
   *          The 'process' element.
   * @return The parsed version of the XML: a {@link ProcessDefinitionImpl}
   *         object.
   */
  public ProcessDefinitionEntity parseProcess(Element processElement) {
    // reset all mappings that are related to one process definition
    sequenceFlows = new HashMap<String, TransitionImpl>();

    ProcessDefinitionEntity processDefinition = new ProcessDefinitionEntity();

    /*
     * Mapping object model - bpmn xml: processDefinition.id -> generated by
     * activiti engine processDefinition.key -> bpmn id (required)
     * processDefinition.name -> bpmn name (optional)
     */
    processDefinition.setKey(processElement.attribute("id"));
    processDefinition.setName(processElement.attribute("name"));
    processDefinition.setCategory(rootElement.attribute("targetNamespace"));
    processDefinition.setProperty(PROPERTYNAME_DOCUMENTATION, parseDocumentation(processElement));
    processDefinition.setTaskDefinitions(new HashMap<String, TaskDefinition>());
    processDefinition.setDeploymentId(deployment.getId());

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("Parsing process " + processDefinition.getKey());
    }
    parseScope(processElement, processDefinition);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseProcess(processElement, processDefinition);
    }

    return processDefinition;
  }

  /**
   * Parses a scope: a process, subprocess, etc.
   * 
   * Note that a process definition is a scope on itself.
   * 
   * @param scopeElement
   *          The XML element defining the scope
   * @param parentScope
   *          The scope that contains the nested scope.
   */
  public void parseScope(Element scopeElement, ScopeImpl parentScope) {

    // Not yet supported on process level (PVM additions needed):
    // parseProperties(processElement);

    parseStartEvents(scopeElement, parentScope);
    parseActivities(scopeElement, parentScope);
    parseEndEvents(scopeElement, parentScope);
    parseBoundaryEvents(scopeElement, parentScope);
    parseSequenceFlow(scopeElement, parentScope);
    parseExecutionListenersOnScope(scopeElement, parentScope);

    IOSpecification ioSpecification = parseIOSpecification(scopeElement.element("ioSpecification"));
    parentScope.setIoSpecification(ioSpecification);
  }

  protected IOSpecification parseIOSpecification(Element ioSpecificationElement) {
    if (ioSpecificationElement == null) {
      return null;
    }

    IOSpecification ioSpecification = new IOSpecification();

    for (Element dataInputElement : ioSpecificationElement.elements("dataInput")) {
      String id = dataInputElement.attribute("id");
      String itemSubjectRef = this.resolveName(dataInputElement.attribute("itemSubjectRef"));
      ItemDefinition itemDefinition = this.itemDefinitions.get(itemSubjectRef);
      Data dataInput = new Data(this.targetNamespace + ":" + id, id, itemDefinition);
      ioSpecification.addInput(dataInput);
    }

    for (Element dataOutputElement : ioSpecificationElement.elements("dataOutput")) {
      String id = dataOutputElement.attribute("id");
      String itemSubjectRef = this.resolveName(dataOutputElement.attribute("itemSubjectRef"));
      ItemDefinition itemDefinition = this.itemDefinitions.get(itemSubjectRef);
      Data dataOutput = new Data(this.targetNamespace + ":" + id, id, itemDefinition);
      ioSpecification.addOutput(dataOutput);
    }

    for (Element inputSetElement : ioSpecificationElement.elements("inputSet")) {
      for (Element dataInputRef : inputSetElement.elements("dataInputRefs")) {
        DataRef dataRef = new DataRef(dataInputRef.getText());
        ioSpecification.addInputRef(dataRef);
      }
    }

    for (Element outputSetElement : ioSpecificationElement.elements("outputSet")) {
      for (Element dataInputRef : outputSetElement.elements("dataOutputRefs")) {
        DataRef dataRef = new DataRef(dataInputRef.getText());
        ioSpecification.addOutputRef(dataRef);
      }
    }

    return ioSpecification;
  }

  protected AbstractDataAssociation parseDataInputAssociation(Element dataAssociationElement) {
    String sourceRef = dataAssociationElement.element("sourceRef").getText();
    String targetRef = dataAssociationElement.element("targetRef").getText();

    List<Element> assignments = dataAssociationElement.elements("assignment");
    if (assignments.isEmpty()) {
      return new MessageImplicitDataInputAssociation(sourceRef, targetRef);
    } else {
      SimpleDataInputAssociation dataAssociation = new SimpleDataInputAssociation(sourceRef, targetRef);

      for (Element assigmentElement : dataAssociationElement.elements("assignment")) {
        Expression from = this.expressionManager.createExpression(assigmentElement.element("from").getText());
        Expression to = this.expressionManager.createExpression(assigmentElement.element("to").getText());
        Assignment assignment = new Assignment(from, to);
        dataAssociation.addAssignment(assignment);
      }

      return dataAssociation;
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
  public void parseStartEvents(Element parentElement, ScopeImpl scope) {
    List<Element> startEventElements = parentElement.elements("startEvent");
    if (startEventElements.size() > 1) {
      addError("Multiple start events are currently unsupported", parentElement);
    } else if (startEventElements.size() > 0) {

      Element startEventElement = startEventElements.get(0);
      ActivityImpl startEventActivity = createActivityOnScope(startEventElement, scope);

      if (scope instanceof ProcessDefinitionEntity) {
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) scope;
        if (processDefinition.getInitial() != null) {
          // in order to support this, the initial should here be replaced with
          // a kind of hidden decision activity that has pvm transitions to all
          // of the visible bpmn start events
          addError("multiple startEvents in a process definition are not yet supported", startEventElement);
        }
        processDefinition.setInitial(startEventActivity);

        StartFormHandler startFormHandler;
        String startFormHandlerClassName = startEventElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "formHandlerClass");
        if (startFormHandlerClassName != null) {
          startFormHandler = (StartFormHandler) ReflectUtil.instantiate(startFormHandlerClassName);
        } else {
          startFormHandler = new DefaultStartFormHandler();
        }
        startFormHandler.parseConfiguration(startEventElement, deployment, processDefinition, this);

        processDefinition.setStartFormHandler(startFormHandler);

        String initiatorVariableName = startEventElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "initiator");
        if (initiatorVariableName != null) {
          processDefinition.setProperty(PROPERTYNAME_INITIATOR_VARIABLE_NAME, initiatorVariableName);
        }
        // only allowed for process...
        Element timerEventDefinition = startEventElement.element("timerEventDefinition");
        if (timerEventDefinition != null) {
          parseTimerStartEventDefinition(timerEventDefinition, startEventActivity, processDefinition);
        }
      } else {
        scope.setProperty(PROPERTYNAME_INITIAL, startEventActivity);
      }

      // Currently only none start events supported

      // TODO: a subprocess is only allowed to have a none start event
      startEventActivity.setActivityBehavior(new NoneStartEventActivityBehavior());

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseStartEvent(startEventElement, scope, startEventActivity);
      }
    }
  }

  /**
   * Parses the activities of a certain level in the process (process,
   * subprocess or another scope).
   * 
   * @param parentElement
   *          The 'parent' element that contains the activities (process,
   *          subprocess).
   * @param scopeElement
   *          The {@link ScopeImpl} to which the activities must be added.
   */
  public void parseActivities(Element parentElement, ScopeImpl scopeElement) {
    for (Element activityElement : parentElement.elements()) {
      ActivityImpl activity = null;
      if (activityElement.getTagName().equals("exclusiveGateway")) {
        activity = parseExclusiveGateway(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("inclusiveGateway")) {
        activity = parseInclusiveGateway(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("parallelGateway")) {
        activity = parseParallelGateway(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("scriptTask")) {
        activity = parseScriptTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("serviceTask")) {
        activity = parseServiceTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("businessRuleTask")) {
        activity = parseBusinessRuleTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("task")) {
        activity = parseTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("manualTask")) {
        activity = parseManualTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("userTask")) {
        activity = parseUserTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("sendTask")) {
        activity = parseSendTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("receiveTask")) {
        activity = parseReceiveTask(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("subProcess")) {
        activity = parseSubProcess(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("callActivity")) {
        activity = parseCallActivity(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("intermediateCatchEvent")) {
        activity = parseIntermediateCatchEvent(activityElement, scopeElement);
      } else if (activityElement.getTagName().equals("adHocSubProcess") || activityElement.getTagName().equals("complexGateway")
              || activityElement.getTagName().equals("eventBasedGateway") || activityElement.getTagName().equals("transaction")) {
        addWarning("Ignoring unsupported activity type", activityElement);
      }

      // Parse stuff common to activities above
      if (activity != null) {
        parseMultiInstanceLoopCharacteristics(activityElement, activity);
      }
    }
  }

  private ActivityImpl parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scopeElement) {
    ActivityImpl nestedActivity = createActivityOnScope(intermediateEventElement, scopeElement);

    // Catch event behavior is the same for all types
    nestedActivity.setActivityBehavior(new IntermediateCatchEventActivitiBehaviour());

    Element timerEventDefinition = intermediateEventElement.element("timerEventDefinition");
    if (timerEventDefinition != null) {
      parseIntemediateTimerEventDefinition(timerEventDefinition, nestedActivity);
    } else {
      addError("Unsupported intermediate event type", intermediateEventElement);
    }
    return nestedActivity;
  }

  /**
   * Parses loopCharacteristics (standardLoop/Multi-instance) of an activity, if
   * any is defined.
   */
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, ActivityImpl activity) {

    // Only 'activities' (in the BPMN 2.0 spec meaning) can have mi
    // characteristics
    if (!(activity.getActivityBehavior() instanceof AbstractBpmnActivityBehavior)) {
      return;
    }

    Element miLoopCharacteristics = activityElement.element("multiInstanceLoopCharacteristics");
    if (miLoopCharacteristics != null) {

      MultiInstanceActivityBehavior miActivityBehavior = null;
      boolean isSequential = parseBooleanAttribute(miLoopCharacteristics.attribute("isSequential"), false);
      if (isSequential) {
        miActivityBehavior = new SequentialMultiInstanceBehavior(activity, (AbstractBpmnActivityBehavior) activity.getActivityBehavior());
      } else {
        miActivityBehavior = new ParallelMultiInstanceBehavior(activity, (AbstractBpmnActivityBehavior) activity.getActivityBehavior());
      }
      activity.setScope(true);
      activity.setProperty("multiInstance", isSequential ? "sequential" : "parallel");
      activity.setActivityBehavior(miActivityBehavior);

      // loopCardinality
      Element loopCardinality = miLoopCharacteristics.element("loopCardinality");
      if (loopCardinality != null) {
        String loopCardinalityText = loopCardinality.getText();
        if (loopCardinalityText == null || "".equals(loopCardinalityText)) {
          addError("loopCardinality must be defined for a multiInstanceLoopCharacteristics definition ", miLoopCharacteristics);
        }
        miActivityBehavior.setLoopCardinalityExpression(expressionManager.createExpression(loopCardinalityText));
      }

      // completionCondition
      Element completionCondition = miLoopCharacteristics.element("completionCondition");
      if (completionCondition != null) {
        String completionConditionText = completionCondition.getText();
        miActivityBehavior.setCompletionConditionExpression(expressionManager.createExpression(completionConditionText));
      }

      // activiti:collection
      String collection = miLoopCharacteristics.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "collection");
      if (collection != null) {
        if (collection.contains("{")) {
          miActivityBehavior.setCollectionExpression(expressionManager.createExpression(collection));
        } else {
          miActivityBehavior.setCollectionVariable(collection);
        }
      }

      // loopDataInputRef
      Element loopDataInputRef = miLoopCharacteristics.element("loopDataInputRef");
      if (loopDataInputRef != null) {
        String loopDataInputRefText = loopDataInputRef.getText();
        if (loopDataInputRefText != null) {
          if (loopDataInputRefText.contains("{")) {
            miActivityBehavior.setCollectionExpression(expressionManager.createExpression(loopDataInputRefText));
          } else {
            miActivityBehavior.setCollectionVariable(loopDataInputRefText);
          }
        }
      }

      // activiti:elementVariable
      String elementVariable = miLoopCharacteristics.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "elementVariable");
      if (elementVariable != null) {
        miActivityBehavior.setCollectionElementVariable(elementVariable);
      }

      // dataInputItem
      Element inputDataItem = miLoopCharacteristics.element("inputDataItem");
      if (inputDataItem != null) {
        String inputDataItemName = inputDataItem.attribute("name");
        miActivityBehavior.setCollectionElementVariable(inputDataItemName);
      }

      // Validation
      if (miActivityBehavior.getLoopCardinalityExpression() == null && miActivityBehavior.getCollectionExpression() == null
              && miActivityBehavior.getCollectionVariable() == null) {
        addError("Either loopCardinality or loopDataInputRef/activiti:collection must been set", miLoopCharacteristics);
      }

      // Validation
      if (miActivityBehavior.getCollectionExpression() == null && miActivityBehavior.getCollectionVariable() == null
              && miActivityBehavior.getCollectionElementVariable() != null) {
        addError("LoopDataInputRef/activiti:collection must be set when using inputDataItem or activiti:elementVariable", miLoopCharacteristics);
      }

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseMultiInstanceLoopCharacteristics(activityElement, miLoopCharacteristics, activity);
      }

    }
  }

  /**
   * Parses the generic information of an activity element (id, name,
   * documentation, etc.), and creates a new {@link ActivityImpl} on the given
   * scope element.
   */
  public ActivityImpl createActivityOnScope(Element activityElement, ScopeImpl scopeElement) {
    String id = activityElement.attribute("id");
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("Parsing activity " + id);
    }
    ActivityImpl activity = scopeElement.createActivity(id);

    activity.setProperty("name", activityElement.attribute("name"));
    activity.setProperty("documentation", parseDocumentation(activityElement));
    activity.setProperty("default", activityElement.attribute("default"));
    activity.setProperty("type", activityElement.getTagName());
    activity.setProperty("line", activityElement.getLine());
    return activity;
  }

  public String parseDocumentation(Element element) {
    Element docElement = element.element("documentation");
    if (docElement != null) {
      return docElement.getText().trim();
    }
    return null;
  }

  /**
   * Parses an exclusive gateway declaration.
   */
  public ActivityImpl parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(exclusiveGwElement, scope);
    activity.setActivityBehavior(new ExclusiveGatewayActivityBehavior());

    parseExecutionListenersOnScope(exclusiveGwElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseExclusiveGateway(exclusiveGwElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses an inclusive gateway declaration.
   */
  public ActivityImpl parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(inclusiveGwElement, scope);
    activity.setActivityBehavior(new InclusiveGatewayActivityBehavior());

    parseExecutionListenersOnScope(inclusiveGwElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseInclusiveGateway(inclusiveGwElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a parallel gateway declaration.
   */
  public ActivityImpl parseParallelGateway(Element parallelGwElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(parallelGwElement, scope);
    activity.setActivityBehavior(new ParallelGatewayActivityBehavior());

    parseExecutionListenersOnScope(parallelGwElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseParallelGateway(parallelGwElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a scriptTask declaration.
   */
  public ActivityImpl parseScriptTask(Element scriptTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(scriptTaskElement, scope);
   
    String script = null;
    String language = null;
    String resultVariableName = null;

    Element scriptElement = scriptTaskElement.element("script");
    if (scriptElement != null) {
      script = scriptElement.getText();

      if (language == null) {
        language = scriptTaskElement.attribute("scriptFormat");
      }

      if (language == null) {
        language = ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE;
      }

      resultVariableName = scriptTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariable");
      if (resultVariableName == null) {
        // for backwards compatible reasons
        resultVariableName = scriptTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariableName");
      }
    }
    
    String async = scriptTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async");
    activity.setAsync("true".equals(async));

    activity.setActivityBehavior(new ScriptTaskActivityBehavior(script, language, resultVariableName));

    parseExecutionListenersOnScope(scriptTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseScriptTask(scriptTaskElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a serviceTask declaration.
   */
  public ActivityImpl parseServiceTask(Element serviceTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(serviceTaskElement, scope);

    String type = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "type");
    String className = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "class");
    String expression = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "expression");
    String delegateExpression = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "delegateExpression");
    String resultVariableName = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariable");
    String async = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async");
    if (resultVariableName == null) {
      resultVariableName = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariableName");
    }
    String implementation = serviceTaskElement.attribute("implementation");
    String operationRef = this.resolveName(serviceTaskElement.attribute("operationRef"));
    
    activity.setAsync("true".equals(async));

    if (type != null) {
      if (type.equalsIgnoreCase("mail")) {
        parseEmailServiceTask(activity, serviceTaskElement, parseFieldDeclarations(serviceTaskElement));
      } else if (type.equalsIgnoreCase("mule")) {
        parseMuleServiceTask(activity, serviceTaskElement, parseFieldDeclarations(serviceTaskElement));
      } else {
        addError("Invalid usage of type attribute: '" + type + "'", serviceTaskElement);
      }

    } else if (className != null && className.trim().length() > 0) {
      if (resultVariableName != null) {
        addError("'resultVariableName' not supported for service tasks using 'class'", serviceTaskElement);
      }
      activity.setActivityBehavior(new ClassDelegate(className, parseFieldDeclarations(serviceTaskElement)));

    } else if (delegateExpression != null) {
      if (resultVariableName != null) {
        addError("'resultVariableName' not supported for service tasks using 'delegateExpression'", serviceTaskElement);
      }
      activity.setActivityBehavior(new ServiceTaskDelegateExpressionActivityBehavior(expressionManager.createExpression(delegateExpression)));

    } else if (expression != null && expression.trim().length() > 0) {
      activity.setActivityBehavior(new ServiceTaskExpressionActivityBehavior(expressionManager.createExpression(expression), resultVariableName));

    } else if (implementation != null && operationRef != null && implementation.equalsIgnoreCase("##WebService")) {
      if (!this.operations.containsKey(operationRef)) {
        addError(operationRef + " does not exist", serviceTaskElement);
      } else {
        Operation operation = this.operations.get(operationRef);
        WebServiceActivityBehavior webServiceActivityBehavior = new WebServiceActivityBehavior(operation);

        Element ioSpecificationElement = serviceTaskElement.element("ioSpecification");
        if (ioSpecificationElement != null) {
          IOSpecification ioSpecification = this.parseIOSpecification(ioSpecificationElement);
          webServiceActivityBehavior.setIoSpecification(ioSpecification);
        }

        for (Element dataAssociationElement : serviceTaskElement.elements("dataInputAssociation")) {
          AbstractDataAssociation dataAssociation = this.parseDataInputAssociation(dataAssociationElement);
          webServiceActivityBehavior.addDataInputAssociation(dataAssociation);
        }

        for (Element dataAssociationElement : serviceTaskElement.elements("dataOutputAssociation")) {
          AbstractDataAssociation dataAssociation = this.parseDataOutputAssociation(dataAssociationElement);
          webServiceActivityBehavior.addDataOutputAssociation(dataAssociation);
        }

        activity.setActivityBehavior(webServiceActivityBehavior);
      }
    } else {
      addError("One of the attributes 'class', 'delegateExpression', 'type', 'operation', or 'expression' is mandatory on serviceTask.", serviceTaskElement);
    }

    parseExecutionListenersOnScope(serviceTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseServiceTask(serviceTaskElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a businessRuleTask declaration.
   */
  public ActivityImpl parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(businessRuleTaskElement, scope);

    BusinessRuleTaskActivityBehavior ruleActivity = new BusinessRuleTaskActivityBehavior();

    String ruleVariableInputString = businessRuleTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "ruleVariablesInput");
    String rulesString = businessRuleTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "rules");
    String excludeString = businessRuleTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "exclude");
    String resultVariableNameString = businessRuleTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariable");
    
    String async = businessRuleTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async");
    activity.setAsync("true".equals(async));
    
    if (resultVariableNameString == null) {
      resultVariableNameString = businessRuleTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "resultVariableName");
    }

    if (ruleVariableInputString != null) {
      String[] ruleVariableInputObjects = ruleVariableInputString.split(",");
      for (String ruleVariableInputObject : ruleVariableInputObjects) {
        ruleActivity.addRuleVariableInputIdExpression(expressionManager.createExpression(ruleVariableInputObject.trim()));
      }
    }

    if (rulesString != null) {
      String[] rules = rulesString.split(",");
      for (String rule : rules) {
        ruleActivity.addRuleIdExpression(expressionManager.createExpression(rule.trim()));
      }

      if (excludeString != null) {
        excludeString = excludeString.trim();
        if ("true".equalsIgnoreCase(excludeString) == false && "false".equalsIgnoreCase(excludeString) == false) {
          addError("'exclude' only supports true or false for business rule tasks", businessRuleTaskElement);

        } else {
          ruleActivity.setExclude(Boolean.valueOf(excludeString.toLowerCase()));
        }
      }

    } else if (excludeString != null) {
      addError("'exclude' not supported for business rule tasks not defining 'rules'", businessRuleTaskElement);
    }

    if (resultVariableNameString != null) {
      resultVariableNameString = resultVariableNameString.trim();
      if (resultVariableNameString.length() > 0 == false) {
        addError("'resultVariable' must contain a text value for business rule tasks", businessRuleTaskElement);

      } else {
        ruleActivity.setResultVariable(resultVariableNameString);
      }
    } else {
      ruleActivity.setResultVariable("org.activiti.engine.rules.OUTPUT");
    }

    activity.setActivityBehavior(ruleActivity);

    parseExecutionListenersOnScope(businessRuleTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBusinessRuleTask(businessRuleTaskElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a sendTask declaration.
   */
  public ActivityImpl parseSendTask(Element sendTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(sendTaskElement, scope);
    
    String async = sendTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async");
    activity.setAsync("true".equals(async));

    // for e-mail
    String type = sendTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "type");

    // for web service
    String implementation = sendTaskElement.attribute("implementation");
    String operationRef = this.resolveName(sendTaskElement.attribute("operationRef"));

    // for e-mail
    if (type != null) {
      if (type.equalsIgnoreCase("mail")) {
        parseEmailServiceTask(activity, sendTaskElement, parseFieldDeclarations(sendTaskElement));
      } else if (type.equalsIgnoreCase("mule")) {
        parseMuleServiceTask(activity, sendTaskElement, parseFieldDeclarations(sendTaskElement));
      } else {
        addError("Invalid usage of type attribute: '" + type + "'", sendTaskElement);
      }

      // for web service
    } else if (implementation != null && operationRef != null && implementation.equalsIgnoreCase("##WebService")) {
      if (!this.operations.containsKey(operationRef)) {
        addError(operationRef + " does not exist", sendTaskElement);
      } else {
        Operation operation = this.operations.get(operationRef);
        WebServiceActivityBehavior webServiceActivityBehavior = new WebServiceActivityBehavior(operation);

        Element ioSpecificationElement = sendTaskElement.element("ioSpecification");
        if (ioSpecificationElement != null) {
          IOSpecification ioSpecification = this.parseIOSpecification(ioSpecificationElement);
          webServiceActivityBehavior.setIoSpecification(ioSpecification);
        }

        for (Element dataAssociationElement : sendTaskElement.elements("dataInputAssociation")) {
          AbstractDataAssociation dataAssociation = this.parseDataInputAssociation(dataAssociationElement);
          webServiceActivityBehavior.addDataInputAssociation(dataAssociation);
        }

        for (Element dataAssociationElement : sendTaskElement.elements("dataOutputAssociation")) {
          AbstractDataAssociation dataAssociation = this.parseDataOutputAssociation(dataAssociationElement);
          webServiceActivityBehavior.addDataOutputAssociation(dataAssociation);
        }

        activity.setActivityBehavior(webServiceActivityBehavior);
      }
    } else {
      addError("One of the attributes 'type' or 'operation' is mandatory on sendTask.", sendTaskElement);
    }

    parseExecutionListenersOnScope(sendTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseSendTask(sendTaskElement, scope, activity);
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

  protected void parseMuleServiceTask(ActivityImpl activity, Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
    try {
      Class< ? > theClass = Class.forName("org.activiti.mule.MuleSendActivitiBehavior");
      activity.setActivityBehavior((ActivityBehavior) ClassDelegate.instantiateDelegate(theClass, fieldDeclarations));
    } catch (ClassNotFoundException e) {
      addError("Could not find org.activiti.mule.MuleSendActivitiBehavior", serviceTaskElement);
    }
  }

  protected void parseEmailServiceTask(ActivityImpl activity, Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
    validateFieldDeclarationsForEmail(serviceTaskElement, fieldDeclarations);
    activity.setActivityBehavior((MailActivityBehavior) ClassDelegate.instantiateDelegate(MailActivityBehavior.class, fieldDeclarations));
  }

  protected void validateFieldDeclarationsForEmail(Element serviceTaskElement, List<FieldDeclaration> fieldDeclarations) {
    boolean toDefined = false;
    boolean textOrHtmlDefined = false;
    for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
      if (fieldDeclaration.getName().equals("to")) {
        toDefined = true;
      }
      if (fieldDeclaration.getName().equals("html")) {
        textOrHtmlDefined = true;
      }
      if (fieldDeclaration.getName().equals("text")) {
        textOrHtmlDefined = true;
      }
    }

    if (!toDefined) {
      addError("No recipient is defined on the mail activity", serviceTaskElement);
    }
    if (!textOrHtmlDefined) {
      addError("Text or html field should be provided", serviceTaskElement);
    }
  }

  public List<FieldDeclaration> parseFieldDeclarations(Element element) {
    List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();

    Element elementWithFieldInjections = element.element("extensionElements");
    if (elementWithFieldInjections == null) { // Custom extensions will just
                                              // have the <field.. as a
                                              // subelement
      elementWithFieldInjections = element;
    }
    List<Element> fieldDeclarationElements = elementWithFieldInjections.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "field");
    if (fieldDeclarationElements != null && !fieldDeclarationElements.isEmpty()) {

      for (Element fieldDeclarationElement : fieldDeclarationElements) {
        FieldDeclaration fieldDeclaration = parseFieldDeclaration(element, fieldDeclarationElement);
        if (fieldDeclaration != null) {
          fieldDeclarations.add(fieldDeclaration);
        }
      }
    }

    return fieldDeclarations;
  }

  protected FieldDeclaration parseFieldDeclaration(Element serviceTaskElement, Element fieldDeclarationElement) {
    String fieldName = fieldDeclarationElement.attribute("name");

    FieldDeclaration fieldDeclaration = parseStringFieldDeclaration(fieldDeclarationElement, serviceTaskElement, fieldName);
    if (fieldDeclaration == null) {
      fieldDeclaration = parseExpressionFieldDeclaration(fieldDeclarationElement, serviceTaskElement, fieldName);
    }

    if (fieldDeclaration == null) {
      addError("One of the following is mandatory on a field declaration: one of attributes stringValue|expression "
              + "or one of child elements string|expression", serviceTaskElement);
    }
    return fieldDeclaration;
  }

  protected FieldDeclaration parseStringFieldDeclaration(Element fieldDeclarationElement, Element serviceTaskElement, String fieldName) {
    try {
      String fieldValue = getStringValueFromAttributeOrElement("stringValue", "string", fieldDeclarationElement);
      if (fieldValue != null) {
        return new FieldDeclaration(fieldName, Expression.class.getName(), new FixedValue(fieldValue));
      }
    } catch (ActivitiException ae) {
      if (ae.getMessage().contains("multiple elements with tag name")) {
        addError("Multiple string field declarations found", serviceTaskElement);
      } else {
        addError("Error when paring field declarations: " + ae.getMessage(), serviceTaskElement);
      }
    }
    return null;
  }

  protected FieldDeclaration parseExpressionFieldDeclaration(Element fieldDeclarationElement, Element serviceTaskElement, String fieldName) {
    try {
      String expression = getStringValueFromAttributeOrElement("expression", "expression", fieldDeclarationElement);
      if (expression != null && expression.trim().length() > 0) {
        return new FieldDeclaration(fieldName, Expression.class.getName(), expressionManager.createExpression(expression));
      }
    } catch (ActivitiException ae) {
      if (ae.getMessage().contains("multiple elements with tag name")) {
        addError("Multiple expression field declarations found", serviceTaskElement);
      } else {
        addError("Error when paring field declarations: " + ae.getMessage(), serviceTaskElement);
      }
    }
    return null;
  }

  protected String getStringValueFromAttributeOrElement(String attributeName, String elementName, Element element) {
    String value = null;

    String attributeValue = element.attribute(attributeName);
    Element childElement = element.elementNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, elementName);
    String stringElementText = null;

    if (attributeValue != null && childElement != null) {
      addError("Can't use attribute '" + attributeName + "' and element '" + elementName + "' together, only use one", element);
    } else if (childElement != null) {
      stringElementText = childElement.getText();
      if (stringElementText == null || stringElementText.length() == 0) {
        addError("No valid value found in attribute '" + attributeName + "' nor element '" + elementName + "'", element);
      } else {
        // Use text of element
        value = stringElementText;
      }
    } else if (attributeValue != null && attributeValue.length() > 0) {
      // Using attribute
      value = attributeValue;
    }

    return value;
  }

  /**
   * Parses a task with no specific type (behaves as passthrough).
   */
  public ActivityImpl parseTask(Element taskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(taskElement, scope);
    activity.setActivityBehavior(new TaskActivityBehavior());
    
    String async = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async");
    activity.setAsync("true".equals(async));

    parseExecutionListenersOnScope(taskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseTask(taskElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a manual task.
   */
  public ActivityImpl parseManualTask(Element manualTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(manualTaskElement, scope);
    activity.setActivityBehavior(new ManualTaskActivityBehavior());

    parseExecutionListenersOnScope(manualTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseManualTask(manualTaskElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a receive task.
   */
  public ActivityImpl parseReceiveTask(Element receiveTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(receiveTaskElement, scope);
    activity.setActivityBehavior(new ReceiveTaskActivityBehavior());
    
    String async = receiveTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async");
    activity.setAsync("true".equals(async));

    parseExecutionListenersOnScope(receiveTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseManualTask(receiveTaskElement, scope, activity);
    }
    return activity;
  }

  /* userTask specific finals */

  protected static final String HUMAN_PERFORMER = "humanPerformer";
  protected static final String POTENTIAL_OWNER = "potentialOwner";

  protected static final String RESOURCE_ASSIGNMENT_EXPR = "resourceAssignmentExpression";
  protected static final String FORMAL_EXPRESSION = "formalExpression";

  protected static final String USER_PREFIX = "user(";
  protected static final String GROUP_PREFIX = "group(";

  protected static final String ASSIGNEE_EXTENSION = "assignee";
  protected static final String CANDIDATE_USERS_EXTENSION = "candidateUsers";
  protected static final String CANDIDATE_GROUPS_EXTENSION = "candidateGroups";
  protected static final String DUE_DATE_EXTENSION = "dueDate";

  /**
   * Parses a userTask declaration.
   */
  public ActivityImpl parseUserTask(Element userTaskElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(userTaskElement, scope);
    
    String async = userTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async");
    activity.setAsync("true".equals(async));    
    
    TaskDefinition taskDefinition = parseTaskDefinition(userTaskElement, activity.getId(), (ProcessDefinitionEntity) scope.getProcessDefinition());

    UserTaskActivityBehavior userTaskActivity = new UserTaskActivityBehavior(expressionManager, taskDefinition);
    activity.setActivityBehavior(userTaskActivity);

    parseProperties(userTaskElement, activity);
    parseExecutionListenersOnScope(userTaskElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseUserTask(userTaskElement, scope, activity);
    }
    return activity;
  }

  public TaskDefinition parseTaskDefinition(Element taskElement, String taskDefinitionKey, ProcessDefinitionEntity processDefinition) {
    TaskFormHandler taskFormHandler;
    String taskFormHandlerClassName = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "formHandlerClass");
    if (taskFormHandlerClassName != null) {
      taskFormHandler = (TaskFormHandler) ReflectUtil.instantiate(taskFormHandlerClassName);
    } else {
      taskFormHandler = new DefaultTaskFormHandler();
    }
    taskFormHandler.parseConfiguration(taskElement, deployment, processDefinition, this);

    TaskDefinition taskDefinition = new TaskDefinition(taskFormHandler);

    taskDefinition.setKey(taskDefinitionKey);
    processDefinition.getTaskDefinitions().put(taskDefinitionKey, taskDefinition);

    String name = taskElement.attribute("name");
    if (name != null) {
      taskDefinition.setNameExpression(expressionManager.createExpression(name));
    }

    String descriptionStr = parseDocumentation(taskElement);
    if (descriptionStr != null) {
      taskDefinition.setDescriptionExpression(expressionManager.createExpression(descriptionStr));
    }

    parseHumanPerformer(taskElement, taskDefinition);
    parsePotentialOwner(taskElement, taskDefinition);

    // Activiti custom extension
    parseUserTaskCustomExtensions(taskElement, taskDefinition);

    return taskDefinition;
  }

  protected void parseHumanPerformer(Element taskElement, TaskDefinition taskDefinition) {
    List<Element> humanPerformerElements = taskElement.elements(HUMAN_PERFORMER);

    if (humanPerformerElements.size() > 1) {
      addError("Invalid task definition: multiple " + HUMAN_PERFORMER + " sub elements defined for " + taskDefinition.getNameExpression(), taskElement);
    } else if (humanPerformerElements.size() == 1) {
      Element humanPerformerElement = humanPerformerElements.get(0);
      if (humanPerformerElement != null) {
        parseHumanPerformerResourceAssignment(humanPerformerElement, taskDefinition);
      }
    }
  }

  protected void parsePotentialOwner(Element taskElement, TaskDefinition taskDefinition) {
    List<Element> potentialOwnerElements = taskElement.elements(POTENTIAL_OWNER);
    for (Element potentialOwnerElement : potentialOwnerElements) {
      parsePotentialOwnerResourceAssignment(potentialOwnerElement, taskDefinition);
    }
  }

  protected void parseHumanPerformerResourceAssignment(Element performerElement, TaskDefinition taskDefinition) {
    Element raeElement = performerElement.element(RESOURCE_ASSIGNMENT_EXPR);
    if (raeElement != null) {
      Element feElement = raeElement.element(FORMAL_EXPRESSION);
      if (feElement != null) {
        taskDefinition.setAssigneeExpression(expressionManager.createExpression(feElement.getText()));
      }
    }
  }

  protected void parsePotentialOwnerResourceAssignment(Element performerElement, TaskDefinition taskDefinition) {
    Element raeElement = performerElement.element(RESOURCE_ASSIGNMENT_EXPR);
    if (raeElement != null) {
      Element feElement = raeElement.element(FORMAL_EXPRESSION);
      if (feElement != null) {
        List<String> assignmentExpressions = parseCommaSeparatedList(feElement.getText());
        for (String assignmentExpression : assignmentExpressions) {
          assignmentExpression = assignmentExpression.trim();
          if (assignmentExpression.startsWith(USER_PREFIX)) {
            String userAssignementId = getAssignmentId(assignmentExpression, USER_PREFIX);
            taskDefinition.addCandidateUserIdExpression(expressionManager.createExpression(userAssignementId));
          } else if (assignmentExpression.startsWith(GROUP_PREFIX)) {
            String groupAssignementId = getAssignmentId(assignmentExpression, GROUP_PREFIX);
            taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(groupAssignementId));
          } else { // default: given string is a goupId, as-is.
            taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(assignmentExpression));
          }
        }
      }
    }
  }

  protected String getAssignmentId(String expression, String prefix) {
    return expression.substring(prefix.length(), expression.length() - 1).trim();
  }

  protected void parseUserTaskCustomExtensions(Element taskElement, TaskDefinition taskDefinition) {

    // assignee
    String assignee = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, ASSIGNEE_EXTENSION);
    if (assignee != null) {
      if (taskDefinition.getAssigneeExpression() == null) {
        taskDefinition.setAssigneeExpression(expressionManager.createExpression(assignee));
      } else {
        addError("Invalid usage: duplicate assignee declaration for task " + taskDefinition.getNameExpression(), taskElement);
      }
    }

    // Candidate users
    String candidateUsersString = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, CANDIDATE_USERS_EXTENSION);
    if (candidateUsersString != null) {
      List<String> candidateUsers = parseCommaSeparatedList(candidateUsersString);
      for (String candidateUser : candidateUsers) {
        taskDefinition.addCandidateUserIdExpression(expressionManager.createExpression(candidateUser.trim()));
      }
    }

    // Candidate groups
    String candidateGroupsString = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, CANDIDATE_GROUPS_EXTENSION);
    if (candidateGroupsString != null) {
      List<String> candidateGroups = parseCommaSeparatedList(candidateGroupsString);
      for (String candidateGroup : candidateGroups) {
        taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(candidateGroup.trim()));
      }
    }

    // Task listeners
    parseTaskListeners(taskElement, taskDefinition);

    // Due date
    String dueDateExpression = taskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, DUE_DATE_EXTENSION);
    if (dueDateExpression != null) {
      taskDefinition.setDueDateExpression(expressionManager.createExpression(dueDateExpression));
    }
  }

  /**
   * Parses the given String as a list of comma separated entries, where an
   * entry can possibly be an expression that has comma's.
   * 
   * If somebody is smart enough to write a regex for this, please let us know.
   * 
   * @return the entries of the comma separated list, trimmed.
   */
  protected List<String> parseCommaSeparatedList(String s) {
    List<String> result = new ArrayList<String>();
    if (s != null && !"".equals(s)) {

      StringCharacterIterator iterator = new StringCharacterIterator(s);
      char c = iterator.first();

      StringBuilder strb = new StringBuilder();
      boolean insideExpression = false;

      while (c != StringCharacterIterator.DONE) {
        if (c == '{' || c == '$') {
          insideExpression = true;
        } else if (c == '}') {
          insideExpression = false;
        } else if (c == ',' && !insideExpression) {
          result.add(strb.toString().trim());
          strb.delete(0, strb.length());
        }

        if (c != ',' || (insideExpression)) {
          strb.append(c);
        }

        c = iterator.next();
      }

      if (strb.length() > 0) {
        result.add(strb.toString().trim());
      }

    }
    return result;
  }

  protected void parseTaskListeners(Element userTaskElement, TaskDefinition taskDefinition) {
    Element extentionsElement = userTaskElement.element("extensionElements");
    if (extentionsElement != null) {
      List<Element> taskListenerElements = extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "taskListener");
      for (Element taskListenerElement : taskListenerElements) {
        String eventName = taskListenerElement.attribute("event");
        if (eventName != null) {
          if (TaskListener.EVENTNAME_CREATE.equals(eventName) || TaskListener.EVENTNAME_ASSIGNMENT.equals(eventName)
                  || TaskListener.EVENTNAME_COMPLETE.equals(eventName)) {
            TaskListener taskListener = parseTaskListener(taskListenerElement);
            taskDefinition.addTaskListener(eventName, taskListener);
          } else {
            addError("Invalid eventName for taskListener: choose 'create' |'assignment'", userTaskElement);
          }
        } else {
          addError("Event is mandatory on taskListener", userTaskElement);
        }
      }
    }
  }

  protected TaskListener parseTaskListener(Element taskListenerElement) {
    TaskListener taskListener = null;

    String className = taskListenerElement.attribute("class");
    String expression = taskListenerElement.attribute("expression");
    String delegateExpression = taskListenerElement.attribute("delegateExpression");

    if (className != null) {
      taskListener = new ClassDelegate(className, parseFieldDeclarations(taskListenerElement));
    } else if (expression != null) {
      taskListener = new ExpressionTaskListener(expressionManager.createExpression(expression));
    } else if (delegateExpression != null) {
      taskListener = new DelegateExpressionTaskListener(expressionManager.createExpression(delegateExpression));
    } else {
      addError("Element 'class' or 'expression' is mandatory on taskListener", taskListenerElement);
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
  public void parseEndEvents(Element parentElement, ScopeImpl scope) {
    for (Element endEventElement : parentElement.elements("endEvent")) {
      ActivityImpl activity = createActivityOnScope(endEventElement, scope);

      Element errorEventDefinition = endEventElement.element("errorEventDefinition");
      if (errorEventDefinition != null) { // error end event
        String errorRef = errorEventDefinition.attribute("errorRef");
        if (errorRef == null || "".equals(errorRef)) {
          addError("'errorRef' attribute is mandatory on error end event", errorEventDefinition);
        } else {
          Error error = errors.get(errorRef);
          activity.setProperty("type", "errorEndEvent");
          activity.setActivityBehavior(new ErrorEndEventActivityBehavior(error != null ? error.getErrorCode() : errorRef));
        }
      } else { // default: none end event
        activity.setActivityBehavior(new NoneEndEventActivityBehavior());
      }

      for (BpmnParseListener parseListener : parseListeners) {
        parseListener.parseEndEvent(endEventElement, scope, activity);
      }
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
  public void parseBoundaryEvents(Element parentElement, ScopeImpl scopeElement) {
    for (Element boundaryEventElement : parentElement.elements("boundaryEvent")) {

      // The boundary event is attached to an activity, reference by the
      // 'attachedToRef' attribute
      String attachedToRef = boundaryEventElement.attribute("attachedToRef");
      if (attachedToRef == null || attachedToRef.equals("")) {
        addError("AttachedToRef is required when using a timerEventDefinition", boundaryEventElement);
      }

      // Representation structure-wise is a nested activity in the activity to
      // which its attached
      String id = boundaryEventElement.attribute("id");
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Parsing boundary event " + id);
      }

      ActivityImpl parentActivity = scopeElement.findActivity(attachedToRef);
      if (parentActivity == null) {
        addError("Invalid reference in boundary event. Make sure that the referenced activity is " + "defined in the same scope as the boundary event",
                boundaryEventElement);
      }
      ActivityImpl nestedActivity = createActivityOnScope(boundaryEventElement, parentActivity);

      String cancelActivity = boundaryEventElement.attribute("cancelActivity", "true");
      boolean interrupting = cancelActivity.equals("true") ? true : false;

      // Catch event behavior is the same for all types
      BoundaryEventActivityBehavior behavior = new BoundaryEventActivityBehavior(interrupting);
      nestedActivity.setActivityBehavior(behavior);

      // Depending on the sub-element definition, the correct activityBehavior
      // parsing is selected
      Element timerEventDefinition = boundaryEventElement.element("timerEventDefinition");
      Element errorEventDefinition = boundaryEventElement.element("errorEventDefinition");
      if (timerEventDefinition != null) {
        parseBoundaryTimerEventDefinition(timerEventDefinition, interrupting, nestedActivity);
      } else if (errorEventDefinition != null) {
        interrupting = true; // non-interrupting not yet supported
        parseBoundaryErrorEventDefinition(errorEventDefinition, interrupting, parentActivity, nestedActivity);
      } else {
        addError("Unsupported boundary event type", boundaryEventElement);
      }
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
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
    timerActivity.setProperty("type", "boundaryTimer");
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerExecuteNestedActivityJobHandler.TYPE);
    addTimerDeclaration(timerActivity.getParent(), timerDeclaration);

    if (timerActivity.getParent() instanceof ActivityImpl) {
      ((ActivityImpl) timerActivity.getParent()).setScope(true);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryTimerEventDefinition(timerEventDefinition, interrupting, timerActivity);
    }
  }

  private void parseTimerStartEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity, ProcessDefinitionEntity processDefinition) {
    timerActivity.setProperty("type", "startTimerEvent");
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerStartEventJobHandler.TYPE);
    timerDeclaration.setJobHandlerConfiguration(processDefinition.getKey());

    List<TimerDeclarationImpl> timerDeclarations = (List<TimerDeclarationImpl>) processDefinition.getProperty(PROPERTYNAME_START_TIMER);
    if (timerDeclarations == null) {
      timerDeclarations = new ArrayList<TimerDeclarationImpl>();
      processDefinition.setProperty(PROPERTYNAME_START_TIMER, timerDeclarations);
    }
    timerDeclarations.add(timerDeclaration);

  }

  private void parseIntemediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
    timerActivity.setProperty("type", "intermediateTimer");
    TimerDeclarationImpl timerDeclaration = parseTimer(timerEventDefinition, timerActivity, TimerCatchIntermediateEventJobHandler.TYPE);
    addTimerDeclaration(timerActivity, timerDeclaration);
    timerActivity.setScope(true);
    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseIntermediateTimerEventDefinition(timerEventDefinition, timerActivity);
    }
  }

  private TimerDeclarationImpl parseTimer(Element timerEventDefinition, ScopeImpl timerActivity, String jobHandlerType) {
    // TimeDate
    TimerDeclarationType type = TimerDeclarationType.DATE;
    Expression expression = parseExpression(timerEventDefinition, "timeDate");

    // TimeCycle
    if (expression == null) {
      type = TimerDeclarationType.CYCLE;
      expression = parseExpression(timerEventDefinition, "timeCycle");
    }

    // TimeDuration
    if (expression == null) {
      type = TimerDeclarationType.DURATION;
      expression = parseExpression(timerEventDefinition, "timeDuration");
    }

    // Parse the timer declaration
    // TODO move the timer declaration into the bpmn activity or next to the
    // TimerSession
    TimerDeclarationImpl timerDeclaration = new TimerDeclarationImpl(expression, type, jobHandlerType);
    timerDeclaration.setJobHandlerConfiguration(timerActivity.getId());
    return timerDeclaration;
  }

  private Expression parseExpression(Element parent, String name) {
    Element value = parent.element(name);
    if (value != null) {
      String expressionText = value.getText().trim();
      return expressionManager.createExpression(expressionText);
    }
    return null;
  }

  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {

    nestedErrorEventActivity.setProperty("type", "boundaryError");
    ((ActivityImpl) nestedErrorEventActivity.getParent()).setScope(true);

    String errorRef = errorEventDefinition.attribute("errorRef");
    Error error = null;
    if (errorRef != null) {
      error = errors.get(errorRef);
      nestedErrorEventActivity.setProperty("errorCode", error == null ? errorRef : error.getErrorCode());
    }

    // TODO: this can probably be made generic for all throw/catch ?

    // Search for all errorEvents that can throw the exception
    List<ActivityImpl> childErrorEndEvents = getAllChildActivitiesOfType("errorEndEvent", activity);
    for (ActivityImpl errorEndEvent : childErrorEndEvents) {
      ErrorEndEventActivityBehavior behavior = (ErrorEndEventActivityBehavior) errorEndEvent.getActivityBehavior();
      if (errorRef == null || errorRef.equals(behavior.getErrorCode()) || (error != null && error.getErrorCode().equals(behavior.getErrorCode()))) {

        ActivityImpl catchingActivity = null;
        if (behavior.getBorderEventActivityId() != null) {
          catchingActivity = activity.getProcessDefinition().findActivity(behavior.getBorderEventActivityId());
        }

        // If the error end event doesnt have a catching activity assigned yet,
        // we can just set it
        // If the error end event already has a catching activity event, we can
        // only set it
        // if the current activity is a child of the previous defined one
        // (ie. the current activity will catch and consume it, and it will
        // never reach the previous one,
        // since the previous one set is an activity which is a parent of the
        // more narrow catching activity)
        if (catchingActivity == null || isChildActivity(activity, catchingActivity)) {
          behavior.setBorderEventActivityId(nestedErrorEventActivity.getId());
        }
      }
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseBoundaryErrorEventDefinition(errorEventDefinition, interrupting, activity, nestedErrorEventActivity);
    }
  }

  protected List<ActivityImpl> getAllChildActivitiesOfType(String type, ScopeImpl scope) {
    List<ActivityImpl> children = new ArrayList<ActivityImpl>();
    for (ActivityImpl childActivity : scope.getActivities()) {
      if (type.equals(childActivity.getProperty("type"))) {
        children.add(childActivity);
      }
      children.addAll(getAllChildActivitiesOfType(type, childActivity));
    }
    return children;
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
   * Parses a subprocess (formely known as an embedded subprocess): a subprocess
   * defined withing another process definition.
   * 
   * @param subProcessElement
   *          The XML element corresponding with the subprocess definition
   * @param scope
   *          The current scope on which the subprocess is defined.
   */
  public ActivityImpl parseSubProcess(Element subProcessElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(subProcessElement, scope);
    
    String async = subProcessElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async");
    activity.setAsync("true".equals(async));
    
    activity.setScope(true);
    activity.setActivityBehavior(new SubProcessActivityBehavior());
    parseScope(subProcessElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseSubProcess(subProcessElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses a call activity (currenly only supporting calling subprocesses).
   * 
   * @param callActivityElement
   *          The XML element defining the call activity
   * @param scope
   *          The current scope on which the call activity is defined.
   */
  public ActivityImpl parseCallActivity(Element callActivityElement, ScopeImpl scope) {
    ActivityImpl activity = createActivityOnScope(callActivityElement, scope);
    
    String async = callActivityElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "async");
    activity.setAsync("true".equals(async));
    
    String calledElement = callActivityElement.attribute("calledElement");
    if (calledElement == null) {
      addError("Missing attribute 'calledElement'", callActivityElement);
    }

    CallActivityBehavior callActivityBehaviour = new CallActivityBehavior(calledElement);

    Element extentionsElement = callActivityElement.element("extensionElements");
    if (extentionsElement != null) {
      // input data elements
      for (Element listenerElement : extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "in")) {
        String sourceExpression = listenerElement.attribute("sourceExpression");
        String target = listenerElement.attribute("target");
        if (sourceExpression != null) {
          Expression expression = expressionManager.createExpression(sourceExpression.trim());
          callActivityBehaviour.addDataInputAssociation(new SimpleDataInputAssociation(expression, target));
        } else {
          String source = listenerElement.attribute("source");
          callActivityBehaviour.addDataInputAssociation(new SimpleDataInputAssociation(source, target));
        }
      }
      // output data elements
      for (Element listenerElement : extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "out")) {
        String sourceExpression = listenerElement.attribute("sourceExpression");
        String target = listenerElement.attribute("target");
        if (sourceExpression != null) {
          Expression expression = expressionManager.createExpression(sourceExpression.trim());
          callActivityBehaviour.addDataOutputAssociation(new MessageImplicitDataOutputAssociation(target, expression));
        } else {
          String source = listenerElement.attribute("source");
          callActivityBehaviour.addDataOutputAssociation(new MessageImplicitDataOutputAssociation(target, source));
        }
      }
    }

    // // parse data input and output
    // for (Element dataAssociationElement :
    // callActivityElement.elements("dataInputAssociation")) {
    // AbstractDataAssociation dataAssociation =
    // this.parseDataInputAssociation(dataAssociationElement);
    // callActivityBehaviour.addDataInputAssociation(dataAssociation);
    // }
    //
    // for (Element dataAssociationElement :
    // callActivityElement.elements("dataOutputAssociation")) {
    // AbstractDataAssociation dataAssociation =
    // this.parseDataOutputAssociation(dataAssociationElement);
    // callActivityBehaviour.addDataOutputAssociation(dataAssociation);
    // }

    activity.setScope(true);
    activity.setActivityBehavior(callActivityBehaviour);

    parseExecutionListenersOnScope(callActivityElement, activity);

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseCallActivity(callActivityElement, scope, activity);
    }
    return activity;
  }

  /**
   * Parses the properties of an element (if any) that can contain properties
   * (processes, activities, etc.)
   * 
   * Returns true if property subelemens are found.
   * 
   * @param element
   *          The element that can contain properties.
   * @param activity
   *          The activity where the property declaration is done.
   */
  public void parseProperties(Element element, ActivityImpl activity) {
    List<Element> propertyElements = element.elements("property");
    for (Element propertyElement : propertyElements) {
      parseProperty(propertyElement, activity);
    }
  }

  /**
   * Parses one property definition.
   * 
   * @param propertyElement
   *          The 'property' element that defines how a property looks like and
   *          is handled.
   */
  public void parseProperty(Element propertyElement, ActivityImpl activity) {
    String id = propertyElement.attribute("id");
    String name = propertyElement.attribute("name");

    // If name isn't given, use the id as name
    if (name == null) {
      if (id == null) {
        addError("Invalid property usage on line " + propertyElement.getLine() + ": no id or name specified.", propertyElement);
      } else {
        name = id;
      }
    }

    String itemSubjectRef = propertyElement.attribute("itemSubjectRef");
    String type = null;
    if (itemSubjectRef != null) {
      ItemDefinition itemDefinition = itemDefinitions.get(itemSubjectRef);
      if (itemDefinition != null) {
        StructureDefinition structure = itemDefinition.getStructureDefinition();
        type = structure.getId();
      } else {
        addError("Invalid itemDefinition reference: " + itemSubjectRef + " not found", propertyElement);
      }
    }

    parsePropertyCustomExtensions(activity, propertyElement, name, type);
  }

  /**
   * Parses the custom extensions for properties.
   * 
   * @param activity
   *          The activity where the property declaration is done.
   * @param propertyElement
   *          The 'property' element defining the property.
   * @param propertyName
   *          The name of the property.
   * @param propertyType
   *          The type of the property.
   */
  public void parsePropertyCustomExtensions(ActivityImpl activity, Element propertyElement, String propertyName, String propertyType) {

    if (propertyType == null) {
      String type = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "type");
      propertyType = type != null ? type : "string"; // default is string
    }

    VariableDeclaration variableDeclaration = new VariableDeclaration(propertyName, propertyType);
    addVariableDeclaration(activity, variableDeclaration);
    activity.setScope(true);

    String src = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "src");
    if (src != null) {
      variableDeclaration.setSourceVariableName(src);
    }

    String srcExpr = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "srcExpr");
    if (srcExpr != null) {
      Expression sourceExpression = expressionManager.createExpression(srcExpr);
      variableDeclaration.setSourceExpression(sourceExpression);
    }

    String dst = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "dst");
    if (dst != null) {
      variableDeclaration.setDestinationVariableName(dst);
    }

    String destExpr = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "dstExpr");
    if (destExpr != null) {
      Expression destinationExpression = expressionManager.createExpression(destExpr);
      variableDeclaration.setDestinationExpression(destinationExpression);
    }

    String link = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "link");
    if (link != null) {
      variableDeclaration.setLink(link);
    }

    String linkExpr = propertyElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "linkExpr");
    if (linkExpr != null) {
      Expression linkExpression = expressionManager.createExpression(linkExpr);
      variableDeclaration.setLinkExpression(linkExpression);
    }

    for (BpmnParseListener parseListener : parseListeners) {
      parseListener.parseProperty(propertyElement, variableDeclaration, activity);
    }
  }

  /**
   * Parses all sequence flow of a scope.
   * 
   * @param processElement
   *          The 'process' element wherein the sequence flow are defined.
   * @param scope
   *          The scope to which the sequence flow must be added.
   */
  public void parseSequenceFlow(Element processElement, ScopeImpl scope) {
    for (Element sequenceFlowElement : processElement.elements("sequenceFlow")) {

      String id = sequenceFlowElement.attribute("id");
      String sourceRef = sequenceFlowElement.attribute("sourceRef");
      String destinationRef = sequenceFlowElement.attribute("targetRef");

      // Implicit check: sequence flow cannot cross (sub) process boundaries: we
      // don't do a processDefinition.findActivity here
      ActivityImpl sourceActivity = scope.findActivity(sourceRef);
      ActivityImpl destinationActivity = scope.findActivity(destinationRef);

      if (sourceActivity != null && destinationActivity != null) {

        TransitionImpl transition = sourceActivity.createOutgoingTransition(id);
        sequenceFlows.put(id, transition);
        transition.setProperty("name", sequenceFlowElement.attribute("name"));
        transition.setProperty("documentation", parseDocumentation(sequenceFlowElement));
        transition.setDestination(destinationActivity);
        parseSequenceFlowConditionExpression(sequenceFlowElement, transition);
        parseExecutionListenersOnTransition(sequenceFlowElement, transition);

        for (BpmnParseListener parseListener : parseListeners) {
          parseListener.parseSequenceFlow(sequenceFlowElement, scope, transition);
        }

      } else if (sourceActivity == null) {
        addError("Invalid source '" + sourceRef + "' of sequence flow '" + id + "'", sequenceFlowElement);
      } else if (destinationActivity == null) {
        addError("Invalid destination '" + destinationRef + "' of sequence flow '" + id + "'", sequenceFlowElement);
      }

    }
  }

  /**
   * Parses a condition expression on a sequence flow.
   * 
   * @param seqFlowElement
   *          The 'sequenceFlow' element that can contain a condition.
   * @param seqFlow
   *          The sequenceFlow object representation to which the condition must
   *          be added.
   */
  public void parseSequenceFlowConditionExpression(Element seqFlowElement, TransitionImpl seqFlow) {
    Element conditionExprElement = seqFlowElement.element("conditionExpression");
    if (conditionExprElement != null) {
      String expression = conditionExprElement.getText().trim();
      String type = conditionExprElement.attributeNS(BpmnParser.XSI_NS, "type");
      if (type != null && !type.equals("tFormalExpression")) {
        addError("Invalid type, only tFormalExpression is currently supported", conditionExprElement);
      }

      Condition expressionCondition = new UelExpressionCondition(expressionManager.createExpression(expression));
      seqFlow.setProperty(PROPERTYNAME_CONDITION_TEXT, expression);
      seqFlow.setProperty(PROPERTYNAME_CONDITION, expressionCondition);
    }
  }

  /**
   * Parses all execution-listeners on a scope.
   * 
   * @param scopeElement
   *          the XML element containing the scope definition.
   * @param scope
   *          the scope to add the executionListeners to.
   */
  public void parseExecutionListenersOnScope(Element scopeElement, ScopeImpl scope) {
    Element extentionsElement = scopeElement.element("extensionElements");
    if (extentionsElement != null) {
      List<Element> listenerElements = extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "executionListener");
      for (Element listenerElement : listenerElements) {
        String eventName = listenerElement.attribute("event");
        if (isValidEventNameForScope(eventName, listenerElement)) {
          ExecutionListener listener = parseExecutionListener(listenerElement);
          if (listener != null) {
            scope.addExecutionListener(eventName, listener);
          }
        }
      }
    }
  }

  /**
   * Check if the given event name is valid. If not, an appropriate error is
   * added.
   */
  protected boolean isValidEventNameForScope(String eventName, Element listenerElement) {
    if (eventName != null && eventName.trim().length() > 0) {
      if ("start".equals(eventName) || "end".equals(eventName)) {
        return true;
      } else {
        addError("Attribute 'eventName' must be one of {start|end}", listenerElement);
      }
    } else {
      addError("Attribute 'eventName' is mandatory on listener", listenerElement);
    }
    return false;
  }

  public void parseExecutionListenersOnTransition(Element activitiElement, TransitionImpl activity) {
    Element extentionsElement = activitiElement.element("extensionElements");
    if (extentionsElement != null) {
      List<Element> listenerElements = extentionsElement.elementsNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "executionListener");
      for (Element listenerElement : listenerElements) {
        ExecutionListener listener = parseExecutionListener(listenerElement);
        if (listener != null) {
          // Since a transition only fires event 'take', we don't parse the
          // eventName, it is ignored
          activity.addExecutionListener(listener);
        }
      }
    }
  }

  /**
   * Parses an {@link ExecutionListener} implementation for the given
   * executionListener element.
   * 
   * @param executionListenerElement
   *          the XML element containing the executionListener definition.
   */
  public ExecutionListener parseExecutionListener(Element executionListenerElement) {
    ExecutionListener executionListener = null;

    String className = executionListenerElement.attribute("class");
    String expression = executionListenerElement.attribute("expression");
    String delegateExpression = executionListenerElement.attribute("delegateExpression");

    if (className != null) {
      executionListener = new ClassDelegate(className, parseFieldDeclarations(executionListenerElement));
    } else if (expression != null) {
      executionListener = new ExpressionExecutionListener(expressionManager.createExpression(expression));
    } else if (delegateExpression != null) {
      executionListener = new DelegateExpressionExecutionListener(expressionManager.createExpression(delegateExpression));
    } else {
      addError("Element 'class' or 'expression' is mandatory on executionListener", executionListenerElement);
    }
    return executionListener;
  }

  /**
   * Retrieves the {@link Operation} corresponding with the given operation
   * identifier.
   */
  public Operation getOperation(String operationId) {
    return operations.get(operationId);
  }

  // Diagram interchange
  // /////////////////////////////////////////////////////////////////

  public void parseDiagramInterchangeElements() {
    // Multiple BPMNDiagram possible
    List<Element> diagrams = rootElement.elementsNS(BpmnParser.BPMN_DI_NS, "BPMNDiagram");
    if (!diagrams.isEmpty()) {
      for (Element diagramElement : diagrams) {
        parseBPMNDiagram(diagramElement);
      }
    }
  }

  public void parseBPMNDiagram(Element bpmndiagramElement) {
    // Each BPMNdiagram needs to have exactly one BPMNPlane
    Element bpmnPlane = bpmndiagramElement.elementNS(BpmnParser.BPMN_DI_NS, "BPMNPlane");
    if (bpmnPlane != null) {
      parseBPMNPlane(bpmnPlane);
    }
  }

  public void parseBPMNPlane(Element bpmnPlaneElement) {
    String processId = bpmnPlaneElement.attribute("bpmnElement");
    if (processId != null && !"".equals(processId)) {
      ProcessDefinitionEntity processDefinition = getProcessDefinition(processId);
      if (processDefinition != null) {
        processDefinition.setGraphicalNotationDefined(true);

        List<Element> shapes = bpmnPlaneElement.elementsNS(BpmnParser.BPMN_DI_NS, "BPMNShape");
        for (Element shape : shapes) {
          parseBPMNShape(shape, processDefinition);
        }

        List<Element> edges = bpmnPlaneElement.elementsNS(BpmnParser.BPMN_DI_NS, "BPMNEdge");
        for (Element edge : edges) {
          parseBPMNEdge(edge, processDefinition);
        }

      } else {
        addError("Invalid reference in 'bpmnElement' attribute, process " + processId + " not found", bpmnPlaneElement);
      }
    } else {
      addError("'bpmnElement' attribute is required on BPMNPlane ", bpmnPlaneElement);
    }
  }

  public void parseBPMNShape(Element bpmnShapeElement, ProcessDefinitionEntity processDefinition) {
    String activityId = bpmnShapeElement.attribute("bpmnElement");
    if (activityId != null && !"".equals(activityId)) {
      ActivityImpl activity = processDefinition.findActivity(activityId);

      // bounds
      if (activity != null) {
        Element bounds = bpmnShapeElement.elementNS(BpmnParser.BPMN_DC_NS, "Bounds");
        if (bounds != null) {
          activity.setX(parseDoubleAttribute(bpmnShapeElement, "x", bounds.attribute("x"), true).intValue());
          activity.setY(parseDoubleAttribute(bpmnShapeElement, "y", bounds.attribute("y"), true).intValue());
          activity.setWidth(parseDoubleAttribute(bpmnShapeElement, "width", bounds.attribute("width"), true).intValue());
          activity.setHeight(parseDoubleAttribute(bpmnShapeElement, "height", bounds.attribute("height"), true).intValue());
        } else {
          addError("'Bounds' element is required", bpmnShapeElement);
        }

        // collapsed or expanded
        String isExpanded = bpmnShapeElement.attribute("isExpanded");
        if (isExpanded != null) {
          activity.setProperty(PROPERTYNAME_ISEXPANDED, parseBooleanAttribute(isExpanded));
        }
      } else {
        addError("Invalid reference in 'bpmnElement' attribute, activity " + activityId + "not found", bpmnShapeElement);
      }
    } else {
      addError("'bpmnElement' attribute is required on BPMNShape", bpmnShapeElement);
    }
  }

  public void parseBPMNEdge(Element bpmnEdgeElement, ProcessDefinitionEntity processDefinition) {
    String sequenceFlowId = bpmnEdgeElement.attribute("bpmnElement");
    if (sequenceFlowId != null && !"".equals(sequenceFlowId)) {
      TransitionImpl sequenceFlow = sequenceFlows.get(sequenceFlowId);
      if (sequenceFlow != null) {
        List<Element> waypointElements = bpmnEdgeElement.elementsNS(BpmnParser.OMG_DI_NS, "waypoint");
        if (waypointElements.size() >= 2) {
          List<Integer> waypoints = new ArrayList<Integer>();
          for (Element waypointElement : waypointElements) {
            waypoints.add(parseDoubleAttribute(waypointElement, "x", waypointElement.attribute("x"), true).intValue());
            waypoints.add(parseDoubleAttribute(waypointElement, "y", waypointElement.attribute("y"), true).intValue());
          }
          sequenceFlow.setWaypoints(waypoints);
        } else {
          addError("Minimum 2 waypoint elements must be definted for a 'BPMNEdge'", bpmnEdgeElement);
        }
      } else {
        addError("Invalid reference in 'bpmnElement' attribute, sequenceFlow " + sequenceFlowId + "not found", bpmnEdgeElement);
      }
    } else {
      addError("'bpmnElement' attribute is required on BPMNEdge", bpmnEdgeElement);
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

  @Override
  public BpmnParse name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public BpmnParse sourceInputStream(InputStream inputStream) {
    super.sourceInputStream(inputStream);
    return this;
  }

  @Override
  public BpmnParse sourceResource(String resource, ClassLoader classLoader) {
    super.sourceResource(resource, classLoader);
    return this;
  }

  @Override
  public BpmnParse sourceResource(String resource) {
    super.sourceResource(resource);
    return this;
  }

  @Override
  public BpmnParse sourceString(String string) {
    super.sourceString(string);
    return this;
  }

  @Override
  public BpmnParse sourceUrl(String url) {
    super.sourceUrl(url);
    return this;
  }

  @Override
  public BpmnParse sourceUrl(URL url) {
    super.sourceUrl(url);
    return this;
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

  public Boolean parseBooleanAttribute(String booleanText, boolean defaultValue) {
    if (booleanText == null) {
      return defaultValue;
    } else {
      return parseBooleanAttribute(booleanText);
    }
  }

  public Boolean parseBooleanAttribute(String booleanText) {
    if ("true".equals(booleanText) || "enabled".equals(booleanText) || "on".equals(booleanText) || "active".equals(booleanText) || "yes".equals(booleanText)) {
      return Boolean.TRUE;
    }
    if ("false".equals(booleanText) || "disabled".equals(booleanText) || "off".equals(booleanText) || "inactive".equals(booleanText)
            || "no".equals(booleanText)) {
      return Boolean.FALSE;
    }
    return null;
  }

  public Double parseDoubleAttribute(Element element, String attributename, String doubleText, boolean required) {
    if (required && (doubleText == null || "".equals(doubleText))) {
      addError(attributename + " is required", element);
    } else {
      try {
        return Double.parseDouble(doubleText);
      } catch (NumberFormatException e) {
        addError("Cannot parse " + attributename + ": " + e.getMessage(), element);
      }
    }
    return -1.0;
  }

}
