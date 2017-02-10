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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
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
@Api(tags = { "Tasks" }, description = "Manage Tasks")
public class TaskVariableDataResource extends TaskVariableBaseResource {

	 @ApiImplicitParams(
	          @ApiImplicitParam(name = "scope", dataType = "string", value = "Scope of variable to be returned. When local, only task-local variable value is returned. When global, only variable value from the task’s parent execution-hierarchy are returned. When the parameter is omitted, a local variable will be returned if it exists, otherwise a global variable.", paramType = "query")
	  )
	  @ApiResponses(value = {
	          @ApiResponse(code = 200, message = "Indicates the task was found and the requested variables are returned."),
	          @ApiResponse(code = 404, message = "Indicates the requested task was not found or the task doesn’t have a variable with the given name (in the given scope). Status message provides additional information.")
	  })
	  @ApiOperation(value = "Get the binary data for a variable", tags = {"Tasks"}, nickname = "geTaskVariableData",
	          notes = "The response body contains the binary value of the variable. When the variable is of type binary, the content-type of the response is set to application/octet-stream, regardless of the content of the variable or the request accept-type header. In case of serializable, application/x-java-serialized-object is used as content-type.")
  @RequestMapping(value = "/runtime/tasks/{taskId}/variables/{variableName}/data", method = RequestMethod.GET, produces = "application/json")
  public @ResponseBody
  byte[] getVariableData(@ApiParam(name = "taskId") @PathVariable("taskId") String taskId,@ApiParam(name = "variableName") @PathVariable("variableName") String variableName,@ApiParam(hidden=true) @RequestParam(value = "scope", required = false) String scope,
      HttpServletRequest request, HttpServletResponse response) {

    try {
      byte[] result = null;

      RestVariable variable = getVariableFromRequest(taskId, variableName, scope, true);
      if (RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
        result = (byte[]) variable.getValue();
        response.setContentType("application/octet-stream");

      } else if (RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
        outputStream.writeObject(variable.getValue());
        outputStream.close();
        result = buffer.toByteArray();
        response.setContentType("application/x-java-serialized-object");

      } else {
        throw new ActivitiObjectNotFoundException("The variable does not have a binary data stream.", null);
      }
      return result;

    } catch (IOException ioe) {
      // Re-throw IOException
      throw new ActivitiException("Unexpected error getting variable data", ioe);
    }
  }
}
