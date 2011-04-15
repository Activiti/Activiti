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
  static final String BUTTON_OK = "button.ok";
  
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
  static final String MAIN_MENU_REPORTS = "main.menu.reports";
  
  // Password
  static final String PASSWORD_CHANGE = "password.change";
  static final String PASSWORD_CHANGE_INPUT_REQUIRED = "password.change.input.required";
  static final String PASSWORD_CHANGE_INPUT_MATCH = "password.change.input.match";
  static final String PASSWORD_CHANGED_NOTIFICATION = "password.changed.notification";
  
  // Profile
  static final String PROFILE_ABOUT = "profile.about";
  static final String PROFILE_NAME = "profile.name";
  static final String PROFILE_FIRST_NAME = "profile.firstname";
  static final String PROFILE_LAST_NAME = "profile.lastname";
  static final String PROFILE_JOBTITLE = "profile.jobtitle";
  static final String PROFILE_BIRTHDATE = "profile.birthdate";
  static final String PROFILE_LOCATION = "profile.location";
  static final String PROFILE_CONTACT = "profile.contact";
  static final String PROFILE_EMAIL = "profile.email";
  static final String PROFILE_PHONE = "profile.phone";;
  static final String PROFILE_TWITTER = "profile.twitter";
  static final String PROFILE_SKYPE = "profile.skype";
  static final String PROFILE_ACCOUNTS = "profile.accounts";
  static final String PROFILE_SHOW = "profile.show";
  static final String PROFILE_EDIT = "profile.edit";
  static final String PROFILE_SAVE = "profile.save";
  static final String PROFILE_CHANGE_PICTURE = "profile.change.picture";
  static final String PROFILE_ACCOUNT_USER_NAME = "profile.account.username";
  static final String PROFILE_ACCOUNT_PASWORD = "profile.account.password";
  static final String PROFILE_DELETE_ACCOUNT_TITLE = "profile.delete.account.title";
  static final String PROFILE_DELETE_ACCOUNT_DESCRIPTION = "profile.delete.account.description";
  static final String PROFILE_ADD_ACCOUNT = "profile.add.account";
  static final String PROFILE_ACCOUNT_IMAP_DESCRIPTION = "profile.account.imap.description";
  static final String PROFILE_ACCOUNT_IMAP = "profile.account.imap";
  static final String PROFILE_ACCOUNT_ALFRESCO = "profile.account.alfresco";
  static final String PROFILE_NEW_PASSWORD = "profile.new.password";
  static final String PROFILE_CONFIRM_PASSWORD = "profile.confirm.password";
  
  // Imap
  static final String IMAP_SERVER = "imap.server";
  static final String IMAP_PORT = "imap.port";
  static final String IMAP_SSL = "imap.ssl";
  static final String IMAP_USERNAME = "imap.username";
  static final String IMAP_PASSWORD = "imap.password";
  static final String IMAP_DESCRIPTION = "imap.description";
  
  //Alfresco
  static final String ALFRESCO_SERVER = "alfresco.server";
  static final String ALFRESCO_USERNAME = "alfresco.username";
  static final String ALFRESCO_PASSWORD = "alfresco.password";
  static final String ALFRESCO_DESCRIPTION = "alfresco.description";

  // Task menu
  static final String TASK_MENU_INBOX = "task.menu.inbox";
  static final String TASK_MENU_QUEUED = "task.menu.queued";
  
  // Task details
  static final String TASK_ID = "task.id";
  static final String TASK_NAME = "task.name";
  static final String TASK_OWNER = "task.owner";
  static final String TASK_OWNER_TRANSFER = "task.owner.transfer";
  static final String TASK_NO_OWNER = "task.no.owner";
  static final String TASK_ASSIGNEE = "task.assignee";
  static final String TASK_ASSIGNEE_REASSIGN = "task.assignee.reassign";
  static final String TASK_INVOLVED_REMOVE = "task.involved.remove";
  static final String TASK_INVOLVED_REMOVE_CONFIRMATION_TITLE = "task.involved.remove.confirmation.title";
  static final String TASK_INVOLVED_REMOVE_CONFIRMATION_DESCRIPTION = "task.involved.remove.confirmation.description";
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
  
  // Events
  static final String EVENT_ADD_IDENTITYLINK = "event.add.identitylink";
  static final String EVENT_ADD_ATTACHMENT = "event.add.attachment";
  static final String EVENT_COMMENT = "event.comment";
  static final String EVENT_DEFAULT = "event.default";
  
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
  static final String MGMT_MENU_USERS = "management.menu.users";
  
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
  
  // User page
  static final String USER_HEADER_DETAILS = "user.header.details";
  static final String USER_HEADER_GROUPS = "user.header.groups";
  static final String USER_ID = "user.id";
  static final String USER_ID_REQUIRED = "user.id.required";
  static final String USER_ID_UNIQUE = "user.id.unique";
  static final String USER_FIRSTNAME = "user.firstname";
  static final String USER_LASTNAME = "user.lastname";
  static final String USER_EMAIL = "user.email";
  static final String USER_PASSWORD = "user.password";
  static final String USER_PASSWORD_REQUIRED = "user.password.required";
  static final String USER_PASSWORD_MIN_LENGTH = "user.password.min.lenth";
  static final String USER_RESET_PASSWORD = "user.reset.password";
  static final String USER_CREATE = "user.create";
  static final String USER_EDIT = "user.edit";
  static final String USER_DELETE = "user.delete";
  static final String USER_SAVE = "user.save";
  static final String USER_NO_PICTURE = "user.no.picture";
  static final String USER_NO_GROUPS = "user.no.groups";
  static final String USER_CONFIRM_DELETE = "user.confirm.delete";
  static final String USER_CONFIRM_DELETE_GROUP = "user.confirm.delete.group";
  static final String USER_SELECT_GROUPS = "user.select.groups";
  static final String USER_SELECT_GROUPS_POPUP = "user.select.groups.popup";
  
  // Upload
  static final String UPLOAD_SELECT = "upload.select";
  static final String UPLOAD_DROP = "upload.drop";
  static final String UPLOAD_FAILED = "upload.failed";
  static final String UPLOAD_LIMIT = "upload.limit";
  static final String UPLOAD_INVALID_MIMETYPE = "upload.invalid.mimetype";

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

  static final String TASK_AUTHORISATION_ERROR_TITLE = "task.authorisation.error.title";
  static final String TASK_AUTHORISATION_MEMBERSHIP_ERROR = "task.authorisation.membership.error";
  static final String TASK_AUTHORISATION_INBOX_ERROR = "task.authorisation.inbox.error";
}
