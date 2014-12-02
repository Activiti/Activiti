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

package org.activiti.rest.service.api.runtime.process;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.runtime.Execution;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class ProcessInstanceVariableDataResource extends BaseExecutionVariableResource {

  @RequestMapping(value="/runtime/process-instances/{processInstanceId}/variables/{variableName}/data", method = RequestMethod.GET)
  public @ResponseBody byte[] getVariableData(@PathVariable("processInstanceId") String processInstanceId, 
      @PathVariable("variableName") String variableName, @RequestParam(value="scope", required=false) String scope,
      HttpServletRequest request, HttpServletResponse response) {
    
    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    return getVariableDataByteArray(execution, variableName, scope, response);
  }
}
