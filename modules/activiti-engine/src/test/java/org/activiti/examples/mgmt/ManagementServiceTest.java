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

import java.util.Arrays;
import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.management.TableMetaData;

/**
 * Test case for the various operations of the {@link ManagementService}
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ManagementServiceTest extends PluggableActivitiTestCase {

  public void testTableCount() {
    Map<String, Long> tableCount = managementService.getTableCount();
    
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    
    assertEquals(new Long(3), tableCount.get(tablePrefix+"ACT_GE_PROPERTY"));
    assertEquals(new Long(0), tableCount.get(tablePrefix+"ACT_GE_BYTEARRAY"));
    assertEquals(new Long(0), tableCount.get(tablePrefix+"ACT_RE_DEPLOYMENT"));
    assertEquals(new Long(0), tableCount.get(tablePrefix+"ACT_RU_EXECUTION"));
    assertEquals(new Long(0), tableCount.get(tablePrefix+"ACT_ID_GROUP"));
    assertEquals(new Long(0), tableCount.get(tablePrefix+"ACT_ID_MEMBERSHIP"));
    assertEquals(new Long(0), tableCount.get(tablePrefix+"ACT_ID_USER"));
    assertEquals(new Long(0), tableCount.get(tablePrefix+"ACT_RE_PROCDEF"));
    assertEquals(new Long(0), tableCount.get(tablePrefix+"ACT_RU_TASK"));
    assertEquals(new Long(0), tableCount.get(tablePrefix+"ACT_RU_IDENTITYLINK"));
  }

  public void testGetTableMetaData() {
    
    String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
    
    TableMetaData tableMetaData = managementService.getTableMetaData(tablePrefix+"ACT_RU_TASK");
    assertEquals(tableMetaData.getColumnNames().size(), tableMetaData.getColumnTypes().size());
    assertEquals(19, tableMetaData.getColumnNames().size());

    int assigneeIndex = tableMetaData.getColumnNames().indexOf("ASSIGNEE_");
    int createTimeIndex = tableMetaData.getColumnNames().indexOf("CREATE_TIME_");

    assertTrue(assigneeIndex >= 0);
    assertTrue(createTimeIndex >= 0);
    
    assertOneOf(new String [] {"VARCHAR", "NVARCHAR2", "nvarchar", "NVARCHAR"}, tableMetaData.getColumnTypes().get(assigneeIndex));
    assertOneOf(new String [] {"TIMESTAMP", "TIMESTAMP(6)", "datetime", "DATETIME"}, tableMetaData.getColumnTypes().get(createTimeIndex));
  }
  
  private void assertOneOf(String[] possibleValues, String currentValue) {
    for(String value : possibleValues) {
      if(currentValue.equals(value)) {
        return;
      }
    }
    fail("Value '" + currentValue + "' should be one of: " + Arrays.deepToString(possibleValues));
  }

}
