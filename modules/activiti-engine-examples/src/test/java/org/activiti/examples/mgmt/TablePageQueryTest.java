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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.TablePage;
import org.activiti.Task;
import org.activiti.test.ActivitiTestCase;
import org.junit.Test;


/**
 * @author Joram Barrez
 */
public class TablePageQueryTest extends ActivitiTestCase {
  
  @Test
  public void testGetTablePage() {
    List<String> taskIds = generateDummyTasks(20);
    
    TablePage tablePage = managementService.createTablePageQuery()
      .tableName("ACT_TASK")
      .start(0)
      .size(5)
      .singleResult();
    
    assertEquals(0, tablePage.getStart());
    assertEquals(5, tablePage.getSize());
    assertEquals(5, tablePage.getRows().size());
    assertEquals(20, tablePage.getTotal());
    
    tablePage = managementService.createTablePageQuery()
      .tableName("ACT_TASK")
      .start(14)
      .size(10)
      .singleResult();
    
    assertEquals(14, tablePage.getStart());
    assertEquals(6, tablePage.getSize());
    assertEquals(6, tablePage.getRows().size());
    assertEquals(20, tablePage.getTotal());
    
    deleteTasks(taskIds);
  }
  
  @Test 
  public void testGetSortedTablePage() {
    List<String> taskIds = generateDummyTasks(15);
    
    // Without a sort
    TablePage tablePage = managementService.createTablePageQuery()
      .tableName("ACT_TASK")
      .start(1)
      .size(7)
      .singleResult();
    String[] expectedTaskNames = new String[] {"1", "2", "3", "4", "5", "6", "7"};
    verifyTaskNames(expectedTaskNames, tablePage.getRows());
    
    // With an ascending sort
    tablePage = managementService.createTablePageQuery()
      .tableName("ACT_TASK")
      .start(1)
      .size(7)
      .orderAsc("NAME_")
      .singleResult();
    expectedTaskNames = new String[] {"1", "10", "11", "12", "13", "14", "2"} ;
    verifyTaskNames(expectedTaskNames, tablePage.getRows());
    
    // With a descending sort
    tablePage = managementService.createTablePageQuery()
      .tableName("ACT_TASK")
      .start(6)
      .size(8)
      .orderDesc("NAME_")
      .singleResult();
    expectedTaskNames = new String[] {"3", "2", "14", "13", "12", "11", "10", "1"} ;
    verifyTaskNames(expectedTaskNames, tablePage.getRows());
    
    deleteTasks(taskIds);
  }
  
  private void verifyTaskNames(String[] expectedTaskNames, List<Map<String, Object>> rowData) {
    assertEquals(expectedTaskNames.length, rowData.size());
    for (int i=0; i < expectedTaskNames.length; i++) {
      assertEquals(expectedTaskNames[i], rowData.get(i).get("NAME_"));
    }
  }
  
  private List<String> generateDummyTasks(int nrOfTasks) {
    ArrayList<String> taskIds = new ArrayList<String>();
    for (int i = 0; i < nrOfTasks; i++) {
      Task task = taskService.newTask();
      task.setName(String.valueOf(i));
      taskService.saveTask(task);
      taskIds.add(task.getId());
    }
    return taskIds;
  }  

}
