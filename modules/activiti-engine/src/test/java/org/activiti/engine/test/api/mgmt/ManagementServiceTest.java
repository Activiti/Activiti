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

package org.activiti.engine.test.api.mgmt;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.management.TableMetaData;


/**
 * @author Frederik Heremans
 */
public class ManagementServiceTest extends ActivitiInternalTestCase {

  public void testGetMetaDataForUnexistingTable() {
    TableMetaData metaData = managementService.getTableMetaData("unexistingtable");
    assertNull(metaData);
  }
  
  public void testGetMetaDataNullTableName() {
    try {
      managementService.getTableMetaData(null);
      fail("Exception expected");
    } catch (ActivitiException re) {
      assertTextPresent("tableName is null", re.getMessage());
    }
  }
  
  public void testExecuteJobNullJobId() {
    try {
      managementService.executeJob(null);
      fail("Exception expected");
    } catch (ActivitiException re) {
      assertTextPresent("jobId is null", re.getMessage());
    }
  }
  
  public void testExecuteJobUnexistingJob() {
    try {
      managementService.executeJob("unexistingjob");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("no job found with id", ae.getMessage());
    }
  }
}
