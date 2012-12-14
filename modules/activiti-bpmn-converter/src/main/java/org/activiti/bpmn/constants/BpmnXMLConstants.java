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

/**
 * @author Tijs Rademakers
 */
public interface BpmnXMLConstants {
  
  public static final String BPMN2_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  public static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
  public static final String XSI_PREFIX = "xsi";
  public static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
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
  public static final String ELEMENT_DEFINITIONS = "definitions";
  public static final String ELEMENT_DOCUMENTATION = "documentation";
  
  public static final String ELEMENT_SIGNAL = "signal";
  public static final String ELEMENT_MESSAGE = "message";
  public static final String ELEMENT_COLLABORATION = "collaboration";
  public static final String ELEMENT_PARTICIPANT = "participant";
  public static final String ELEMENT_LANESET = "laneSet";
  public static final String ELEMENT_LANE = "lane";
  public static final String ATTRIBUTE_PROCESS_REF = "processRef";
  public static final String ATTRIBUTE_FLOWNODE_REF = "flowNodeRef";
  
  public static final String ELEMENT_PROCESS = "process";
  public static final String ATTRIBUTE_PROCESS_EXECUTABLE = "isExecutable";
  public static final String ELEMENT_SUBPROCESS = "subProcess";
  public static final String ATTRIBUTE_TRIGGERED_BY = "triggeredByEvent";
  
  public static final String ELEMENT_EXTENSIONS = "extensionElements";
  
  public static final String ELEMENT_EXECUTION_LISTENER = "executionListener";
  public static final String ELEMENT_TASK_LISTENER = "taskListener";
  public static final String ATTRIBUTE_LISTENER_EVENT = "event";
  public static final String ATTRIBUTE_LISTENER_CLASS = "class";
  public static final String ATTRIBUTE_LISTENER_EXPRESSION = "expression";
  public static final String ATTRIBUTE_LISTENER_DELEGATEEXPRESSION = "delegateExpression";
  
  public static final String ATTRIBUTE_VALUE_TRUE = "true";
  public static final String ATTRIBUTE_VALUE_FALSE = "false";
  
  public static final String ATTRIBUTE_ACTIVITY_ASYNCHRONOUS = "async";
  public static final String ATTRIBUTE_ACTIVITY_EXCLUSIVE = "exclusive";
  public static final String ATTRIBUTE_ACTIVITY_DEFAULT = "default";
  
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
  public static final String ATTRIBUTE_FORM_FORMKEY = "formKey";
  
  public static final String ELEMENT_MULTIINSTANCE = "multiInstanceLoopCharacteristics";
  public static final String ELEMENT_MULTIINSTANCE_CARDINALITY = "loopCardinality";
  public static final String ELEMENT_MULTIINSTANCE_DATAINPUT = "loopDataInputRef";
  public static final String ELEMENT_MULTIINSTANCE_DATAITEM = "inputDataItem";
  public static final String ELEMENT_MULTIINSTANCE_CONDITION = "completionCondition";
  public static final String ATTRIBUTE_MULTIINSTANCE_SEQUENTIAL = "isSequential";
  public static final String ATTRIBUTE_MULTIINSTANCE_COLLECTION = "collection";
  public static final String ATTRIBUTE_MULTIINSTANCE_VARIABLE = "elementVariable";
  
  public static final String ATTRIBUTE_TASK_SCRIPT_TEXT = "script";
  public static final String ATTRIBUTE_TASK_SCRIPT_FORMAT = "scriptFormat";
  
  public static final String ATTRIBUTE_TASK_SERVICE_CLASS = "class";
  public static final String ATTRIBUTE_TASK_SERVICE_EXPRESSION = "expression";
  public static final String ATTRIBUTE_TASK_SERVICE_DELEGATEEXPRESSION = "delegateExpression";
  public static final String ATTRIBUTE_TASK_SERVICE_RESULTVARIABLE = "resultVariableName";
  public static final String ATTRIBUTE_TASK_SERVICE_EXTENSIONID = "extensionId";
  
  public static final String ATTRIBUTE_TASK_USER_ASSIGNEE = "assignee";
  public static final String ATTRIBUTE_TASK_USER_CANDIDATEUSERS = "candidateUsers";
  public static final String ATTRIBUTE_TASK_USER_CANDIDATEGROUPS = "candidateGroups";
  public static final String ATTRIBUTE_TASK_USER_DUEDATE = "dueDate";
  public static final String ATTRIBUTE_TASK_USER_PRIORITY = "priority";
  
  public static final String ATTRIBUTE_TASK_RULE_VARIABLES_INPUT = "ruleVariablesInput";
  public static final String ATTRIBUTE_TASK_RULE_RESULT_VARIABLE = "resultVariable";
  public static final String ATTRIBUTE_TASK_RULE_RULES = "rules";
  public static final String ATTRIBUTE_TASK_RULE_EXCLUDE = "exclude";
  public static final String ATTRIBUTE_TASK_RULE_CLASS = "class";
  
  public static final String ATTRIBUTE_CALL_ACTIVITY_CALLEDELEMENT = "calledElement";
  public static final String ELEMENT_CALL_ACTIVITY_IN_PARAMETERS = "in";
  public static final String ELEMENT_CALL_ACTIVITY_OUT_PARAMETERS = "out";
  public static final String ATTRIBUTE_IOPARAMETER_SOURCE = "source";
  public static final String ATTRIBUTE_IOPARAMETER_SOURCE_EXPRESSION = "sourceExpression";
  public static final String ATTRIBUTE_IOPARAMETER_TARGET = "target";
  
  public static final String ELEMENT_SEQUENCE_FLOW = "sequenceFlow";
  public static final String ELEMENT_FLOW_CONDITION = "conditionExpression";
  public static final String ATTRIBUTE_FLOW_SOURCE_REF = "sourceRef";
  public static final String ATTRIBUTE_FLOW_TARGET_REF = "targetRef";
  
  public static final String ELEMENT_TEXT_ANNOTATION = "textAnnotation";
  public static final String ATTRIBUTE_TEXTFORMAT = "textFormat";
  public static final String ELEMENT_TEXT_ANNOTATION_TEXT = "text";
  
  public static final String ELEMENT_ASSOCIATION = "association";
  
  public static final String ELEMENT_GATEWAY_EXCLUSIVE = "exclusiveGateway";
  public static final String ELEMENT_GATEWAY_EVENT = "eventBasedGateway";
  public static final String ELEMENT_GATEWAY_INCLUSIVE = "inclusiveGateway";
  public static final String ELEMENT_GATEWAY_PARALLEL = "parallelGateway";
  
  public static final String ELEMENT_EVENT_START = "startEvent";
  public static final String ELEMENT_EVENT_END = "endEvent";
  public static final String ELEMENT_EVENT_BOUNDARY = "boundaryEvent";
  public static final String ELEMENT_EVENT_THROW = "intermediateThrowEvent";
  public static final String ELEMENT_EVENT_CATCH = "intermediateCatchEvent";
  
  public static final String ATTRIBUTE_BOUNDARY_ATTACHEDTOREF = "attachedToRef";
  public static final String ATTRIBUTE_BOUNDARY_CANCELACTIVITY = "cancelActivity";
  
  public static final String ELEMENT_EVENT_ERRORDEFINITION = "errorEventDefinition";
  public static final String ATTRIBUTE_ERROR_REF = "errorRef";
  public static final String ELEMENT_EVENT_MESSAGEDEFINITION = "messageEventDefinition";
  public static final String ATTRIBUTE_MESSAGE_REF = "messageRef";
  public static final String ELEMENT_EVENT_SIGNALDEFINITION = "signalEventDefinition";
  public static final String ATTRIBUTE_SIGNAL_REF = "signalRef";
  public static final String ELEMENT_EVENT_TIMERDEFINITION = "timerEventDefinition";
  public static final String ATTRIBUTE_TIMER_DATE = "timeDate";
  public static final String ATTRIBUTE_TIMER_CYCLE = "timeCycle";
  public static final String ATTRIBUTE_TIMER_DURATION = "timeDuration";
  
  public static final String ELEMENT_FORMPROPERTY = "formProperty";
  public static final String ATTRIBUTE_FORM_ID = "id";
  public static final String ATTRIBUTE_FORM_NAME = "name";
  public static final String ATTRIBUTE_FORM_TYPE = "type";
  public static final String ATTRIBUTE_FORM_EXPRESSION = "expression";
  public static final String ATTRIBUTE_FORM_VARIABLE = "variable";
  
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
  public static final String ATTRIBUTE_DI_WIDTH = "width";
  public static final String ATTRIBUTE_DI_HEIGHT = "height";
  public static final String ATTRIBUTE_DI_X = "x";
  public static final String ATTRIBUTE_DI_Y = "y";
}
