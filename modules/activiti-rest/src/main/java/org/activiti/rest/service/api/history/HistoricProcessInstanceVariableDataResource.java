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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@RestController
@Api(tags = { "History" }, description = "Manage History", authorizations = { @Authorization(value = "basicAuth") })
public class HistoricProcessInstanceVariableDataResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;

  @Autowired
  protected HistoryService historyService;


  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the process instance was found and the requested variable data is returned."),
      @ApiResponse(code = 404, message = "Indicates the requested process instance was not found or the process instance doesn’t have a variable with the given name or the variable doesn’t have a binary stream available. Status message provides additional information.")})
  @ApiOperation(value = "Get the binary data for a historic process instance variable", tags = {"History"}, nickname = "getHistoricProcessInstanceVariableData",
  notes = "The response body contains the binary value of the variable. When the variable is of type binary, the content-type of the response is set to application/octet-stream, regardless of the content of the variable or the request accept-type header. In case of serializable, application/x-java-serialized-object is used as content-type.")
  @RequestMapping(value = "/history/historic-process-instances/{processInstanceId}/variables/{variableName}/data", method = RequestMethod.GET)
  public @ResponseBody
  byte[] getVariableData(@ApiParam(name="processInstanceId") @PathVariable("processInstanceId") String processInstanceId,@ApiParam(name="variableName") @PathVariable("variableName") String variableName, HttpServletRequest request, HttpServletResponse response) {

    try {
      byte[] result = null;
      RestVariable variable = getVariableFromRequest(true, processInstanceId, variableName, request);
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
      throw new ActivitiException("Unexpected exception getting variable data", ioe);
    }
  }

  public RestVariable getVariableFromRequest(boolean includeBinary, String processInstanceId, String variableName, HttpServletRequest request) {

    HistoricProcessInstance processObject = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).includeProcessVariables().singleResult();

    if (processObject == null) {
      throw new ActivitiObjectNotFoundException("Historic process instance '" + processInstanceId + "' couldn't be found.", HistoricProcessInstanceEntity.class);
    }

    Object value = processObject.getProcessVariables().get(variableName);

    if (value == null) {
      throw new ActivitiObjectNotFoundException("Historic process instance '" + processInstanceId + "' variable value for " + variableName + " couldn't be found.", VariableInstanceEntity.class);
    } else {
      return restResponseFactory.createRestVariable(variableName, value, null, processInstanceId, RestResponseFactory.VARIABLE_HISTORY_PROCESS, includeBinary);
    }
  }
}
