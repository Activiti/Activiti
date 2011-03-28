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
  static final String TASK_CREATED = "task.created";
  static final String TASK_DUEDATE = "task.duedate";
  static final String TASK_COMPLETE = "task.complete";
  static final String TASK_COMPLETED = "task.task.completed";
  static final String TASK_RESET_FORM = "task.form.reset";
  static final String TASK_ADD_COMMENT = "task.comment.add";
  static final String TASK_COMMENT_POPUP_HEADER = "task.comment.popup.header";
  
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
  static final String FLOW_STARTED_NOTIFICATIOn = "flow.started.notification";
  
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
  
  // Database page
  static final String DATABASE_NO_ROWS = "database.no.rows";
  
  // Upload
  static final String UPLOAD_SELECT = "upload.select";
  static final String UPLOAD_DROP = "upload.drop";
  
}
