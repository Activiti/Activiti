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
package org.activiti.explorer;


/**
 * @author Joram Barrez
 */
public interface Messages {
  
  // General
  static final String APP_TITLE = "app.title";
  
  static final String CONFIRMATION_DIALOG_DEFAULT_TITLE = "confirmation.dialog.default.title";
  static final String CONFIRMATION_DIALOG_YES = "confirmation.dialog.yes";
  static final String CONFIRMATION_DIALOG_NO = "confirmation.dialog.no";
  
  // Login
  static final String LOGIN_USERNAME = "login.username";
  static final String LOGIN_PASSWORD = "login.password";
  static final String LOGIN_BUTTON = "login.button";
  static final String LOGIN_FAILED_HEADER = "login.failed.header";
  static final String LOGIN_FAILED_INVALID = "login.failed.invalid";
  
  // Header
  static final String HEADER_SEARCHBOX = "header.searchbox";
  static final String HEADER_LOGOUT = "header.logout";

  // Main menu
  static final String MAIN_MENU_TASKS = "main.menu.tasks";
  static final String MAIN_MENU_FLOWS = "main.menu.flows";
  static final String MAIN_MENU_MANAGEMENT = "main.menu.management";
  
  // Profile
  static final String PROFILE_ABOUT = "profile.about";
  static final String PROFILE_NAME = "profile.name";
  static final String PROFILE_JOBTITLE = "profile.jobtitle";
  static final String PROFILE_BIRTHDATE = "profile.birthdate";
  static final String PROFILE_LOCATION = "profile.location";
  static final String PROFILE_CONTACT = "profile.contact";
  static final String PROFILE_EMAIL = "profile.email";
  static final String PROFILE_PHONE = "profile.phone";;
  static final String PROFILE_TWITTER = "profile.twitter";
  static final String PROFILE_SKYPE = "profile.skype";
  static final String PROFILE_ACCOUNTS = "profile.accounts";

  // Task menu
  static final String TASK_MENU_INBOX = "task.menu.inbox";
  static final String TASK_MENU_QUEUED = "task.menu.queued";
  
  // Task details
  static final String TASK_ID = "task.id";
  static final String TASK_NAME = "task.name";
  static final String TASK_OWNER = "task.owner";
  static final String TASK_NO_OWNER = "task.no.owner";
  static final String TASK_ASSIGNEE = "task.assignee";
  static final String TASK_CREATED = "task.created";
  static final String TASK_DUEDATE = "task.duedate";
  static final String TASK_COMPLETE = "task.complete";
  static final String TASK_COMPLETED = "task.task.completed";
  static final String TASK_RESET_FORM = "task.form.reset";
  static final String TASK_ADD_COMMENT = "task.comment.add";
  static final String TASK_COMMENT_POPUP_HEADER = "task.comment.popup.header";
  static final String TASK_CREATE_TIME = "task.create.time";
  static final String TASK_COMPLETE_TIME = "task.complete.time";
  static final String TASK_DURATION = "task.duration";
  static final String TASK_PRIORITY = "task.priority";
  static final String TASK_NOT_FINISHED_YET = "task.not.finished.yet";
  static final String TASK_PART_OF_PROCESS = "task.part.of.process";
  static final String TASK_JUMP_TO_PROCESS = "task.jump.to.process";
  static final String TASK_CLAIM_FAILED = "task.claim.failed";
  static final String TASK_CLAIM_SUCCESS = "task.claim.success";
  static final String TASK_CLAIM = "task.claim";
  static final String TASK_RELATED_CONTENT = "task.related.content";
  static final String TASK_PEOPLE = "task.people";
  
  // Task roles
  static final String TASK_ROLE_CONTRIBUTOR = "task.role.contributor";
  static final String TASK_ROLE_MANAGER = "task.role.manager";
  static final String TASK_ROLE_SPONSOR = "task.role.sponsor";
  static final String TASK_ROLE_IMPLEMENTER = "task.role.implementer";
  
  // Flow menu
  static final String FLOW_MENU_MY_FLOWS = "flow.menu.my";
  static final String FLOW_MENU_LAUNCH_FLOW = "flow.menu.launch";
  
  // Flow page
  static final String FLOW_CATEGORY = "flow.category";
  static final String FLOW_VERSION = "flow.version"; 
  static final String FLOW_DEPLOY_TIME = "flow.deploy.time";
  static final String FLOW_HEADER_DIAGRAM = "flow.header.diagram";
  static final String FLOW_NO_DIAGRAM = "flow.no.diagram";
  static final String FLOW_START = "flow.start";
  static final String FLOW_STARTED_NOTIFICATION = "flow.started.notification";
  static final String FLOW_INSTANCE_STARTED_ON = "flow.instance.started.on";
  static final String FLOW_INSTANCE_HEADER_DIAGRAM = "flow.instance.header.diagram";
  static final String FLOW_INSTANCE_HEADER_TASKS = "flow.instance.header.tasks";
  static final String FLOW_INSTANCE_NO_TASKS = "flow.instance.no.tasks";
  
  // Management menu
  static final String MGMT_MENU_DATABASE = "management.menu.database";
  static final String MGMT_MENU_DEPLOYMENTS = "management.menu.deployments";
  static final String MGMT_MENU_JOBS = "management.menu.jobs";
  static final String MGMT_MENU_DEPLOYMENTS_SHOW_ALL = "management.menu.deployments.show.all";
  static final String MGMT_MENU_DEPLOYMENTS_UPLOAD = "management.menu.deployments.upload";
  
  // Job page
  static final String JOB_EXECUTE = "job.execute";
  static final String JOB_HEADER_EXECUTION = "job.header.execution";
  static final String JOB_RETRIES = "job.retries";
  static final String JOB_NO_RETRIES = "job.no.retries";
  static final String JOB_DEFAULT_NAME = "job.default.name";
  static final String JOB_TIMER = "job.timer";
  static final String JOB_MESSAGE = "job.message";
  static final String JOB_DUEDATE = "job.duedate";
  static final String JOB_NO_DUEDATE = "job.no.dudedate";
  static final String JOB_ERROR = "job.error";
  static final String JOB_NOT_EXECUTED = "job.not.executed";
  
  // Deployment page
  static final String DEPLOYMENT_DELETE = "deployment.delete";
  static final String DEPLOYMENT_CREATE_TIME = "deployment.create.time";
  static final String DEPLOYMENT_HEADER_DEFINITIONS = "deployment.header.definitions";
  static final String DEPLOYMENT_HEADER_RESOURCES = "deployment.header.resources";
  static final String DEPLOYMENT_UPLOAD = "deployment.upload";
  static final String DEPLOYMENT_UPLOAD_DESCRIPTION = "deployment.upload.description";
  static final String DEPLOYMENT_UPLOAD_INVALID_FILE = "deployment.upload.invalid.file";
  static final String DEPLOYMENT_UPLOAD_INVALID_FILE_EXPLANATION = "deployment.upload.invalid.file.explanation";
  static final String DEPLOYMENT_UPLOAD_SERVER_ERROR = "deployment.upload.server.error";
  
  // Database page
  static final String DATABASE_NO_ROWS = "database.no.rows";
  
  // Upload
  static final String UPLOAD_SELECT = "upload.select";
  static final String UPLOAD_DROP = "upload.drop";
  static final String UPLOAD_FAILED = "upload.failed";

  // Related content
  static final String RELATED_CONTENT_ADD = "related.content.add";
  static final String RELATED_CONTENT_NAME = "related.content.name";
  static final String RELATED_CONTENT_NAME_REQUIRED = "related.content.name.required";
  static final String RELATED_CONTENT_DESCRIPTION = "related.content.description";
  static final String RELATED_CONTENT_CREATE = "related.content.create";
  
  static final String RELATED_CONTENT_TYPE_URL = "related.content.type.url";
  static final String RELATED_CONTENT_TYPE_URL_URL = "related.content.type.url.url";
  static final String RELATED_CONTENT_TYPE_URL_URL_REQUIRED = "related.content.type.url.url.required";;
  static final String RELATED_CONTENT_TYPE_URL_HELP = "related.content.type.url.help";
  
  static final String RELATED_CONTENT_TYPE_FILE = "related.content.type.file";
  static final String RELATED_CONTENT_TYPE_FILE_HELP = "related.content.type.file.help";
  static final String RELATED_CONTENT_TYPE_FILE_UPLOADED = "related.content.type.file.uploaded";
  static final String RELATED_CONTENT_TYPE_FILE_REQUIRED = "related.content.type.file.required";
  static final String RELATED_CONTENT_CONFIRM_DELETE = "related.content.confirm.delete";
  
  // People involvement
  static final String PEOPLE_SEARCH = "people.search";
  static final String PEOPLE_INVOLVE_POPUP_CAPTION = "people.involve.popup.caption";

}
