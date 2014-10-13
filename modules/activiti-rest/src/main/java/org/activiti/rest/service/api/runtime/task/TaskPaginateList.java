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

package org.activiti.rest.service.api.runtime.task;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.AbstractPaginateList;
import org.activiti.rest.service.api.RestResponseFactory;

/**
 * @author Frederik Heremans
 */
public class TaskPaginateList extends AbstractPaginateList {

  protected RestResponseFactory restResponseFactory;
  protected String serverRootUrl;
  
  public TaskPaginateList(RestResponseFactory restResponseFactory, String serverRootUrl) {
    this.restResponseFactory = restResponseFactory;
    this.serverRootUrl = serverRootUrl;
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<TaskResponse> responseList = new ArrayList<TaskResponse>();
    for (Object task : list) {
      responseList.add(restResponseFactory.createTaskResponse((Task) task, serverRootUrl));
    }
    return responseList;
  }
}
