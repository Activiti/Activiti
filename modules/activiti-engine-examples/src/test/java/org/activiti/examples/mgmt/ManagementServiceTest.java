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
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.activiti.ManagementService;
import org.activiti.TableMetaData;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test case for the various operations of the {@link ManagementService}
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ManagementServiceTest extends ActivitiTestCase {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  public void testTableCount() {
    Map<String, Long> tableCount = deployer.getManagementService().getTableCount();

    assertEquals(new Long(2), tableCount.get("ACT_PROPERTY"));
    assertEquals(new Long(0), tableCount.get("ACT_BYTEARRAY"));
    assertEquals(new Long(0), tableCount.get("ACT_DEPLOYMENT"));
    assertEquals(new Long(0), tableCount.get("ACT_EXECUTION"));
    assertEquals(new Long(0), tableCount.get("ACT_ID_GROUP"));
    assertEquals(new Long(0), tableCount.get("ACT_ID_MEMBERSHIP"));
    assertEquals(new Long(0), tableCount.get("ACT_ID_USER"));
    assertEquals(new Long(0), tableCount.get("ACT_PROCESSDEFINITION"));
    assertEquals(new Long(0), tableCount.get("ACT_TASK"));
    assertEquals(new Long(0), tableCount.get("ACT_TASKINVOLVEMENT"));
    ;
  }

  @Test
  public void testGetTableMetaData() {
    TableMetaData tableMetaData = deployer.getManagementService().getTableMetaData("ACT_TASK");
    assertEquals(tableMetaData.getColumnNames().size(), tableMetaData.getColumnTypes().size());
    assertEquals(13, tableMetaData.getColumnNames().size());

    int assigneeIndex = tableMetaData.getColumnNames().indexOf("ASSIGNEE_");
    int createTimeIndex = tableMetaData.getColumnNames().indexOf("CREATE_TIME_");

    assertTrue(assigneeIndex >= 0);
    assertTrue(createTimeIndex >= 0);

    assertEquals("VARCHAR", tableMetaData.getColumnTypes().get(assigneeIndex));
    assertEquals("TIMESTAMP", tableMetaData.getColumnTypes().get(createTimeIndex));
  }

}
