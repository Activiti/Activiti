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
package org.activiti.examples.mgmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.task.Task;


/**
 * @author Joram Barrez
 */
public class TablePageQueryTest extends PluggableActivitiTestCase {
  
  public void testGetTablePage() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    List<String> taskIds = generateDummyTasks(20);
    
    TablePage tablePage = managementService.createTablePageQuery()
      .tableName(tablePrefix+"ACT_RU_TASK")
      .listPage(0, 5);
    
    assertEquals(0, tablePage.getFirstResult());
    assertEquals(5, tablePage.getSize());
    assertEquals(5, tablePage.getRows().size());
    assertEquals(20, tablePage.getTotal());
    
    tablePage = managementService.createTablePageQuery()
      .tableName(tablePrefix+"ACT_RU_TASK")
      .listPage(14, 10);
    
    assertEquals(14, tablePage.getFirstResult());
    assertEquals(6, tablePage.getSize());
    assertEquals(6, tablePage.getRows().size());
    assertEquals(20, tablePage.getTotal());

    taskService.deleteTasks(taskIds, true);
  }
  
  public void testGetSortedTablePage() {
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    List<String> taskIds = generateDummyTasks(15);
    
    // With an ascending sort
    TablePage tablePage = managementService.createTablePageQuery()
      .tableName(tablePrefix+"ACT_RU_TASK")
      .orderAsc("NAME_")
      .listPage(1, 7);
    String[] expectedTaskNames = new String[] {"B", "C", "D", "E", "F", "G", "H"};
    verifyTaskNames(expectedTaskNames, tablePage.getRows());
    
    // With a descending sort
    tablePage = managementService.createTablePageQuery()
      .tableName(tablePrefix+"ACT_RU_TASK")
      .orderDesc("NAME_")
      .listPage(6, 8);
    expectedTaskNames = new String[] {"I", "H", "G", "F", "E", "D", "C", "B"} ;
    verifyTaskNames(expectedTaskNames, tablePage.getRows());
    
    taskService.deleteTasks(taskIds, true);
  }
  
  private void verifyTaskNames(String[] expectedTaskNames, List<Map<String, Object>> rowData) {
    assertEquals(expectedTaskNames.length, rowData.size());
    String columnKey = "NAME_";

    // mybatis will return the correct case for postgres table columns from version 3.0.6 on
    if (processEngineConfiguration.getDatabaseType().equals("postgres")) {
      columnKey = "name_";
    }
    
    for (int i=0; i < expectedTaskNames.length; i++) {
      assertEquals(expectedTaskNames[i], rowData.get(i).get(columnKey));
    }
  }
  
  private List<String> generateDummyTasks(int nrOfTasks) {
    ArrayList<String> taskIds = new ArrayList<String>();
    for (int i = 0; i < nrOfTasks; i++) {
      Task task = taskService.newTask();
      task.setName(((char)('A' + i)) + "");
      taskService.saveTask(task);
      taskIds.add(task.getId());
    }
    return taskIds;
  } 

}
