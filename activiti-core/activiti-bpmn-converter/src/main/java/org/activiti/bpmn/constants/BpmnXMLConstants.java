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
package org.activiti.bpmn.constants;

public interface BpmnXMLConstants {

  public static final String BPMN2_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  public static final String BPMN2_PREFIX = "bpmn2";
  public static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
  public static final String XSI_PREFIX = "xsi";
  public static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
  public static final String XSD_PREFIX = "xsd";
  public static final String TYPE_LANGUAGE_ATTRIBUTE = "typeLanguage";
  public static final String XPATH_NAMESPACE = "http://www.w3.org/1999/XPath";
  public static final String EXPRESSION_LANGUAGE_ATTRIBUTE = "expressionLanguage";
  public static final String PROCESS_NAMESPACE = "http://www.activiti.org/test";
  public static final String TARGET_NAMESPACE_ATTRIBUTE = "targetNamespace";
  public static final String ACTIVITI_EXTENSIONS_NAMESPACE = "http://activiti.org/bpmn";
  public static final String ACTIVITI_EXTENSIONS_PREFIX = "activiti";
  public static final String BPMNDI_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/DI";
  public static final String BPMNDI_PREFIX = "bpmndi";
  public static final String OMGDC_NAMESPACE = "http://www.omg.org/spec/DD/20100524/DC";
  public static final String OMGDC_PREFIX = "omgdc";
  public static final String OMGDI_NAMESPACE = "http://www.omg.org/spec/DD/20100524/DI";
  public static final String OMGDI_PREFIX = "omgdi";

  public static final String ATTRIBUTE_ID = "id";
  public static final String ATTRIBUTE_NAME = "name";
  public static final String ATTRIBUTE_TYPE = "type";
  public static final String ATTRIBUTE_DEFAULT = "default";
  public static final String ATTRIBUTE_ITEM_REF = "itemRef";
  public static final String ELEMENT_DEFINITIONS = "definitions";
  public static final String ELEMENT_DOCUMENTATION = "documentation";

  public static final String ELEMENT_SIGNAL = "signal";
  public static final String ELEMENT_MESSAGE = "message";
  public static final String ELEMENT_ERROR = "error";
  public static final String ELEMENT_COLLABORATION = "collaboration";
  public static final String ELEMENT_PARTICIPANT = "participant";
  public static final String ELEMENT_MESSAGE_FLOW = "messageFlow";
  public static final String ELEMENT_LANESET = "laneSet";
  public static final String ELEMENT_LANE = "lane";
  public static final String ELEMENT_FLOWNODE_REF = "flowNodeRef";
  public static final String ATTRIBUTE_PROCESS_REF = "processRef";
  public static final String ELEMENT_RESOURCE = "resource";

  public static final String ELEMENT_PROCESS = "process";
  public static final String ATTRIBUTE_PROCESS_EXECUTABLE = "isExecutable";
  public static final String ELEMENT_POTENTIAL_STARTER = "potentialStarter";
  public static final String ATTRIBUTE_PROCESS_CANDIDATE_USERS = "candidateStarterUsers";
  public static final String ATTRIBUTE_PROCESS_CANDIDATE_GROUPS = "candidateStarterGroups";
  public static final String ELEMENT_SUBPROCESS = "subProcess";
  public static final String ATTRIBUTE_TRIGGERED_BY = "triggeredByEvent";
  public static final String ELEMENT_TRANSACTION = "transaction";
  public static final String ELEMENT_ADHOC_SUBPROCESS = "adHocSubProcess";
  public static final String ATTRIBUTE_ORDERING = "ordering";
  public static final String ATTRIBUTE_CANCEL_REMAINING_INSTANCES = "cancelRemainingInstances";
  public static final String ELEMENT_COMPLETION_CONDITION = "completionCondition";
  public static final String ATTRIBUTE_MESSAGE_EXPRESSION = "messageExpression";
  public static final String ATTRIBUTE_MESSAGE_CORRELATION_KEY = "correlationKey";
  public static final String ATTRIBUTE_SIGNAL_EXPRESSION = "signalExpression";

  public static final String ELEMENT_DATA_STATE = "dataState";

  public static final String ELEMENT_EXTENSIONS = "extensionElements";

  public static final String ELEMENT_EXECUTION_LISTENER = "executionListener";
  public static final String ELEMENT_EVENT_LISTENER = "eventListener";
  public static final String ELEMENT_TASK_LISTENER = "taskListener";
  public static final String ATTRIBUTE_LISTENER_EVENT = "event";
  public static final String ATTRIBUTE_LISTENER_EVENTS = "events";
  public static final String ATTRIBUTE_LISTENER_ENTITY_TYPE = "entityType";
  public static final String ATTRIBUTE_LISTENER_CLASS = "class";
  public static final String ATTRIBUTE_LISTENER_EXPRESSION = "expression";
  public static final String ATTRIBUTE_LISTENER_DELEGATEEXPRESSION = "delegateExpression";
  public static final String ATTRIBUTE_LISTENER_THROW_EVENT_TYPE = "throwEvent";
  public static final String ATTRIBUTE_LISTENER_THROW_SIGNAL_EVENT_NAME = "signalName";
  public static final String ATTRIBUTE_LISTENER_THROW_MESSAGE_EVENT_NAME = "messageName";
  public static final String ATTRIBUTE_LISTENER_THROW_ERROR_EVENT_CODE = "errorCode";
  public static final String ATTRIBUTE_LISTENER_ON_TRANSACTION = "onTransaction";
  public static final String ATTRIBUTE_LISTENER_CUSTOM_PROPERTIES_RESOLVER_CLASS = "customPropertiesResolverClass";
  public static final String ATTRIBUTE_LISTENER_CUSTOM_PROPERTIES_RESOLVER_EXPRESSION = "customPropertiesResolverExpression";
  public static final String ATTRIBUTE_LISTENER_CUSTOM_PROPERTIES_RESOLVER_DELEGATEEXPRESSION = "customPropertiesResolverDelegateExpression";

  public static final String ATTRIBUTE_LISTENER_THROW_EVENT_TYPE_SIGNAL = "signal";
  public static final String ATTRIBUTE_LISTENER_THROW_EVENT_TYPE_GLOBAL_SIGNAL = "globalSignal";
  public static final String ATTRIBUTE_LISTENER_THROW_EVENT_TYPE_MESSAGE = "message";
  public static final String ATTRIBUTE_LISTENER_THROW_EVENT_TYPE_ERROR = "error";

  public static final String ATTRIBUTE_VALUE_TRUE = "true";
  public static final String ATTRIBUTE_VALUE_FALSE = "false";

  public static final String ATTRIBUTE_ACTIVITY_ASYNCHRONOUS = "async";
  public static final String ATTRIBUTE_ACTIVITY_EXCLUSIVE = "exclusive";
  public static final String ATTRIBUTE_ACTIVITY_ISFORCOMPENSATION = "isForCompensation";

  public static final String ELEMENT_IMPORT = "import";
  public static final String ATTRIBUTE_IMPORT_TYPE = "importType";
  public static final String ATTRIBUTE_LOCATION = "location";
  public static final String ATTRIBUTE_NAMESPACE = "namespace";

  public static final String ELEMENT_INTERFACE = "interface";
  public static final String ELEMENT_OPERATION = "operation";
  public static final String ATTRIBUTE_IMPLEMENTATION_REF = "implementationRef";
  public static final String ELEMENT_IN_MESSAGE = "inMessageRef";
  public static final String ELEMENT_OUT_MESSAGE = "outMessageRef";

  public static final String ELEMENT_ITEM_DEFINITION = "itemDefinition";
  public static final String ATTRIBUTE_STRUCTURE_REF = "structureRef";
  public static final String ATTRIBUTE_ITEM_KIND = "itemKind";

  public static final String ELEMENT_DATA_STORE = "dataStore";
  public static final String ELEMENT_DATA_STORE_REFERENCE = "dataStoreReference";
  public static final String ATTRIBUTE_ITEM_SUBJECT_REF = "itemSubjectRef";
  public static final String ATTRIBUTE_DATA_STORE_REF = "dataStoreRef";

  public static final String ELEMENT_IOSPECIFICATION = "ioSpecification";
  public static final String ELEMENT_DATA_INPUT = "dataInput";
  public static final String ELEMENT_DATA_OUTPUT = "dataOutput";
  public static final String ELEMENT_DATA_INPUT_REFS = "dataInputRefs";
  public static final String ELEMENT_DATA_OUTPUT_REFS = "dataOutputRefs";

  public static final String ELEMENT_INPUT_ASSOCIATION = "dataInputAssociation";
  public static final String ELEMENT_OUTPUT_ASSOCIATION = "dataOutputAssociation";
  public static final String ELEMENT_SOURCE_REF = "sourceRef";
  public static final String ELEMENT_TARGET_REF = "targetRef";
  public static final String ELEMENT_TRANSFORMATION = "transformation";
  public static final String ELEMENT_ASSIGNMENT = "assignment";
  public static final String ELEMENT_FROM = "from";
  public static final String ELEMENT_TO = "to";

  // fake element for mail task
  public static final String ELEMENT_TASK_MAIL = "mailTask";

  public static final String ELEMENT_TASK = "task";
  public static final String ELEMENT_TASK_BUSINESSRULE = "businessRuleTask";
  public static final String ELEMENT_TASK_MANUAL = "manualTask";
  public static final String ELEMENT_TASK_RECEIVE = "receiveTask";
  public static final String ELEMENT_TASK_SCRIPT = "scriptTask";
  public static final String ELEMENT_TASK_SEND = "sendTask";
  public static final String ELEMENT_TASK_SERVICE = "serviceTask";
  public static final String ELEMENT_TASK_USER = "userTask";
  public static final String ELEMENT_CALL_ACTIVITY = "callActivity";

  public static final String ATTRIBUTE_EVENT_START_INITIATOR = "initiator";
  public static final String ATTRIBUTE_EVENT_START_INTERRUPTING = "isInterrupting";
  public static final String ATTRIBUTE_FORM_FORMKEY = "formKey";

  public static final String ELEMENT_MULTIINSTANCE = "multiInstanceLoopCharacteristics";
  public static final String ELEMENT_MULTIINSTANCE_CARDINALITY = "loopCardinality";
  public static final String ELEMENT_MULTIINSTANCE_DATAINPUT = "loopDataInputRef";
  public static final String ELEMENT_MULTIINSTANCE_DATAITEM = "inputDataItem";
  public static final String ELEMENT_MULTI_INSTANCE_DATA_OUTPUT = "loopDataOutputRef";
  public static final String ELEMENT_MULTI_INSTANCE_OUTPUT_DATA_ITEM = "outputDataItem";
  public static final String ELEMENT_MULTIINSTANCE_CONDITION = "completionCondition";
  public static final String ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL = "isSequential";
  public static final String ATTRIBUTE_MULTIINSTANCE_COLLECTION = "collection";
  public static final String ATTRIBUTE_MULTIINSTANCE_VARIABLE = "elementVariable";
  public static final String ATTRIBUTE_MULTIINSTANCE_INDEX_VARIABLE = "elementIndexVariable";

  public static final String ATTRIBUTE_TASK_IMPLEMENTATION = "implementation";
  public static final String ATTRIBUTE_TASK_OPERATION_REF = "operationRef";

  public static final String ATTRIBUTE_TASK_SCRIPT_TEXT = "script";
  public static final String ATTRIBUTE_TASK_SCRIPT_FORMAT = "scriptFormat";
  public static final String ATTRIBUTE_TASK_SCRIPT_RESULTVARIABLE = "resultVariable";
  public static final String ATTRIBUTE_TASK_SCRIPT_AUTO_STORE_VARIABLE = "autoStoreVariables";

  public static final String ATTRIBUTE_TASK_SERVICE_CLASS = "class";
  public static final String ATTRIBUTE_TASK_SERVICE_EXPRESSION = "expression";
  public static final String ATTRIBUTE_TASK_SERVICE_DELEGATEEXPRESSION = "delegateExpression";
  public static final String ATTRIBUTE_TASK_SERVICE_RESULTVARIABLE = "resultVariableName";
  public static final String ATTRIBUTE_TASK_SERVICE_EXTENSIONID = "extensionId";
  public static final String ATTRIBUTE_TASK_SERVICE_SKIP_EXPRESSION = "skipExpression";

  public static final String ATTRIBUTE_TASK_USER_ASSIGNEE = "assignee";
  public static final String ATTRIBUTE_TASK_USER_OWNER = "owner";
  public static final String ATTRIBUTE_TASK_USER_CANDIDATEUSERS = "candidateUsers";
  public static final String ATTRIBUTE_TASK_USER_CANDIDATEGROUPS = "candidateGroups";
  public static final String ATTRIBUTE_TASK_USER_DUEDATE = "dueDate";
  public static final String ATTRIBUTE_TASK_USER_BUSINESS_CALENDAR_NAME = "businessCalendarName";
  public static final String ATTRIBUTE_TASK_USER_CATEGORY = "category";
  public static final String ATTRIBUTE_TASK_USER_PRIORITY = "priority";
  public static final String ATTRIBUTE_TASK_USER_SKIP_EXPRESSION = "skipExpression";

  public static final String ATTRIBUTE_TASK_RULE_VARIABLES_INPUT = "ruleVariablesInput";
  public static final String ATTRIBUTE_TASK_RULE_RESULT_VARIABLE = "resultVariable";
  public static final String ATTRIBUTE_TASK_RULE_RULES = "rules";
  public static final String ATTRIBUTE_TASK_RULE_EXCLUDE = "exclude";
  public static final String ATTRIBUTE_TASK_RULE_CLASS = "class";

  public static final String ATTRIBUTE_CALL_ACTIVITY_CALLEDELEMENT = "calledElement";
  public static final String ATTRIBUTE_CALL_ACTIVITY_BUSINESS_KEY = "businessKey";
  public static final String ATTRIBUTE_CALL_ACTIVITY_INHERIT_BUSINESS_KEY = "inheritBusinessKey";
  public static final String ATTRIBUTE_CALL_ACTIVITY_INHERITVARIABLES = "inheritVariables";
  public static final String ELEMENT_CALL_ACTIVITY_IN_PARAMETERS = "in";
  public static final String ELEMENT_CALL_ACTIVITY_OUT_PARAMETERS = "out";
  public static final String ATTRIBUTE_IOPARAMETER_SOURCE = "source";
  public static final String ATTRIBUTE_IOPARAMETER_SOURCE_EXPRESSION = "sourceExpression";
  public static final String ATTRIBUTE_IOPARAMETER_TARGET = "target";

  public static final String ELEMENT_SEQUENCE_FLOW = "sequenceFlow";
  public static final String ELEMENT_FLOW_CONDITION = "conditionExpression";
  public static final String ATTRIBUTE_FLOW_SOURCE_REF = "sourceRef";
  public static final String ATTRIBUTE_FLOW_TARGET_REF = "targetRef";
  public static final String ATTRIBUTE_FLOW_SKIP_EXPRESSION = "skipExpression";

  public static final String ELEMENT_TEXT_ANNOTATION = "textAnnotation";
  public static final String ATTRIBUTE_TEXTFORMAT = "textFormat";
  public static final String ELEMENT_TEXT_ANNOTATION_TEXT = "text";

  public static final String ELEMENT_ASSOCIATION = "association";

  public static final String ELEMENT_GATEWAY_EXCLUSIVE = "exclusiveGateway";
  public static final String ELEMENT_GATEWAY_EVENT = "eventBasedGateway";
  public static final String ELEMENT_GATEWAY_INCLUSIVE = "inclusiveGateway";
  public static final String ELEMENT_GATEWAY_PARALLEL = "parallelGateway";
  public static final String ELEMENT_GATEWAY_COMPLEX = "complexGateway";

  public static final String ELEMENT_EVENT_START = "startEvent";
  public static final String ELEMENT_EVENT_END = "endEvent";
  public static final String ELEMENT_EVENT_BOUNDARY = "boundaryEvent";
  public static final String ELEMENT_EVENT_THROW = "intermediateThrowEvent";
  public static final String ELEMENT_EVENT_CATCH = "intermediateCatchEvent";

  public static final String ATTRIBUTE_BOUNDARY_ATTACHEDTOREF = "attachedToRef";
  public static final String ATTRIBUTE_BOUNDARY_CANCELACTIVITY = "cancelActivity";

  public static final String ELEMENT_EVENT_ERRORDEFINITION = "errorEventDefinition";
  public static final String ATTRIBUTE_ERROR_REF = "errorRef";
  public static final String ATTRIBUTE_ERROR_CODE = "errorCode";
  public static final String ELEMENT_EVENT_MESSAGEDEFINITION = "messageEventDefinition";
  public static final String ATTRIBUTE_MESSAGE_REF = "messageRef";
  public static final String ELEMENT_EVENT_SIGNALDEFINITION = "signalEventDefinition";
  public static final String ATTRIBUTE_SIGNAL_REF = "signalRef";
  public static final String ATTRIBUTE_SCOPE = "scope";
  public static final String ELEMENT_EVENT_TIMERDEFINITION = "timerEventDefinition";
  public static final String ATTRIBUTE_CALENDAR_NAME = "businessCalendarName";
  public static final String ATTRIBUTE_TIMER_DATE = "timeDate";
  public static final String ATTRIBUTE_TIMER_CYCLE = "timeCycle";
  public static final String ATTRIBUTE_END_DATE = "endDate";
  public static final String ATTRIBUTE_TIMER_DURATION = "timeDuration";
  public static final String ELEMENT_EVENT_TERMINATEDEFINITION = "terminateEventDefinition";
  public static final String ATTRIBUTE_TERMINATE_ALL = "terminateAll";
  public static final String ATTRIBUTE_TERMINATE_MULTI_INSTANCE = "terminateMultiInstance";
  public static final String ELEMENT_EVENT_CANCELDEFINITION = "cancelEventDefinition";
  public static final String ELEMENT_EVENT_COMPENSATEDEFINITION = "compensateEventDefinition";
  public static final String ATTRIBUTE_COMPENSATE_ACTIVITYREF = "activityRef";
  public static final String ATTRIBUTE_COMPENSATE_WAITFORCOMPLETION = "waitForCompletion";

  public static final String ELEMENT_FORMPROPERTY = "formProperty";
  public static final String ATTRIBUTE_FORM_ID = "id";
  public static final String ATTRIBUTE_FORM_NAME = "name";
  public static final String ATTRIBUTE_FORM_TYPE = "type";
  public static final String ATTRIBUTE_FORM_EXPRESSION = "expression";
  public static final String ATTRIBUTE_FORM_VARIABLE = "variable";
  public static final String ATTRIBUTE_FORM_READABLE = "readable";
  public static final String ATTRIBUTE_FORM_WRITABLE = "writable";
  public static final String ATTRIBUTE_FORM_REQUIRED = "required";
  public static final String ATTRIBUTE_FORM_DEFAULT = "default";
  public static final String ATTRIBUTE_FORM_DATEPATTERN = "datePattern";
  public static final String ELEMENT_VALUE = "value";

  public static final String ELEMENT_FIELD = "field";
  public static final String ATTRIBUTE_FIELD_NAME = "name";
  public static final String ATTRIBUTE_FIELD_STRING = "stringValue";
  public static final String ATTRIBUTE_FIELD_EXPRESSION = "expression";
  public static final String ELEMENT_FIELD_STRING = "string";

  public static final String ALFRESCO_TYPE = "alfrescoScriptType";

  public static final String ELEMENT_DI_DIAGRAM = "BPMNDiagram";
  public static final String ELEMENT_DI_PLANE = "BPMNPlane";
  public static final String ELEMENT_DI_SHAPE = "BPMNShape";
  public static final String ELEMENT_DI_EDGE = "BPMNEdge";
  public static final String ELEMENT_DI_LABEL = "BPMNLabel";
  public static final String ELEMENT_DI_BOUNDS = "Bounds";
  public static final String ELEMENT_DI_WAYPOINT = "waypoint";
  public static final String ATTRIBUTE_DI_BPMNELEMENT = "bpmnElement";
  public static final String ATTRIBUTE_DI_IS_EXPANDED = "isExpanded";
  public static final String ATTRIBUTE_DI_WIDTH = "width";
  public static final String ATTRIBUTE_DI_HEIGHT = "height";
  public static final String ATTRIBUTE_DI_X = "x";
  public static final String ATTRIBUTE_DI_Y = "y";

  public static final String ELEMENT_DATA_OBJECT = "dataObject";
  public static final String ATTRIBUTE_DATA_ID = "id";
  public static final String ATTRIBUTE_DATA_NAME = "name";
  public static final String ATTRIBUTE_DATA_ITEM_REF = "itemSubjectRef";
  // only used by valued data objects
  public static final String ELEMENT_DATA_VALUE = "value";

  public static final String ELEMENT_CUSTOM_RESOURCE = "customResource";
  public static final String ELEMENT_RESOURCE_ASSIGNMENT = "resourceAssignmentExpression";
  public static final String ELEMENT_FORMAL_EXPRESSION = "formalExpression";
  public static final String ELEMENT_RESOURCE_REF = "resourceRef";
  public static final String ATTRIBUTE_ASSOCIATION_DIRECTION = "associationDirection";

  public static final String FAILED_JOB_RETRY_TIME_CYCLE = "failedJobRetryTimeCycle";
  public static final String MAP_EXCEPTION = "mapException";
  public static final String MAP_EXCEPTION_ERRORCODE = "errorCode";
  public static final String MAP_EXCEPTION_ANDCHILDREN = "includeChildExceptions";

  public static final String ELEMENT_GATEWAY_INCOMING = "incoming";
  public static final String ELEMENT_GATEWAY_OUTGOING = "outgoing";

}
