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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.rest.common.api.DataResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



/**
 * @author Frederik Heremans
 */
@RestController
public class ExecutionQueryResource extends ExecutionBaseResource {

  @RequestMapping(value="/query/executions", method = RequestMethod.POST, produces="application/json")
  public DataResponse queryProcessInstances(@RequestBody ExecutionQueryRequest queryRequest,
      @RequestParam Map<String,String> allRequestParams, HttpServletRequest request) {
    
    return getQueryResponse(queryRequest, allRequestParams, 
        request.getRequestURL().toString().replace("/query/executions", ""));
  }
}
