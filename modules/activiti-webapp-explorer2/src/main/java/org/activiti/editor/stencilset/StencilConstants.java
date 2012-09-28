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
package org.activiti.editor.stencilset;

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
  final String STENCIL_CALL_ACTIVITY = "CallActivity";
  
  final String STENCIL_TASK_BUSINESS_RULE = "BusinessRule";
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
	
  //stencil properties
	final String PROPERTY_BASE_ATTRIBUTES = "baseAttributes";
	final String PROPERTY_NAME = "name";
	final String PROPERTY_DOCUMENTATION = "documentation";
	
	final String PROPERTY_DIAGRAM_BASE = "diagrambase";
	final String PROPERTY_PROCESS_ID = "process_id";
	final String PROPERTY_PROCESS_VERSION = "process_version";
	final String PROPERTY_PROCESS_AUTHOR = "process_author";
	final String PROPERTY_PROCESS_NAMESPACE = "process_namespace";
	
	final String PROPERTY_TIMER_DEFINITION = "timerdefinition";
	final String PROPERTY_TIMER_DURATON = "timerdurationdefinition";
	final String PROPERTY_TIMER_DATE = "timerdatedefinition";
	final String PROPERTY_TIMER_CYCLE = "timercycledefinition";
	
	final String PROPERTY_MESSAGEREF_DEFINITION = "messagerefdefinition";
	final String PROPERTY_MESSAGEREF = "messageRef";
	
	final String PROPERTY_SIGNALREF_DEFINITION = "signalrefdefinition";
	final String PROPERTY_SIGNALREF = "signalRef";
	
	final String PROPERTY_ERRORREF_DEFINITION = "errorrefdefinition";
	final String PROPERTY_ERRORREF = "errorRef";
	
	final String PROPERTY_NONE_STARTEVENT_BASE = "nonestarteventbase";
	final String PROPERTY_NONE_STARTEVENT_INITIATOR = "initiator";
	
	final String PROPERTY_ACTIVITY_ASYNCHRONOUS = "activityasyncdefinition";
	final String PROPERTY_ASYNCHRONOUS = "asynchronousDefinition";
	final String PROPERTY_EXCLUSIVE = "exclusiveDefinition";
	
	final String PROPERTY_TASK_LISTENERS = "tasklisteners";
	final String PROPERTY_EXECUTION_LISTENERS_DEFINITION = "executionlistenerdefinition";
	final String PROPERTY_EXECUTION_LISTENERS = "executionlisteners";
	
	final String PROPERTY_FORM_DEFINITION = "formdefinition";
	final String PROPERTY_FORMKEY = "formKeyDefinition";
	final String PROPERTY_DUEDATE = "duedateDefinition";
	final String PROPERTY_PRIORITY = "priorityDefinition";
	
	final String PROPERTY_USERTASK_ASSIGNMENT = "usertaskassignment";
	final String PROPERTY_USERTASK_BASE = "usertaskbase";
  final String PROPERTY_USERTASK_ASSIGNMENT_TYPE = "assignment_type";
  final String PROPERTY_USERTASK_ASSIGNMENT_EXPRESSION = "resourceassignmentexpr";
  final String PROPERTY_USERTASK_ASSIGNEE = "assignee";
  final String PROPERTY_USERTASK_CANDIDATE_USERS = "candidateUsers";
  final String PROPERTY_USERTASK_CANDIDATE_GROUPS = "candidateGroups";
	final String PROPERTY_USERTASK_TASKLISTENER = "tasklistenerdefinition";
	
	final String PROPERTY_SERVICETASK_BASE = "servicetaskbase";
	final String PROPERTY_SERVICETASK_CLASS = "servicetaskclass";
	final String PROPERTY_SERVICETASK_EXPRESSION = "servicetaskexpression";
	final String PROPERTY_SERVICETASK_DELEGATE_EXPRESSION = "servicetaskdelegateexpression";
	final String PROPERTY_SERVICETASK_RESULT_VARIABLE = "servicetaskresultvariable";
	final String PROPERTY_SERVICETASK_FIELDS = "servicetaskfields";
	
	final String PROPERTY_FORM_PROPERTIES = "formproperties";
  final String PROPERTY_FORM_ID = "formproperty_id";
  final String PROPERTY_FORM_NAME = "formproperty_name";
  final String PROPERTY_FORM_TYPE = "formproperty_type";
  final String PROPERTY_FORM_EXPRESSION = "formproperty_expression";
  final String PROPERTY_FORM_VARIABLE = "formproperty_variable";
	
	final String PROPERTY_SCRIPTTASK_BASE = "scripttaskbase";
	final String PROPERTY_SCRIPT_FORMAT = "scriptformat";
	final String PROPERTY_SCRIPT_TEXT = "scripttext";
	
	final String PROPERTY_SEQUENCEFLOW_BASE = "sequenceflowbase";
}
