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

package org.activiti.rest.service.api.history;

import org.activiti.rest.common.api.DataResponse;
import org.restlet.resource.Post;



/**
 * @author Tijs Rademakers
 */
public class HistoricVariableInstanceQueryResource extends HistoricVariableInstanceBaseResource {

  @Post
  public DataResponse queryVariableInstances(HistoricVariableInstanceQueryRequest queryRequest) {
  	if(authenticate() == false) return null;
    return getQueryResponse(queryRequest, getQuery());
  }
}
