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

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * @author Joram Barrez
 */
public class Constants {
  
  public static final String THEME = "activiti";
  public static final String LOGIN_LAYOUT = "login";
  
  // Locations in the general layout
  public static final String LOCATION_LOGIN = "login-content";
  public static final String LOCATION_CONTENT = "content";
  public static final String LOCATION_SEARCH = "search";
  public static final String LOCATION_MAIN_MENU = "main-menu";
  public static final String LOCATION_HIDDEN = "hidden";

  // Css styles
  public static final String STYLE_LOGIN_PAGE = "login-general";
  
  public static final String STYLE_SMALL_TEXTFIELD = "small";
  public static final String STYLE_SEARCHBOX = "searchBox";
  public static final String STYLE_LOGOUT_BUTTON = "logout";
  public static final String STYLE_USER_LABEL = "user";
  
  public static final String STYLE_MENUBAR = "menubar";
  public static final String STYLE_MENUBAR_BUTTON = "menu-button";
  
  public static final String STYLE_ACTION_BAR = "action-bar";
  
  public static final String STYLE_PROFILE_LAYOUT = "profile-layout";
  public static final String STYLE_PROFILE_HEADER = "profile-header";
  public static final String STYLE_PROFILE_FIELD = "profile-field";
  public static final String STYLE_PROFILE_PICTURE = "profile-picture";
  
  public static final String STYLE_LABEL_BOLD = "bold";
  
  public static final String STYLE_TASK_LIST = "task-list";
  public static final String STYLE_TASK_DETAILS = "task-details";
  public static final String STYLE_TASK_DETAILS_HEADER = "task-details-header";
  
  public static final String STYLE_TASK_COMMENT = "task-comment";
  public static final String STYLE_TASK_COMMENT_AUTHOR = "task-comment-author";
  public static final String STYLE_TASK_COMMENT_TIME = "task-comment-time";
  public static final String STYLE_TASK_COMMENT_PICTURE = "task-comment-picture";

  public static final String STYLE_PROCESS_DEFINITION_LIST = "proc-def-list";
  public static final String STYLE_PROCESS_DEFINITION_DETAILS_HEADER = "proc-def-details-header";
  
  public static final String STYLE_DATABASE_DETAILS = "database-details";
  public static final String STYLE_DATABASE_TABLE_ROW = "database-table-row";
  
  public static final String STYLE_DEPLOYMENT_DETAILS_HEADER = "deployment-details-header";
  public static final String STYLE_DEPLOYMENT_UPLOAD_DESCRIPTION = "upload-description";
  public static final String STYLE_DEPLOYMENT_UPLOAD_BUTTON = "upload-button";
  
  public static final String STYLE_JOB_DETAILS_HEADER = "job-details-header";
  public static final String STYLE_JOB_EXCEPTION_MESSAGE = "job-exception-message";
  public static final String STYLE_JOB_EXCEPTION_TRACE = "job-exception-trace";
  
  public static final String STYLE_FORMPROPERTY_READONLY = "formprop-readonly";
  public static final String STYLE_FORMPROPERTY_LABEL = "formprop-label";
  
  // Date formatting
  public static final DateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("dd MMM yyyy - hh:mm");
  
  // Default diagram image extension, when name cannot be deducted from resource name
  public static final String DEFAULT_DIAGRAM_IMAGE_EXTENSION = "png";


}
