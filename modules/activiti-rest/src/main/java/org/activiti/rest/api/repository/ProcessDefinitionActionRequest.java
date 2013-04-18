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

package org.activiti.rest.api.repository;

import java.util.Date;

import org.activiti.rest.api.RestActionRequest;


/**
 * @author Frederik Heremans
 */
public class ProcessDefinitionActionRequest extends RestActionRequest {

  private boolean includeProcessInstances = false;
  private Date date;
  
  public void setIncludeProcessInstances(boolean includeProcessInstances) {
    this.includeProcessInstances = includeProcessInstances;
  }
  public boolean isIncludeProcessInstances() {
    return includeProcessInstances;
  }
  public void setDate(Date date) {
    this.date = date;
  }
  public Date getDate() {
    return date;
  }
}
