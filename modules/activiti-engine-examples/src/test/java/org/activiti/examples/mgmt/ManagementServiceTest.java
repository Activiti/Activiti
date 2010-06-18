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
import java.util.logging.Logger;

import org.activiti.Task;
import org.activiti.mgmt.TablePage;
import org.activiti.test.ActivitiTestCase;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ManagementServiceTest extends ActivitiTestCase {

  private static Logger log = Logger.getLogger(ManagementServiceTest.class.getName());
  
  public void testTableCount() {
    Map<String, Long> tableCount = managementService.getTableCount();

    assertEquals(new Long(2), tableCount.get("ACT_PROPERTY"));
    assertEquals(new Long(0), tableCount.get("ACT_BYTEARRAY"));
    assertEquals(new Long(0), tableCount.get("ACT_DEPLOYMENT"));
    assertEquals(new Long(0), tableCount.get("ACT_EXECUTION"));
    assertEquals(new Long(0), tableCount.get("ACT_ID_GROUP"));
    assertEquals(new Long(0), tableCount.get("ACT_ID_MEMBERSHIP"));
    assertEquals(new Long(0), tableCount.get("ACT_ID_USER"));
    assertEquals(new Long(0), tableCount.get("ACT_PROCESSDEFINITION"));
    assertEquals(new Long(0), tableCount.get("ACT_TASK"));
    assertEquals(new Long(0), tableCount.get("ACT_TASKINVOLVEMENT"));;
  }
  
  public void testGetTablePage() {
    List<String> taskIds = generateDummyTasks(20);
    
    TablePage tablePage = managementService.getTablePage("ACT_TASK", 0, 5);
    assertEquals(0, tablePage.getOffset());
    assertEquals(5, tablePage.getNoOfResults());
    assertEquals(5, tablePage.getRows().size());
    assertEquals(tablePage.getColumnNames().size(), tablePage.getColumnTypes().size());
    assertTrue(tablePage.getColumnNames().contains("ID_"));
    assertTrue(tablePage.getColumnNames().contains("PRIORITY_"));
    
    tablePage = managementService.getTablePage("ACT_TASK", 14, 10);
    assertEquals(14, tablePage.getOffset());
    assertEquals(6, tablePage.getNoOfResults());
    assertEquals(6, tablePage.getRows().size());
    
    deleteTasks(taskIds);
  }
  
  private List<String> generateDummyTasks(int nrOfTasks) {
    ArrayList<String> taskIds = new ArrayList<String>();
    for (int i = 0; i < nrOfTasks; i++) {
      log.info("=== new task =============================================================");
      Task task = taskService.newTask();
      task.setName(String.valueOf(i));

      log.info("=== saving task =============================================================");
      taskService.saveTask(task);
      taskIds.add(task.getId());
    }
    return taskIds;
  }
  
  private void deleteTasks(List<String> taskIds) {
    for (String taskId : taskIds) {
      taskService.deleteTask(taskId);
    }
  }
  
}
