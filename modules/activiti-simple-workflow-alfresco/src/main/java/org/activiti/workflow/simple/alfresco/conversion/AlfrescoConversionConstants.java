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
package org.activiti.workflow.simple.alfresco.conversion;

import org.activiti.workflow.simple.alfresco.model.M2Namespace;

/**
 * @author Frederik Heremans
 */
public interface AlfrescoConversionConstants {

	// Content model constants
	/**
	 * Prefix for the id of created process definitions. 
	 */
	String PROCESS_ID_PREFIX = "activiti";
	
	/**
	 * Name of the shared content-model, without namespace prefix
	 */
	String CONTENT_MODEL_UNQUALIFIED_NAME = "model";
	
	/**
	 * Full namespace URL template, with {0} placeholder for the actual name
	 */
	String CONTENT_MODEL_NAMESPACE_URL = "http://www.alfresco.org/model/dynamic-workflow/{0}/1.0";
	
	M2Namespace BPM_NAMESPACE = new M2Namespace("http://www.alfresco.org/model/bpm/1.0", "bpm");
	M2Namespace WORKFLOW_NAMESPACE = new M2Namespace("http://www.alfresco.org/model/workflow/1.0", "wf");
	M2Namespace DICTIONARY_NAMESPACE = new M2Namespace("http://www.alfresco.org/model/dictionary/1.0", "d");
	M2Namespace CONTENT_NAMESPACE = new M2Namespace("http://www.alfresco.org/model/content/1.0", "cm");
	
	String CONTENT_MODEL_CONSTRAINT_TYPE_LIST = "LIST";
	String CONTENT_MODEL_CONSTRAINT_ALLOWED_VALUES = "allowedValues";
	
	/**
	 * Default start form key
	 */
	String DEFAULT_START_FORM_TYPE = "bpm:startTask";
	String START_TASK_SIMPLE_NAME = "start";
	
	/**
	 * Base type for all task models
	 */
	String DEFAULT_BASE_FORM_TYPE = "bpm:workflowTask";
	String OUTCOME_BASE_FORM_TYPE = "bpm:activitiOutcomeTask";
	
	String PROPERTY_WORKFLOW_DESCRIPTION = "bpm:workflowDescription";
	String PROPERTY_WORKFLOW_DUE_DATE = "bpm:workflowDueDate";
	String PROPERTY_WORKFLOW_PRIORITY = "bpm:workflowPriority";
	String PROPERTY_SEND_EMAIL_NOTIFICATIONS = "bpm:sendEMailNotifications";
	String PROPERTY_COMMENT = "bpm:comment";
	String PROPERTY_PACKAGEITEMS = "packageItems";
	
	String PROPERTY_PACKAGEITEMS_ITEM_ACTION_GROUP = "bpm:packageItemActionGroup";
	String PROPERTY_PACKAGEITEMS_ACTION_GROUP = "bpm:packageActionGroup";
	String PROPERTY_PACKAGEITEMS_ACTION_GROUP_ADD = "add_package_item_actions";
	String PROPERTY_PACKAGEITEMS_ITEM_ACTION_GROUP_REMOVE = "edit_and_remove_package_item_actions";
	String PROPERTY_PACKAGEITEMS_ITEM_ACTION_GROUP_EDIT = "edit_package_item_actions";
	String PROPERTY_OUTCOME_PROPERTY_NAME = "bpm:outcomePropertyName";
	
	String PROPERTY_TYPE_TEXT = "d:text";
	String PROPERTY_TYPE_DATE = "d:date";
	String PROPERTY_TYPE_DOUBLE = "d:double";
	String PROPERTY_TYPE_DATETIME = "d:datetime";
	String PROPERTY_TYPE_BOOLEAN = "d:boolean";
	
	String CONTENT_TYPE_PEOPLE = "cm:person";
	String CONTENT_TYPE_GROUP = "cm:authorityContainer";
	String CONTENT_TYPE_CONTENT = "cm:content";
	
	String PROPERTY_DUE_DATE = "bpm:dueDate";
	String PROPERTY_PRIORITY = "bpm:priority";
	String PROPERTY_DESCRIPTION = "bpm:description";
	String PROPERTY_TRANSITIONS_SUFFIX = "transitions";
	
	// Form constants
	/**
	 * Module id template, with {0} placeholder for unique id
	 */
	String MODULE_ID = "kickstart_form_{0}";
	
	String EVALUATOR_STRING_COMPARE = "string-compare";
	String EVALUATOR_TASK_TYPE = "task-type";
	
	String FORM_SET_APPEARANCE_TITLE = "title";
	
	String FORM_SET_TEMPLATE_2_COLUMN = "/org/alfresco/components/form/2-column-set.ftl";
	
	String FORM_SET_TEMPLATE_3_COLUMN = "/org/alfresco/components/form/3-column-set.ftl";
	
	String FORM_READONLY_TEMPLATE = "/org/alfresco/components/form/controls/info.ftl";
	String FORM_MULTILINE_TEXT_TEMPLATE = "/org/alfresco/components/form/controls/textarea.ftl";
	String FORM_TEXT_TEMPLATE = "/org/alfresco/components/form/controls/textfield.ftl";
	String FORM_DATE_TEMPLATE = "/org/alfresco/components/form/controls/date.ftl";
	String FORM_DATE_PARAM_SHOW_TIME = "showTime";
	String FORM_DATE_PARAM_SUBMIT_TIME = "submitTime";
	String FORM_NUMBER_TEMPLATE = "/org/alfresco/components/form/controls/number.ftl";
	String FORM_PRIORITY_TEMPLATE = "/org/alfresco/components/form/controls/workflow/priority.ftl";
	String FORM_EMAIL_NOTIFICATION_TEMPLATE = "/org/alfresco/components/form/controls/workflow/email-notification.ftl";
	String FORM_TRANSITIONS_TEMPLATE = "/org/alfresco/components/form/controls/workflow/activiti-transitions.ftl";
	String FORM_PACKAGE_ITEMS_TEMPLATE = "/org/alfresco/components/form/controls/workflow/packageitems.ftl";
	String FORM_PACKAGE_ITEMS_PARAM_ROOTNODE = "rootNode";
	String FORM_PACKAGE_ITEMS_PARAM_ROOTNODE_DEFAULT = "{siteshome}";
	
	String FORM_SET_GENERAL = "";
	String FORM_SET_INFO = "info";
	String FORM_SET_ASSIGNEE = "assignee";
	String FORM_SET_ITEMS = "items";
	String FORM_SET_OTHER = "other";
	String FORM_SET_RESPONSE = "response";
	String FORM_SET_GENERAL_LABEL = "workflow.set.general";
	String FORM_SET_ASSIGNEE_LABEL = "workflow.set.assignee";
	String FORM_SET_ITEMS_LABEL = "workflow.set.items";
	String FORM_SET_OTHER_LABEL = "workflow.set.other";
	String FORM_WORKFLOW_DESCRIPTION_LABEL = "workflow.field.message";
	String FORM_COMMENT_LABEL = "workflow.field.comment";
	String FORM_WORKFLOW_DUE_DATE_LABEL = "workflow.field.due";
	String FORM_WORKFLOW_PRIORITY_LABEL = "workflow.field.priority";
	
	String FORM_FIELD_TRANSITIONS = "transitions";
	
	// Custom property definition constants
	String FORM_GROUP_LAYOUT_1_COLUMN = "one-column";
	String FORM_GROUP_LAYOUT_2_COLUMNS = "two-column";
	String FORM_GROUP_LAYOUT_3_COLUMNS = "three-column";
	
	String FORM_REFERENCE_DUEDATE = "duedate";
	String FORM_REFERENCE_EMAIL_NOTIFICATION = "email-notification";
	String FORM_REFERENCE_COMMENT = "comment";
	String FORM_REFERENCE_PRIORITY = "priority";
	String FORM_REFERENCE_WORKFLOW_DESCRIPTION = "workflow-description";
	String FORM_REFERENCE_PACKAGE_ITEMS = "package-items";
	String FORM_REFERENCE_FIELD = "field";
	
	// Process constants
	String INITIATOR_VARIABLE = "initiatorUserName";
  String INITIATOR_ASSIGNEE_EXPRESSION = "${initiator.properties.userName}";
	
	/**
	 * Evaluator condition template, with {0} placeholder for the task/workflow key 
	 */
	String EVALUATOR_CONDITION_ACTIVITI = "activiti${0}";
	
	// Custom form definition parameters
	String PARAMETER_PACKAGEITEMS_ALLOW_ADD = "allow-add";
	String PARAMETER_PACKAGEITEMS_ALLOW_REMOVE = "allow-remove";
	String PARAMETER_ADD_PROPERTY_TO_OUTPUT = "property-output";
	String PARAMETER_REFERENCE_MANY = "reference-many";
	String PARAMETER_FORCE_NOTOFICATIONS = "force-notifications";
	String PARAMETER_SCRIPT_TASK_RUNAS = "run-as";
	
	
	// Artifact keys
	String ARTIFACT_CONTENT_MODEL_KEY = "contentModel";
	String ARTIFACT_SHARE_CONFIG_EXTENSION = "configExtension";
	String ARTIFACT_MODEL_NAMESPACE_PREFIX = "modelNamespacePrefix";
	String ARTIFACT_PROPERTY_SHARING = "propertySharing";
	String ARTIFACT_PROPERTY_TASK_SCRIPT_BUILDER = "scriptTaskListenerBuilder";
	String ARTIFACT_PROPERTY_REFERENCES = "propertyReferences";

	// Listener and extension elements
	String CLASSNAME_SCRIPT_TASK_LISTENER = "org.alfresco.repo.workflow.activiti.tasklistener.ScriptTaskListener";
	String CLASSNAME_SCRIPT_DELEGATE = "org.alfresco.repo.workflow.activiti.script.AlfrescoScriptDelegate";
	String SCRIPT_TASK_LISTENER_SCRIPT_FIELD_NAME = "script";
	String SCRIPT_DELEGATE_SCRIPT_FIELD_NAME = "script";
	String SCRIPT_DELEGATE_RUN_AS_FIELD_NAME = "runAs";
	String TASK_LISTENER_EVENT_CREATE = "create";
	String TASK_LISTENER_EVENT_COMPLETE = "complete";

	String TRANSITION_APPROVE = "Approve";
	String TRANSITION_REJECT = "Reject";
}
