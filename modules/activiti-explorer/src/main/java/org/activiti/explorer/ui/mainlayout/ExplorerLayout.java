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

package org.activiti.explorer.ui.mainlayout;


/**
 * @author Joram Barrez
 */
public interface ExplorerLayout {
  
  // Application theme
  static final String THEME = "activiti";
  
  // Custom layouts (found in /VAADIN/themes/${THEME}/layouts
  static final String CUSTOM_LAYOUT_LOGIN = "login";
  
  // Locations defined in the layout .html files
  static final String LOCATION_LOGIN = "login-content";
  static final String LOCATION_CONTENT = "content";
  static final String LOCATION_SEARCH = "search";
  static final String LOCATION_LOGOUT = "logout";
  static final String LOCATION_MAIN_MENU = "main-menu";
  static final String LOCATION_HIDDEN = "hidden";
  static final String STYLE_IMAGE_ACTION = "image-action";
  
  static final String LINK_TARGET_BLANK = "_blank";

  // ---------- 
  // Css styles
  // ----------
  
  // Skeleton
  static final String STYLE_MAIN_WRAPPER = "main";
  static final String STYLE_HEADER = "header";
  static final String STYLE_MAIN_CONTENT = "main-content";
  static final String STYLE_MAIN_FOOTER = "footer";

  // General
  static final String STYLE_SMALL_TEXTFIELD = "small";
  static final String STYLE_SEARCHBOX = "searchbox";
  static final String STYLE_SEARCHBOX_LEFT = "searchbox-left";
  static final String STYLE_SEARCHBOX_RIGHT = "searchbox-right";
  static final String STYLE_SEARCHBOX_SORTMENU = "searchbox-sortmenu";
  static final String STYLE_HEADER_PROFILE_BOX = "person";
  static final String STYLE_HEADER_PROFILE_MENU = "person-menu";
  static final String STYLE_USER_PROFILE = "user";
  static final String STYLE_LABEL_BOLD = "bold";
  static final String STYLE_LABEL_RED = "red";
  static final String STYLE_CLICKABLE = "clickable";
  static final String STYLE_ACTIVE = "active";
  static final String STYLE_TOOLBAR = "toolbar";
  static final String STYLE_TOOLBAR_BUTTON = "toolbar-button";
  static final String STYLE_TOOLBAR_COUNT = "toolbar-count";
  static final String STYLE_TOOLBAR_POPUP = "toolbar-popup";
  static final String STYLE_SCROLLABLE = "scrollable";
  static final String STYLE_TITLE_BLOCK = "title-block";
  static final String STYLE_DETAIL_BLOCK = "block-holder";
  static final String STYLE_DETAIL_PANEL = "detail-panel";
  static final String STYLE_H3 = "h3";
  static final String STYLE_H4 = "h4";
  static final String STYLE_ADD = "add";
  static final String STYLE_NO_LINE = "no-line";
  static final String STYLE_TEXTAREA_NO_RESIZE = "noResizeTextArea";
  static final String STYLE_ERROR = "error";
  
  static final String STYLE_APPLICATION_LOGO = "logo";
  static final String STYLE_WORKFLOW_CONSOLE_LOGO = "workflow-console-logo";
  static final String STYLE_MAIN_MENU_BUTTON = "main-menu-button";
  
  //Forms
  static final String STYLE_FORM_PROPERTIES = "formprops";
  static final String STYLE_FORM_USER_SELECTED = "formprop-user-selected";
  static final String STYLE_FORM_NO_USER_SELECTED = "formprop-no-user-selected";
  
  // Login page
  static final String STYLE_LOGIN_PAGE = "login-general";
  
  // Profile page
  static final String STYLE_PROFILE_LAYOUT = "profile-layout";
  static final String STYLE_PROFILE_FIELD = "profile-field";
  static final String STYLE_PROFILE_PICTURE = "profile-picture";
  static final String STYLE_PROFILE_LINK = "profile-link";
  
  // Task pages
  static final String STYLE_TASK_LIST = "task-list";
  static final String STYLE_TASK_EVENT_PANEL = "task-event-panel";
  static final String STYLE_TASK_EVENT = "task-event";
  static final String STYLE_TASK_EVENT_AUTHOR = "task-event-author";
  static final String STYLE_TASK_EVENT_TIME = "task-event-time";
  static final String STYLE_TASK_EVENT_PICTURE = "task-event-picture";
  static final String STYLE_TASK_EVENT_GRID = "event-grid";
  static final String STYLE_TASK_HEADER_DUEDATE = "task-duedate";
  static final String STYLE_TASK_HEADER_PRIORITY_LOW = "task-priority-low";
  static final String STYLE_TASK_HEADER_PRIORITY_MEDIUM = "task-priority-medium";
  static final String STYLE_TASK_HEADER_PRIORITY_HIGH = "task-priority-high";
  static final String STYLE_TASK_HEADER_CREATE_TIME = "task-create-time";
  static final String STYLE_INVOLVE_PEOPLE = "involve-people";
  static final String STYLE_TASK_SUBTASKS_LIST = "subtasks";

  // Process pages
  static final String STYLE_PROCESS_DEFINITION_LIST = "proc-def-list";
  static final String STYLE_PROCESS_INSTANCE_LIST = "proc-inst-list";
  static final String STYLE_PROCESS_INSTANCE_TASK_LIST = "proc-inst-task-list";
  static final String STYLE_PROCESS_HEADER_DEPLOY_TIME = "process-deploy-time";
  static final String STYLE_PROCESS_HEADER_VERSION = "process-version";
  static final String STYLE_PROCESS_HEADER_START_TIME = "process-start-time";
  static final String STYLE_PROCESS_EDITOR_CHOICE = "process-definition-editor-choice";
  
  // Database page
  static final String STYLE_DATABASE_TABLE = "database-table";
  
  
  // Deployment page
  static final String STYLE_DEPLOYMENT_UPLOAD_DESCRIPTION = "upload-description";
  static final String STYLE_DEPLOYMENT_UPLOAD_BUTTON = "upload-button";
  static final String STYLE_DEPLOYMENT_HEADER_DEPLOY_TIME = "deployment-deploy-time";
  
  // Jobs page
  static final String STYLE_JOB_DETAILS_HEADER = "job-details-header";
  static final String STYLE_JOB_EXCEPTION_MESSAGE = "job-exception-message";
  static final String STYLE_JOB_EXCEPTION_TRACE = "job-exception-trace";
  static final String STYLE_JOB_HEADER_DUE_DATE = "job-duedate";
  
  // Process definitions page
  
  static final String STYLE_PROCESS_DEFINITION_SUSPEND_CHOICE = "process-definition-suspend-choice";

  // Related content
  static final String STYLE_RELATED_CONTENT_LIST = "related-content-list";
  static final String STYLE_RELATED_CONTENT_CREATE_LIST = "related-content-create-list";
  static final String STYLE_RELATED_CONTENT_CREATE_DETAIL = "related-content-create-detail";
  static final String STYLE_RELATED_CONTENT_CREATE_LIST_LAST_COLUMN = "related-last-column";

}
