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

package org.activiti.explorer.ui.task;

import org.activiti.engine.task.Task;
import org.activiti.explorer.Constants;
import org.activiti.explorer.ui.ViewManager;

import com.vaadin.event.LayoutEvents;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;


/**
 * @author Joram Barrez
 */
public class TaskListEntry extends CustomComponent {
  
  private static final long serialVersionUID = -7940890991976505800L;

  protected ViewManager viewManager;
  protected Task task;
  
  public TaskListEntry(final ViewManager viewManager, final Table taskTable, final int itemId, final Task task) {
    this.task = task;
    
    GridLayout grid = new GridLayout(2, 1);
    setCompositionRoot(grid);
    
    Embedded taskImage = new Embedded(null, viewManager.getClassResource(Constants.IMAGE_TASK));
    taskImage.setType(Embedded.TYPE_IMAGE);
    grid.addComponent(taskImage);
    
    Label taskNameLabel = new Label(task.getName());
    grid.addComponent(taskNameLabel);
    
    // Required such that click on component is propagated to parent (bug in Vaadin?)
    grid.addListener(new LayoutEvents.LayoutClickListener() {
      private static final long serialVersionUID = 3178252539054165461L;
      public void layoutClick(LayoutClickEvent event) {
        taskTable.select(itemId);
      }
    });
    
  }
  
  public TaskListEntry getComponent() {
    return this;
  }
  
  public Task getTask() {
    return task;
  }
  
}
