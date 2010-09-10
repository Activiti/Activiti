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

import junit.framework.Assert;

import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.management.TableMetaData;


/**
 * @author Frederik Heremans
 */
public class ManagementServiceTest extends ActivitiInternalTestCase {

  public void testGetMetaDataForUnexistingTable() {
    
      TableMetaData metaData = managementService.getTableMetaData("unexistingtable");
      Assert.assertNotNull(metaData);
      Assert.assertTrue(metaData.getColumnNames().isEmpty());
      Assert.assertTrue(metaData.getColumnTypes().isEmpty());
  }
}
