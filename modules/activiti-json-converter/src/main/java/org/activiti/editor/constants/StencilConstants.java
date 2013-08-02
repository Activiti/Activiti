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
package org.activiti.editor.constants;

/**
 * @author Tijs Rademakers
 */
public interface StencilConstants {

	// stencil items
	final String STENCIL_EVENT_START_NONE = "StartNoneEvent";
  final String STENCIL_EVENT_START_TIMER = "StartTimerEvent";
  final String STENCIL_EVENT_START_MESSAGE = "StartMessageEvent";
  final String STENCIL_EVENT_START_SIGNAL = "StartSignalEvent";
  final String STENCIL_EVENT_START_ERROR = "StartErrorEvent";
  
  final String STENCIL_EVENT_END_NONE = "EndNoneEvent";
  final String STENCIL_EVENT_END_ERROR = "EndErrorEvent";
  
  final String STENCIL_SUB_PROCESS = "SubProcess";
  final String STENCIL_EVENT_SUB_PROCESS = "EventSubProcess";
  final String STENCIL_CALL_ACTIVITY = "CallActivity";
  
  final String STENCIL_POOL = "Pool";
  final String STENCIL_LANE = "Lane";
  
  final String STENCIL_TASK_BUSINESS_RULE = "BusinessRule";
  final String STENCIL_TASK_MAIL = "MailTask";
  final String STENCIL_TASK_MANUAL = "ManualTask";
  final String STENCIL_TASK_RECEIVE = "ReceiveTask";
  final String STENCIL_TASK_SCRIPT = "ScriptTask";
  final String STENCIL_TASK_SEND = "SendTask";
  final String STENCIL_TASK_SERVICE = "ServiceTask";
  final String STENCIL_TASK_USER = "UserTask";
  
  final String STENCIL_GATEWAY_EXCLUSIVE = "ExclusiveGateway";
  final String STENCIL_GATEWAY_PARALLEL = "ParallelGateway";
  final String STENCIL_GATEWAY_INCLUSIVE = "InclusiveGateway";
  final String STENCIL_GATEWAY_EVENT = "EventGateway";
  
  final String STENCIL_EVENT_BOUNDARY_TIMER = "BoundaryTimerEvent";
  final String STENCIL_EVENT_BOUNDARY_ERROR = "BoundaryErrorEvent";
  final String STENCIL_EVENT_BOUNDARY_SIGNAL = "BoundarySignalEvent";
  
  final String STENCIL_EVENT_CATCH_SIGNAL = "CatchSignalEvent";
  final String STENCIL_EVENT_CATCH_TIMER = "CatchTimerEvent";
  final String STENCIL_EVENT_CATCH_MESSAGE = "CatchMessageEvent";
  
  final String STENCIL_EVENT_THROW_SIGNAL = "ThrowSignalEvent";
  final String STENCIL_EVENT_THROW_NONE = "ThrowNoneEvent";
  
  final String STENCIL_SEQUENCE_FLOW = "SequenceFlow";
  
  final String STENCIL_TEXT_ANNOTATION = "TextAnnotation";
	
  final String PROPERTY_VALUE_YES = "Yes";
  final String PROPERTY_VALUE_NO = "No";
  
  //stencil properties
  final String PROPERTY_OVERRIDE_ID = "overrideid";
	final String PROPERTY_NAME = "name";
	final String PROPERTY_DOCUMENTATION = "documentation";
	
	final String PROPERTY_PROCESS_ID = "process_id";
	final String PROPERTY_PROCESS_EXECUTABLE = "process_executable";
	final String PROPERTY_PROCESS_VERSION = "process_version";
	final String PROPERTY_PROCESS_AUTHOR = "process_author";
	final String PROPERTY_PROCESS_NAMESPACE = "process_namespace";
	
	final String PROPERTY_CANCEL_ACTIVITY = "cancelactivity";
        
	final String PROPERTY_TIMER_DURATON = "timerdurationdefinition";
	final String PROPERTY_TIMER_DATE = "timerdatedefinition";
	final String PROPERTY_TIMER_CYCLE = "timercycledefinition";
	
	final String PROPERTY_MESSAGEREF = "messageref";
	
	final String PROPERTY_SIGNALREF = "signalref";
	
	final String PROPERTY_ERRORREF = "errorref";
	
	final String PROPERTY_NONE_STARTEVENT_INITIATOR = "initiator";
	
	final String PROPERTY_ASYNCHRONOUS = "asynchronousdefinition";
	final String PROPERTY_EXCLUSIVE = "exclusivedefinition";
	
	final String PROPERTY_MULTIINSTANCE_SEQUENTIAL = "multiinstance_sequential";
  final String PROPERTY_MULTIINSTANCE_CARDINALITY = "multiinstance_cardinality";
  final String PROPERTY_MULTIINSTANCE_COLLECTION = "multiinstance_collection";
  final String PROPERTY_MULTIINSTANCE_VARIABLE = "multiinstance_variable";
  final String PROPERTY_MULTIINSTANCE_CONDITION = "multiinstance_condition";
	
	final String PROPERTY_TASK_LISTENERS = "tasklisteners";
	final String PROPERTY_TASK_LISTENER_EVENT = "task_listener_event_type";
  final String PROPERTY_TASK_LISTENER_CLASS = "task_listener_class";
  final String PROPERTY_TASK_LISTENER_EXPRESSION = "task_listener_expression";
  final String PROPERTY_TASK_LISTENER_DELEGATEEXPRESSION = "task_listener_delegate_expression";
  final String PROPERTY_TASK_LISTENER_FIELDS = "task_listener_fields";
  final String PROPERTY_TASK_LISTENER_FIELD_NAME = "task_listener_field_name";
  final String PROPERTY_TASK_LISTENER_FIELD_VALUE = "task_listener_field_value";
  final String PROPERTY_TASK_LISTENER_FIELD_EXPRESSION = "task_listener_field_expression";
	
	final String PROPERTY_EXECUTION_LISTENERS = "executionlisteners";
	final String PROPERTY_EXECUTION_LISTENER_EVENT = "execution_listener_event_type";
	final String PROPERTY_EXECUTION_LISTENER_CLASS = "execution_listener_class";
	final String PROPERTY_EXECUTION_LISTENER_EXPRESSION = "execution_listener_expression";
	final String PROPERTY_EXECUTION_LISTENER_DELEGATEEXPRESSION = "execution_listener_delegate_expression";
        
        final String PROPERTY_EXECUTION_LISTENER_FIELDS = "execution_listener_fields";
        final String PROPERTY_EXECUTION_LISTENER_FIELD_NAME = "execution_listener_field_name";
        final String PROPERTY_EXECUTION_LISTENER_FIELD_VALUE = "execution_listener_field_value";
        final String PROPERTY_EXECUTION_LISTENER_FIELD_EXPRESSION = "execution_listener_field_expression";
	
	final String PROPERTY_FORMKEY = "formkeydefinition";
	final String PROPERTY_DUEDATE = "duedatedefinition";
	final String PROPERTY_PRIORITY = "prioritydefinition";
	
	final String PROPERTY_USERTASK_ASSIGNMENT = "usertaskassignment";
  final String PROPERTY_USERTASK_ASSIGNMENT_TYPE = "assignment_type";
  final String PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION = "resourceassignmentexpr";
  final String PROPERTY_USERTASK_ASSIGNEE = "assignee";
  final String PROPERTY_USERTASK_CANDIDATE_USERS = "candidateUsers";
  final String PROPERTY_USERTASK_CANDIDATE_GROUPS = "candidateGroups";
	
	final String PROPERTY_SERVICETASK_CLASS = "servicetaskclass";
	final String PROPERTY_SERVICETASK_EXPRESSION = "servicetaskexpression";
	final String PROPERTY_SERVICETASK_DELEGATE_EXPRESSION = "servicetaskdelegateexpression";
	final String PROPERTY_SERVICETASK_RESULT_VARIABLE = "servicetaskresultvariable";
	final String PROPERTY_SERVICETASK_FIELDS = "servicetaskfields";
	final String PROPERTY_SERVICETASK_FIELD_NAME = "servicetask_field_name";
	final String PROPERTY_SERVICETASK_FIELD_VALUE = "servicetask_field_value";
	final String PROPERTY_SERVICETASK_FIELD_EXPRESSION = "servicetask_field_expression";
	
	final String PROPERTY_FORM_PROPERTIES = "formproperties";
  final String PROPERTY_FORM_ID = "formproperty_id";
  final String PROPERTY_FORM_NAME = "formproperty_name";
  final String PROPERTY_FORM_TYPE = "formproperty_type";
  final String PROPERTY_FORM_EXPRESSION = "formproperty_expression";
  final String PROPERTY_FORM_VARIABLE = "formproperty_variable";
  final String PROPERTY_FORM_REQUIRED = "formproperty_required";
  final String PROPERTY_FORM_READABLE = "formproperty_readable";
  final String PROPERTY_FORM_WRITEABLE = "formproperty_writeable";
  final String PROPERTY_FORM_FORM_VALUES = "formproperty_formvalues";
  final String PROPERTY_FORM_FORM_VALUE_ID = "formproperty_formvalue_id";
  final String PROPERTY_FORM_FORM_VALUE_NAME = "formproperty_formvalue_name";

	final String PROPERTY_SCRIPT_FORMAT = "scriptformat";
	final String PROPERTY_SCRIPT_TEXT = "scripttext";
	
	final String PROPERTY_RULETASK_CLASS = "ruletask_class";
	final String PROPERTY_RULETASK_VARIABLES_INPUT = "ruletask_variables_input";
	final String PROPERTY_RULETASK_RESULT = "ruletask_result";
	final String PROPERTY_RULETASK_RULES = "ruletask_rules";
	final String PROPERTY_RULETASK_EXCLUDE = "ruletask_exclude";
	
	final String PROPERTY_MAILTASK_TO = "mailtaskto";
	final String PROPERTY_MAILTASK_FROM = "mailtaskfrom";
	final String PROPERTY_MAILTASK_SUBJECT= "mailtasksubject";
	final String PROPERTY_MAILTASK_CC= "mailtaskcc";
	final String PROPERTY_MAILTASK_BCC= "mailtaskbcc";
	final String PROPERTY_MAILTASK_TEXT= "mailtasktext";
	final String PROPERTY_MAILTASK_HTML= "mailtaskhtml";
	final String PROPERTY_MAILTASK_CHARSET= "mailtaskcharset";
	
	final String PROPERTY_CALLACTIVITY_CALLEDELEMENT = "callactivitycalledelement";
  final String PROPERTY_CALLACTIVITY_IN = "callactivityinparameters";
  final String PROPERTY_CALLACTIVITY_OUT = "callactivityoutparameters";
  final String PROPERTY_IOPARAMETER_SOURCE = "ioparameter_source";
  final String PROPERTY_IOPARAMETER_SOURCE_EXPRESSION = "ioparameter_sourceexpression";
  final String PROPERTY_IOPARAMETER_TARGET = "ioparameter_target";
	
	final String PROPERTY_SEQUENCEFLOW_CONDITION = "conditionsequenceflow";
}
