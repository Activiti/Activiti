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

package org.activiti.explorer.ui;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;


/**
 * Contains statically created {@link Resource} instances for all the images.
 * 
 * @author Joram Barrez
 */
public class Images {
  
  // General
  public static final Resource USER = new ThemeResource("img/user-icon.png");
  public static final Resource WHITE_DIVIDER = new ThemeResource("img/divider-white.png");
  public static final Resource TASK = new ThemeResource("img/task.png");
  public static final Resource CLOCK = new ThemeResource("img/clock.png");
  public static final Resource PEOPLE = new ThemeResource("img/people.png");
  public static final Resource PROCESS = new ThemeResource("img/process.png");
  public static final Resource PROCESS_48PX = new ThemeResource("img/process_48.png");
  public static final Resource RESOURCE = new ThemeResource("img/resource.png");
  public static final Resource WARNING = new ThemeResource("img/warning.png");
  public static final Resource DELETE = new ThemeResource("img/delete.png");
  public static final Resource EXECUTE = new ThemeResource("img/execute.png");
  public static final Resource SUCCESS = new ThemeResource("img/tick.png");
  
  // Task
  public static final Resource TASK_FINISHED = new ThemeResource("img/tick.png");
  public static final Resource TASK_UNFINISHED = new ThemeResource("img/control_play.png");
  
  // Accounts
  public static final Resource SKYPE = new ThemeResource("img/skype.png");
  public static final Resource GOOGLE = new ThemeResource("img/google.png");
  public static final Resource ALFRESCO = new ThemeResource("img/alfresco.gif");
  
  // Database
  public static final Resource DATABASE_GENERAL = new ThemeResource("img/database_general.png");
  public static final Resource DATABASE_HISTORY = new ThemeResource("img/database_history.png");
  public static final Resource DATABASE_IDENTITY = new ThemeResource("img/database_identity.png");
  public static final Resource DATABASE_REPOSITORY = new ThemeResource("img/database_repository.png");
  public static final Resource DATABASE_RUNTIME = new ThemeResource("img/database_runtime.png");
  
  // Related content
  public static final Resource ADD_RELATED_CONTENT = new ThemeResource("img/list-add.png");
  public static final Resource RELATED_CONTENT_URL = new ThemeResource("img/page_white_world.png");
  public static final Resource RELATED_CONTENT_FILE = new ThemeResource("img/page_white_get.png");
  public static final Resource RELATED_CONTENT_PDF = new ThemeResource("img/page_white_acrobat.png");
  public static final Resource RELATED_CONTENT_PICTURE = new ThemeResource("img/picture.png");;

}
