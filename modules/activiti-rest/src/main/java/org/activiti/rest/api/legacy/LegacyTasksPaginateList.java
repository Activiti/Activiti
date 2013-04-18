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

package org.activiti.rest.api.legacy;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Task;
import org.activiti.rest.api.AbstractPaginateList;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.legacy.task.LegacyTaskResponse;

/**
 * @author Tijs Rademakers
 */
public class LegacyTasksPaginateList extends AbstractPaginateList {

  @SuppressWarnings("rawtypes")
  @Override
  protected List processList(List list) {
    List<LegacyTaskResponse> responseList = new ArrayList<LegacyTaskResponse>();
    for (Object task : list) {
      LegacyTaskResponse taskResponse = new LegacyTaskResponse((Task) task);
      TaskFormData taskFormData = ActivitiUtil.getFormService().getTaskFormData(taskResponse.getId());
      if(taskFormData != null) {
        taskResponse.setFormResourceKey(taskFormData.getFormKey());     
      }
      responseList.add(taskResponse);
    }
    return responseList;
  }
}
