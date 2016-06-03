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
  String APP_TITLE = "app.title";
  
  String CONFIRMATION_DIALOG_DEFAULT_TITLE = "confirmation.dialog.default.title";
  String CONFIRMATION_DIALOG_YES = "confirmation.dialog.yes";
  String CONFIRMATION_DIALOG_NO = "confirmation.dialog.no";
  String BUTTON_OK = "button.ok";
  String BUTTON_CREATE = "button.create";
  String BUTTON_CANCEL = "button.cancel";
  String BUTTON_SAVE = "button.save";
  String BUTTON_DELETE = "button.delete";
  String UNCAUGHT_EXCEPTION = "uncaught.exception";
  
  // Navigation
  String NAVIGATION_ERROR_NOT_INVOLVED_TITLE = "navigation.error.not.involved.title";
  String NAVIGATION_ERROR_NOT_INVOLVED = "navigation.error.not.involved";
  
  // Login
  String LOGIN_USERNAME = "login.username";
  String LOGIN_PASSWORD = "login.password";
  String LOGIN_BUTTON = "login.button";
  String LOGIN_FAILED_HEADER = "login.failed.header";
  String LOGIN_FAILED_INVALID = "login.failed.invalid";
  
  // Header
  String HEADER_SEARCHBOX = "header.searchbox";
  String HEADER_LOGOUT = "header.logout";
  
  // Footer
  String FOOTER_MESSAGE = "footer.message";

  // Main menu
  String MAIN_MENU_TASKS = "main.menu.tasks";
  String MAIN_MENU_PROCESS = "main.menu.process";
  String MAIN_MENU_MANAGEMENT = "main.menu.management";
  String MAIN_MENU_REPORTS = "main.menu.reports";
  
  // Password
  String PASSWORD_CHANGE = "password.change";
  String PASSWORD_CHANGE_INPUT_REQUIRED = "password.change.input.required";
  String PASSWORD_CHANGE_INPUT_MATCH = "password.change.input.match";
  String PASSWORD_CHANGED_NOTIFICATION = "password.changed.notification";
  
  // Forms
  String FORM_USER_NO_USER_SELECTED = "form.user.no.user.selected";
  String FORM_USER_SELECT = "form.user.select";
  String FORM_FIELD_REQUIRED = "form.field.required";
  
  // Profile
  String PROFILE_ABOUT = "profile.about";
  String PROFILE_NAME = "profile.name";
  String PROFILE_FIRST_NAME = "profile.firstname";
  String PROFILE_LAST_NAME = "profile.lastname";
  String PROFILE_JOBTITLE = "profile.jobtitle";
  String PROFILE_BIRTHDATE = "profile.birthdate";
  String PROFILE_LOCATION = "profile.location";
  String PROFILE_CONTACT = "profile.contact";
  String PROFILE_EMAIL = "profile.email";
  String PROFILE_PHONE = "profile.phone";
  String PROFILE_TWITTER = "profile.twitter";
  String PROFILE_SKYPE = "profile.skype";
  String PROFILE_ACCOUNTS = "profile.accounts";
  String PROFILE_SHOW = "profile.show";
  String PROFILE_EDIT = "profile.edit";
  String PROFILE_SAVE = "profile.save";
  String PROFILE_CHANGE_PICTURE = "profile.change.picture";
  String PROFILE_ACCOUNT_USER_NAME = "profile.account.username";
  String PROFILE_ACCOUNT_PASWORD = "profile.account.password";
  String PROFILE_DELETE_ACCOUNT_TITLE = "profile.delete.account.title";
  String PROFILE_DELETE_ACCOUNT_DESCRIPTION = "profile.delete.account.description";
  String PROFILE_ADD_ACCOUNT = "profile.add.account";
  String PROFILE_ACCOUNT_IMAP_DESCRIPTION = "profile.account.imap.description";
  String PROFILE_ACCOUNT_IMAP = "profile.account.imap";
  String PROFILE_ACCOUNT_ALFRESCO = "profile.account.alfresco";
  String PROFILE_NEW_PASSWORD = "profile.new.password";
  String PROFILE_CONFIRM_PASSWORD = "profile.confirm.password";
  
  // Imap
  String IMAP_SERVER = "imap.server";
  String IMAP_PORT = "imap.port";
  String IMAP_SSL = "imap.ssl";
  String IMAP_USERNAME = "imap.username";
  String IMAP_PASSWORD = "imap.password";
  String IMAP_DESCRIPTION = "imap.description";
  
  //Alfresco
  String ALFRESCO_SERVER = "alfresco.server";
  String ALFRESCO_USERNAME = "alfresco.username";
  String ALFRESCO_PASSWORD = "alfresco.password";
  String ALFRESCO_DESCRIPTION = "alfresco.description";
  
  // Case
  String TASK_CREATE_NEW = "task.create.new";
  String TASK_NEW = "task.new";
  String TASK_NAME_REQUIRED = "task.name.required";

  // Task menu
  String TASK_MENU_TASKS = "task.menu.tasks";
  String TASK_MENU_INBOX = "task.menu.inbox";
  String TASK_MENU_QUEUED = "task.menu.queued";
  String TASK_MENU_INVOLVED = "task.menu.involved";
  String TASK_MENU_ARCHIVED = "task.menu.archived";
  
  // Task details
  String TASK_ID = "task.id";
  String TASK_NAME = "task.name";
  String TASK_DESCRIPTION = "task.description";
  String TASK_NO_DESCRIPTION = "task.no.description";
  String TASK_OWNER = "task.owner";
  String TASK_OWNER_TRANSFER = "task.owner.transfer";
  String TASK_NO_OWNER = "task.no.owner";
  String TASK_ASSIGNEE = "task.assignee";
  String TASK_NO_ASSIGNEE = "task.no.assignee";
  String TASK_ASSIGNEE_REASSIGN = "task.assignee.reassign";
  String TASK_INVOLVED_REMOVE = "task.involved.remove";
  String TASK_INVOLVED_REMOVE_CONFIRMATION_TITLE = "task.involved.remove.confirmation.title";
  String TASK_INVOLVED_REMOVE_CONFIRMATION_DESCRIPTION = "task.involved.remove.confirmation.description";
  String TASK_CREATED_SHORT = "task.created.short";
  String TASK_DUEDATE = "task.duedate";
  String TASK_DUEDATE_UNKNOWN = "task.duedate.unknown";
  String TASK_DUEDATE_SHORT = "task.duedate.short";
  String TASK_COMPLETE = "task.complete";
  String TASK_COMPLETED = "task.task.completed";
  String TASK_RESET_FORM = "task.form.reset";
  String TASK_ADD_COMMENT = "task.comment.add";
  String TASK_COMMENT_POPUP_HEADER = "task.comment.popup.header";
  String TASK_CREATE_TIME = "task.create.time";
  String TASK_COMPLETE_TIME = "task.complete.time";
  String TASK_DURATION = "task.duration";
  String TASK_PRIORITY = "task.priority";
  String TASK_PRIORITY_LOW = "task.priority.low";
  String TASK_PRIORITY_MEDIUM = "task.priority.medium";
  String TASK_PRIORITY_HIGH = "task.priority.high";
  String TASK_NOT_FINISHED_YET = "task.not.finished.yet";
  String TASK_PART_OF_PROCESS = "task.part.of.process";
  String TASK_SUBTASK_OF_PARENT_TASK = "task.subtask.of.parent.task";
  String TASK_JUMP_TO_PROCESS = "task.jump.to.process";
  String TASK_CLAIM_FAILED = "task.claim.failed";
  String TASK_CLAIM_SUCCESS = "task.claim.success";
  String TASK_CLAIM = "task.claim";
  String TASK_RELATED_CONTENT = "task.related.content";
  String TASK_NO_RELATED_CONTENT = "task.no.related.content";
  String TASK_PEOPLE = "task.people";
  String TASK_FORM_HELP = "task.form.help";
  String TASK_SUBTASKS = "task.subtasks";
  String TASK_NO_SUBTASKS = "task.no.subtasks";
  String TASK_CONFIRM_DELETE_SUBTASK = "task.confirm.delete.subtask";
  
  // Task roles
  String TASK_ROLE_CONTRIBUTOR = "task.role.contributor";
  String TASK_ROLE_MANAGER = "task.role.manager";
  String TASK_ROLE_SPONSOR = "task.role.sponsor";
  String TASK_ROLE_IMPLEMENTER = "task.role.implementer";
        
  // Events
  String EVENT_ADD_USER_LINK = "event.add.user.link";
  String EVENT_DELETE_USER_LINK = "event.delete.user.link";
  String EVENT_ADD_GROUP_LINK = "event.add.group.link";
  String EVENT_DELETE_GROUP_LINK = "event.delete.group.link";
  String EVENT_ADD_ATTACHMENT = "event.add.attachment";
  String EVENT_DELETE_ATTACHMENT = "event.delete.attachment";
  String EVENT_COMMENT = "event.comment";
  String EVENT_DEFAULT = "event.default";
  String EVENT_TITLE = "event.title";
  
  // Process menu
  String PROCESS_MENU_MY_INSTANCES = "process.menu.my.instances";
  String PROCESS_MENU_DEPLOYED_DEFINITIONS = "process.menu.deployed.definitions";
  String PROCESS_MENU_EDITOR_DEFINITIONS = "process.menu.editor.definitions";
  String PROCESS_MENU_INSTANCES = "process.menu.instances";
  
  // Process page
  String PROCESS_CATEGORY = "process.category";
  String PROCESS_VERSION = "process.version"; 
  String PROCESS_DEPLOY_TIME = "process.deploy.time";
  String PROCESS_HEADER_DIAGRAM = "process.header.diagram";
  String PROCESS_NO_DIAGRAM = "process.no.diagram";
  String PROCESS_HEADER_SUSPENSION_STATE = "process.header.suspension.state";
  String PROCESS_SCHEDULED_SUSPEND = "process.scheduled.suspend";
  String PROCESS_SCHEDULED_ACTIVATE = "process.scheduled.activate";
  String PROCESS_START = "process.start";
  String PROCESS_EDIT = "process.edit";
  String PROCESS_COPY = "process.copy";
  String PROCESS_NEW = "process.new";
  String PROCESS_IMPORT = "process.import";
  String PROCESS_DELETE = "process.delete";
  String PROCESS_DEPLOY = "process.deploy";
  String PROCESS_ACTIVATE = "process.activate";
  String PROCESS_ACTIVATE_POPUP = "process.activate.popup";
  String PROCESS_ACTIVATE_POPUP_TIME_DESCRIPTION = "process.activate.popup.time.description";
  String PROCESS_ACTIVATE_POPUP_INCLUDE_PROCESS_INSTANCES_DESCRIPTION = "process.activate.popup.process.instances.description";
  String PROCESS_SUSPEND = "process.suspend";
  String PROCESS_SUSPEND_POPUP = "process.suspend.popup";
  String PROCESS_SUSPEND_POPUP_TIME_DESCRIPTION = "process.suspend.popup.time.description";
  String PROCESS_SUSPEND_POPUP_TIME_NOW = "process.suspend.popup.time.now";
  String PROCESS_SUSPEND_POPUP_TIME_DATE = "process.suspend.popup.time.date";
  String PROCESS_SUSPEND_POPUP_INCLUDE_PROCESS_INSTANCES_DESCRIPTION = "process.suspend.popup.process.instances.description";
  String PROCESS_TOXML_FAILED = "process.toxml.failed";
  String PROCESS_CONVERT = "process.convert";
  String PROCESS_EXPORT = "process.export";
  String PROCESS_EDITOR_CHOICE = "process.editor.choice";
  String PROCESS_EDITOR_MODELER = "process.editor.modeler";
  String PROCESS_EDITOR_MODELER_DESCRIPTION = "process.editor.modeler.description";
  String PROCESS_EDITOR_CONVERSION_WARNING_MODELER = "process.editor.conversion.warning.modeler";
  String PROCESS_EDITOR_TABLE = "process.editor.table";
  String PROCESS_EDITOR_TABLE_DESCRIPTION = "process.editor.table.description";
  String PROCESS_EDITOR_CREATE_NEW = "process.editor.create.new";
  String PROCESS_EDITOR_CREATE_NEW_DEFAULT = "process.editor.create.new.default";
  String PROCESS_EDITOR_TITLE = "process.editor.title";
  String PROCESS_EDITOR_BPMN_PREVIEW = "process.editor.bpmn.preview";
  String PROCESS_EDITOR_SAVE = "process.editor.save";
  String PROCESS_EDITOR_NAME = "process.editor.name";
  String PROCESS_EDITOR_DESCRIPTION = "process.editor.description";
  String PROCESS_EDITOR_TASKS = "process.editor.tasks";
  String PROCESS_EDITOR_TASK_NAME = "process.editor.task.name";
  String PROCESS_EDITOR_TASK_ASSIGNEE = "process.editor.task.assignee";
  String PROCESS_EDITOR_TASK_GROUPS = "process.editor.task.groups";
  String PROCESS_EDITOR_TASK_DESCRIPTION = "process.editor.task.description";
  String PROCESS_EDITOR_TASK_CONCURRENCY = "process.editor.task.concurrency";
  String PROCESS_EDITOR_TASK_START_WITH_PREVIOUS = "process.editor.task.startwithprevious";
  String PROCESS_EDITOR_TASK_FORM_CREATE = "process.editor.task.form.create";
  String PROCESS_EDITOR_TASK_FORM_EDIT = "process.editor.task.form.edit";
  String PROCESS_EDITOR_ACTIONS = "process.editor.actions";
  String PROCESS_EDITOR_PROPERTY_NAME = "process.editor.property.name";
  String PROCESS_EDITOR_PROPERTY_TYPE = "process.editor.property.type";
  String PROCESS_EDITOR_PROPERTY_REQUIRED = "process.editor.property.required";
  String PROCESS_EDITOR_PROPERTY_TYPE_TEXT = "process.editor.property.type.text";
  String PROCESS_EDITOR_PROPERTY_TYPE_NUMBER = "process.editor.property.type.number";
  String PROCESS_EDITOR_PROPERTY_TYPE_DATE = "process.editor.property.type.date";
  String PROCESS_EDITOR_LOADING_ERROR = "process.editor.loading.error";
  
  String PROCESS_INSTANCE_DELETE = "process.instance.delete";
  String PROCESS_INSTANCE_DELETE_POPUP_TITLE = "process.instance.delete.popup.title";
  String PROCESS_INSTANCE_DELETE_POPUP_DESCRIPTION = "process.instance.delete.popup.description";
  String PROCESS_START_TIME = "process.start.time";
  String PROCESS_STARTED_NOTIFICATION = "process.started.notification";
  String PROCESS_INSTANCE_STARTED_ON = "process.instance.started.on";
  String PROCESS_INSTANCE_STARTED = "process.instance.started";
  String PROCESS_INSTANCE_ENDED = "process.instance.ended";
  String PROCESS_INSTANCE_HEADER_TASKS = "process.instance.header.tasks";
  String PROCESS_INSTANCE_NO_TASKS = "process.instance.no.tasks";
  String PROCESS_INSTANCE_HEADER_VARIABLES = "process.instance.header.variables";
  String PROCESS_INSTANCE_NO_VARIABLES = "process.instance.no.variables";
  String PROCESS_INSTANCES = "process.instances";
  String PROCESS_NO_INSTANCES = "process.no.instances";
  String PROCESS_ACTION_VIEW = "process.action.view";
  String PROCESS_INSTANCE_ID = "process.instance.id";
  String PROCESS_INSTANCE_NAME = "process.instance.name";
  String PROCESS_INSTANCE_BUSINESSKEY = "process.instance.businesskey";
  String PROCESS_INSTANCE_ACTIONS = "process.instance.actions";
  String PROCESS_INSTANCE_VARIABLE_NAME = "process.instance.variable.name";
  String PROCESS_INSTANCE_VARIABLE_VALUE = "process.instance.variable.value";
  String PROCESS_CONVERT_POPUP_CAPTION = "process.convert.popup.caption";
  String PROCESS_CONVERT_POPUP_MESSAGE = "process.convert.popup.message";
  String PROCESS_CONVERT_POPUP_CONVERT_BUTTON = "process.convert.popup.convert.button";
  String PROCESS_NEW_POPUP_CAPTION = "process.new.popup.caption";
  String PROCESS_NEW_POPUP_CREATE_BUTTON = "process.new.popup.create.button";
  String PROCESS_COPY_POPUP_CAPTION = "process.copy.popup.caption";
  String PROCESS_DELETE_POPUP_CAPTION = "process.delete.popup.caption";
  String PROCESS_DELETE_POPUP_MESSAGE = "process.delete.popup.message";
  String PROCESS_DELETE_POPUP_DELETE_BUTTON = "process.delete.popup.delete.button";
  
  // Reporting menu
  String REPORTING_MENU_RUN_REPORTS = "reporting.menu.run.reports";
  String REPORTING_MENU_SAVED_REPORTS = "reporting.menu.saved.reports";
  String REPORTING_SAVE_POPUP_CAPTION = "reporting.save.popup.caption";
  String REPORTING_ERROR_NOT_ENOUGH_DATA = "reporting.error.not.enough.data";
  String REPORTING_SAVE_POPUP_NAME = "reporting.save.popup.name";
  String REPORTING_SAVE_POPUP_NAME_EMPTY = "reporting.save.popup.name.empty";
  String REPORTING_SAVE_POPUP_NAME_EXISTS = "reporting.save.popup.name.exists";
  String REPORTING_SAVE_POPUP_NAME_TOO_LONG = "reporting.save.popup.name.too.long";
  String REPORTING_CREATE_TIME = "reporting.report.created";
  String REPORTING_GENERATE_REPORT = "reporting.generatereport";
  
  // Management menu
  String MGMT_MENU_DATABASE = "management.menu.database";
  String MGMT_MENU_DEPLOYMENTS = "management.menu.deployments";
  String MGMT_MENU_ACTIVE_PROCESS_DEFINITIONS = "management.menu.active.processdefinitions";
  String MGMT_MENU_SUSPENDED_PROCESS_DEFINITIONS = "management.menu.suspended.processdefinitions";
  String MGMT_MENU_JOBS = "management.menu.jobs";
  String MGMT_MENU_DEPLOYMENTS_SHOW_ALL = "management.menu.deployments.show.all";
  String MGMT_MENU_DEPLOYMENTS_UPLOAD = "management.menu.deployments.upload";
  String MGMT_MENU_USERS = "management.menu.users";
  String MGMT_MENU_GROUPS = "management.menu.groups";
  String MGMT_MENU_ADMINISTRATION = "management.menu.admin";
  String MGMT_MENU_CRYSTALBALL = "management.menu.crystalball";
  
  // Job page
  String JOB_EXECUTE = "job.execute";
  String JOB_DELETE = "job.delete";
  String JOB_DELETED = "job.deleted";
  String JOB_HEADER_EXECUTION = "job.header.execution";
  String JOB_RETRIES = "job.retries";
  String JOB_NO_RETRIES = "job.no.retries";
  String JOB_DEFAULT_NAME = "job.default.name";
  String JOB_TIMER = "job.timer";
  String JOB_MESSAGE = "job.message";
  String JOB_DUEDATE = "job.duedate";
  String JOB_NO_DUEDATE = "job.no.dudedate";
  String JOB_ERROR = "job.error";
  String JOB_NOT_EXECUTED = "job.not.executed";
  String JOB_SUSPEND_PROCESSDEFINITION = "job.suspend.processdefinition";
  String JOB_ACTIVATE_PROCESSDEFINITION = "job.activate.processdefinition";
  
  // Deployment page
  String DEPLOYMENT_DELETE = "deployment.delete";
  String DEPLOYMENT_CREATE_TIME = "deployment.create.time";
  String DEPLOYMENT_HEADER_DEFINITIONS = "deployment.header.definitions";
  String DEPLOYMENT_HEADER_RESOURCES = "deployment.header.resources";
  String DEPLOYMENT_UPLOAD = "deployment.upload";
  String DEPLOYMENT_UPLOAD_DESCRIPTION = "deployment.upload.description";
  String DEPLOYMENT_UPLOAD_FAILED = "deployment.upload.failed";
  String DEPLOYMENT_UPLOAD_INVALID_FILE = "deployment.upload.invalid.file";
  String DEPLOYMENT_UPLOAD_INVALID_FILE_EXPLANATION = "deployment.upload.invalid.file.explanation";
  String DEPLOYMENT_UPLOAD_SERVER_ERROR = "deployment.upload.server.error";
  String DEPLOYMENT_DEPLOY_TIME = "deployment.deploy.time";
  String DEPLOYMENT_NO_NAME = "deployment.no.name";
  String DEPLOYMENT_NO_INSTANCES = "deployment.no.instances";
  String DEPLOYMENT_DELETE_POPUP_CAPTION = "deployment.delete.popup.caption";
  String DEPLOYMENT_DELETE_POPUP_WARNING = "deployment.delete.popup.warning";
  String DEPLOYMENT_DELETE_POPUP_DELETE_BUTTON = "deployment.delete.popup.delete.button";
  
  // Import to model workspace
  String MODEL_IMPORT = "model.import";
  String MODEL_IMPORT_DESCRIPTION = "model.import.description";
  String MODEL_IMPORT_FAILED = "model.import.failed";
  String MODEL_IMPORT_INVALID_FILE = "model.import.invalid.file";
  String MODEL_IMPORT_INVALID_FILE_EXPLANATION = "model.import.invalid.file.explanation";
  String MODEL_IMPORT_INVALID_BPMNDI = "model.import.invalid.bpmndi";
  String MODEL_IMPORT_INVALID_BPMNDI_EXPLANATION = "model.import.invalid.bpmndi.explanation";
  String MODEL_IMPORT_INVALID_BPMN_EXPLANATION = "model.import.invalid.bpmn.explanation";
  
  String MODEL_ACTION = "model.action";
  
  String MODEL_DEPLOY_POPUP_CAPTION = "model.deploy.popup.caption";
  String MODEL_DEPLOY_NAME = "model.deploy.name";
  String MODEL_DEPLOY_GENERATE_REPORTS = "model.deploy.generate.reports";
  String MODEL_DEPLOY_BUTTON_DEPLOY = "model.deploy.button.deploy";
  
  // Database page
  String DATABASE_NO_ROWS = "database.no.rows";
  
  // User page
  String USER_HEADER_DETAILS = "user.header.details";
  String USER_HEADER_GROUPS = "user.header.groups";
  String USER_ID = "user.id";
  String USER_ID_REQUIRED = "user.id.required";
  String USER_ID_UNIQUE = "user.id.unique";
  String USER_FIRSTNAME = "user.firstname";
  String USER_LASTNAME = "user.lastname";
  String USER_EMAIL = "user.email";
  String USER_PASSWORD = "user.password";
  String USER_PASSWORD_REQUIRED = "user.password.required";
  String USER_PASSWORD_MIN_LENGTH = "user.password.min.lenth";
  String USER_RESET_PASSWORD = "user.reset.password";
  String USER_CREATE = "user.create";
  String USER_EDIT = "user.edit";
  String USER_DELETE = "user.delete";
  String USER_SAVE = "user.save";
  String USER_NO_PICTURE = "user.no.picture";
  String USER_NO_GROUPS = "user.no.groups";
  String USER_CONFIRM_DELETE = "user.confirm.delete";
  String USER_CONFIRM_DELETE_GROUP = "user.confirm.delete.group";
  String USER_SELECT_GROUPS = "user.select.groups";
  String USER_SELECT_GROUPS_POPUP = "user.select.groups.popup";
  
  // Group page
  String GROUP_HEADER_DETAILS = "group.header.details";
  String GROUP_HEADER_USERS = "group.header.users";
  String GROUP_CREATE = "group.create";
  String GROUP_ID = "group.id";
  String GROUP_NAME = "group.name";
  String GROUP_TYPE = "group.type";
  String GROUP_CONFIRM_DELETE = "group.confirm.delete";
  String GROUP_ID_REQUIRED = "group.id.required";
  String GROUP_ID_UNIQUE = "group.id.unique";
  String GROUP_NO_MEMBERS = "group.no.members";
  String GROUP_SELECT_MEMBERS = "group.select.members";
  String GROUP_DELETE = "group.delete";
  
  // Running process instances page
  String ADMIN_MENU_RUNNING = "admin.menu.running";
  String ADMIN_MENU_COMPLETED = "admin.menu.completed";
  String ADMIN_MENU_DATABASE = "admin.menu.database";
  String ADMIN_RUNNING_TITLE = "admin.running.title";
  String ADMIN_RUNNING_NONE_FOUND = "admin.running.none.found";
  String ADMIN_COMPLETED_TITLE = "admin.completed.title";
  String ADMIN_COMPLETED_NONE_FOUND = "admin.completed.none.found";
  String ADMIN_DEFINITIONS = "admin.definitions";
  String ADMIN_NR_INSTANCES = "admin.nr.instances";
  String ADMIN_STARTED_BY = "admin.started.by";
  String ADMIN_START_ACTIVITY = "admin.start.activity";
  String ADMIN_FINISHED = "admin.finished";
  
  // Database settings page
  String DATABASE_TITLE = "database.title";
  String DATABASE_TYPE = "database.type";
  String DATABASE_UPDATE = "database.update";
  String DATABASE_CONFIG_TYPE = "database.config.type";
  String DATABASE_JNDI = "database.jndi";
  String DATABASE_DATASOURCE_CLASS = "database.datasource.class";
  String DATABASE_DATASOURCE_URL = "database.datasource.url";
  String DATABASE_JDBC_URL = "database.jdbc.url";
  
  // Upload
  String UPLOAD_SELECT = "upload.select";
  String UPLOAD_DROP = "upload.drop";
  String UPLOAD_FAILED = "upload.failed";
  String UPLOAD_LIMIT = "upload.limit";
  String UPLOAD_INVALID_MIMETYPE = "upload.invalid.mimetype";

  // Related content
  String RELATED_CONTENT_ADD = "related.content.add";
  String RELATED_CONTENT_NAME = "related.content.name";
  String RELATED_CONTENT_NAME_REQUIRED = "related.content.name.required";
  String RELATED_CONTENT_DESCRIPTION = "related.content.description";
  String RELATED_CONTENT_CREATE = "related.content.create";
  
  String RELATED_CONTENT_TYPE_URL = "related.content.type.url";
  String RELATED_CONTENT_TYPE_URL_URL = "related.content.type.url.url";
  String RELATED_CONTENT_TYPE_URL_URL_REQUIRED = "related.content.type.url.url.required";
  String RELATED_CONTENT_TYPE_URL_HELP = "related.content.type.url.help";
  
  String RELATED_CONTENT_TYPE_FILE = "related.content.type.file";
  String RELATED_CONTENT_TYPE_FILE_HELP = "related.content.type.file.help";
  String RELATED_CONTENT_TYPE_FILE_UPLOADED = "related.content.type.file.uploaded";
  String RELATED_CONTENT_TYPE_FILE_REQUIRED = "related.content.type.file.required";
  String RELATED_CONTENT_CONFIRM_DELETE = "related.content.confirm.delete";
  String RELATED_CONTENT_SHOW_FULL_SIZE = "related.content.show.full.size";
  
  String RELATED_CONTENT_TYPE_EMAIL = "related.content.type.email";
  
  // People involvement
  String PEOPLE_SEARCH = "people.search";
  String PEOPLE_INVOLVE_POPUP_CAPTION = "people.involve.popup.caption";
  String PEOPLE_SELECT_MYSELF = "people.select.myself";

  String TASK_AUTHORISATION_ERROR_TITLE = "task.authorisation.error.title";
  String TASK_AUTHORISATION_MEMBERSHIP_ERROR = "task.authorisation.membership.error";
  String TASK_AUTHORISATION_INBOX_ERROR = "task.authorisation.inbox.error";

  String EMAIL_SUBJECT = "email.subject";
  String EMAIL_SENT_DATE = "email.sent.date";
  String EMAIL_RECEIVED_DATE = "email.received.date";
  String EMAIL_HTML_CONTENT = "email.html.content";
  String EMAIL_RECIPIENTS = "email.recipients";
  
  // Crystalball
  String CRYSTALBALL_BUTTON_REPLAY = "crystalball.button.replay";
  String CRYSTALBALL_BUTTON_NEXTEVENT = "crystalball.button.nextevent";
  String CRYSTALBALL_EVENT_TYPE = "crystalball.event.type";
  String CRYSTALBALL_EVENT_EXECUTED = "crystalball.event.executed";

  // Time formatting
  String TIME_UNIT_MOMENTS = "time.unit.moments";
  String TIME_UNIT_PAST = "time.unit.past";
  String TIME_UNIT_FUTURE = "time.unit.future";
  
  String TIME_UNIT_MINUTE = "time.unit.minute";
  String TIME_UNIT_MINUTES = "time.unit.minutes";
  String TIME_UNIT_HOUR = "time.unit.hour";
  String TIME_UNIT_HOURS = "time.unit.hours";
  String TIME_UNIT_DAY = "time.unit.day";
  String TIME_UNIT_DAYS = "time.unit.days";
  String TIME_UNIT_WEEK = "time.unit.week";
  String TIME_UNIT_WEEKS = "time.unit.weeks";
  String TIME_UNIT_MONTH = "time.unit.month";
  String TIME_UNIT_MONTHS = "time.unit.months";
  String TIME_UNIT_YEAR = "time.unit.year";
  String TIME_UNIT_YEARS = "time.unit.years";
  String TIME_UNIT_JUST_NOW = "time.unit.just.now";
  
  String MONTH_PREFIX = "month.";

}
