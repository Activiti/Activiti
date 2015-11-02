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
package org.activiti.validation.validator;


/**
 * @author jbarrez
 */
public interface Problems {
	
	String ALL_PROCESS_DEFINITIONS_NOT_EXECUTABLE = "activiti-process-definition-not-executable";
	String PROCESS_DEFINITION_NOT_EXECUTABLE = "activiti-specific-process-definition-not-executable";
	
	String ASSOCIATION_INVALID_SOURCE_REFERENCE = "activiti-asscociation-invalid-source-reference";
	
	String ASSOCIATION_INVALID_TARGET_REFERENCE = "activiti-asscociation-invalid-target-reference";
	
	String EXECUTION_LISTENER_IMPLEMENTATION_MISSING = "activiti-execution-listener-implementation-missing";
	
	String EVENT_LISTENER_IMPLEMENTATION_MISSING = "activiti-event-listener-implementation-missing";
	String EVENT_LISTENER_INVALID_IMPLEMENTATION = "activiti-event-listener-invalid-implementation";
	String EVENT_LISTENER_INVALID_THROW_EVENT_TYPE = "activiti-event-listener-invalid-throw-event-type";
	
	String START_EVENT_MULTIPLE_FOUND = "activiti-start-event-multiple-found";
	String START_EVENT_INVALID_EVENT_DEFINITION = "activiti-start-event-invalid-event-definition";
	
	String SEQ_FLOW_INVALID_SRC = "activiti-seq-flow-invalid-src";
	String SEQ_FLOW_INVALID_TARGET = "activiti-seq-flow-invalid-target";
	
	String USER_TASK_LISTENER_IMPLEMENTATION_MISSING = "activiti-usertask-listener-implementation-missing";

	String SERVICE_TASK_INVALID_TYPE = "activiti-servicetask-invalid-type";
	String SERVICE_TASK_RESULT_VAR_NAME_WITH_DELEGATE = "activiti-servicetask-result-var-name-with-delegate";
	String SERVICE_TASK_MISSING_IMPLEMENTATION = "activiti-servicetask-missing-implementation";
	String SERVICE_TASK_WEBSERVICE_INVALID_OPERATION_REF = "activiti-servicetask-webservice-invalid-operation-ref";
	
	String SEND_TASK_INVALID_IMPLEMENTATION = "activiti-sendtask-invalid-implementation";
	String SEND_TASK_INVALID_TYPE = "activiti-sendtask-invalid-type";
	String SEND_TASK_WEBSERVICE_INVALID_OPERATION_REF = "activiti-send-webservice-invalid-operation-ref";
	
	String SCRIPT_TASK_MISSING_SCRIPT = "activiti-scripttask-missing-script";
	
	String MAIL_TASK_NO_RECIPIENT = "activiti-mailtask-no-recipient";
	String MAIL_TASK_NO_CONTENT = "activiti-mailtask-no-content";
	
	String SHELL_TASK_NO_COMMAND = "activiti-shelltask-no-command";
	String SHELL_TASK_INVALID_PARAM = "activiti-shelltask-invalid-param";
	
	String EXCLUSIVE_GATEWAY_NO_OUTGOING_SEQ_FLOW = "activiti-exclusive-gateway-no-outgoing-seq-flow";
	String EXCLUSIVE_GATEWAY_CONDITION_NOT_ALLOWED_ON_SINGLE_SEQ_FLOW = "activiti-exclusive-gateway-condition-not-allowed-on-single-seq-flow";
	String EXCLUSIVE_GATEWAY_CONDITION_ON_DEFAULT_SEQ_FLOW = "activiti-exclusive-gateway-condition-on-seq-flow";
	String EXCLUSIVE_GATEWAY_SEQ_FLOW_WITHOUT_CONDITIONS = "activiti-exclusive-gateway-seq-flow-without-conditions";
	
	String EVENT_GATEWAY_ONLY_CONNECTED_TO_INTERMEDIATE_EVENTS = "activiti-event-gateway-only-connected-to-intermediate-events";

	String BPMN_MODEL_TARGET_NAMESPACE_TOO_LONG = "activiti-bpmn-model-target-namespace-too-long";

	String PROCESS_DEFINITION_ID_TOO_LONG = "activiti-process-definition-id-too-long";
	String PROCESS_DEFINITION_NAME_TOO_LONG = "activiti-process-definition-name-too-long";
	String PROCESS_DEFINITION_DOCUMENTATION_TOO_LONG = "activiti-process-definition-documentation-too-long";

	String FLOW_ELEMENT_ID_TOO_LONG = "activiti-flow-element-id-too-long";

	String SUBPROCESS_MULTIPLE_START_EVENTS = "activiti-subprocess-multiple-start-event";
	String SUBPROCESS_START_EVENT_EVENT_DEFINITION_NOT_ALLOWED = "activiti-subprocess-start-event-event-definition-not-allowed";
	
	String EVENT_SUBPROCESS_INVALID_START_EVENT_DEFINITION = "activiti-event-subprocess-invalid-start-event-definition";
	
	String BOUNDARY_EVENT_NO_EVENT_DEFINITION = "activiti-boundary-event-no-event-definition";
	String BOUNDARY_EVENT_INVALID_EVENT_DEFINITION = "activiti-boundary-event-invalid-event-definition";
	String BOUNDARY_EVENT_CANCEL_ONLY_ON_TRANSACTION = "activiti-boundary-event-cancel-only-on-transaction";
	String BOUNDARY_EVENT_MULTIPLE_CANCEL_ON_TRANSACTION = "activiti-boundary-event-multiple-cancel-on-transaction";
	
	String INTERMEDIATE_CATCH_EVENT_NO_EVENTDEFINITION = "activiti-intermediate-catch-event-no-eventdefinition";
	String INTERMEDIATE_CATCH_EVENT_INVALID_EVENTDEFINITION = "activiti-intermediate-catch-event-invalid-eventdefinition";
	
	String ERROR_MISSING_ERROR_CODE = "activiti-error-missing-error-code";
	String EVENT_MISSING_ERROR_CODE = "activiti-event-missing-error-code"; 
	String EVENT_TIMER_MISSING_CONFIGURATION = "activiti-event-timer-missing-configuration";
	
	String THROW_EVENT_INVALID_EVENTDEFINITION = "activiti-throw-event-invalid-eventdefinition";
	
	String MULTI_INSTANCE_MISSING_COLLECTION = "activiti-multi-instance-missing-collection";
	
	String MESSAGE_MISSING_NAME = "activiti-message-missing-name";
	String MESSAGE_INVALID_ITEM_REF = "activiti-message-invalid-item-ref";
	String MESSAGE_EVENT_MISSING_MESSAGE_REF = "activiti-message-event-missing-message-ref";
	String MESSAGE_EVENT_INVALID_MESSAGE_REF = "activiti-message-event-invalid-message-ref";
	String MESSAGE_EVENT_MULTIPLE_ON_BOUNDARY_SAME_MESSAGE_ID = "activiti-message-event-multiple-on-boundary-same-message-id";
	
	String OPERATION_INVALID_IN_MESSAGE_REFERENCE = "activiti-operation-invalid-in-message-reference";
	
	String SIGNAL_EVENT_MISSING_SIGNAL_REF = "activiti-signal-event-missing-signal-ref";
	String SIGNAL_EVENT_INVALID_SIGNAL_REF = "activiti-signal-event-invalid-signal-ref";
	
	String COMPENSATE_EVENT_INVALID_ACTIVITY_REF = "activiti-compensate-event-invalid-activity-ref";
	String COMPENSATE_EVENT_MULTIPLE_ON_BOUNDARY = "activiti-compensate-event-multiple-on-boundary";
	
	String SIGNAL_MISSING_ID = "activiti-signal-missing-id";
	String SIGNAL_MISSING_NAME = "activiti-signal-missing-name";
	String SIGNAL_DUPLICATE_NAME = "activiti-signal-duplicate-name";
	String SIGNAL_INVALID_SCOPE = "activiti-signal-invalid-scope";
	
	String DATA_ASSOCIATION_MISSING_TARGETREF = "activiti-data-association-missing-targetref";
	
	String DATA_OBJECT_MISSING_NAME = "activiti-data-object-missing-name";
	
	String END_EVENT_CANCEL_ONLY_INSIDE_TRANSACTION = "activiti-end-event-cancel-only-inside-transaction";
	
	String DI_INVALID_REFERENCE = "activiti-di-invalid-reference";
	String DI_DOES_NOT_REFERENCE_FLOWNODE = "activiti-di-does-not-reference-flownode";
	String DI_DOES_NOT_REFERENCE_SEQ_FLOW = "activiti-di-does-not-reference-seq-flow";
	
}
