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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * @author Frederik Heremans
 */
public class ExecutionVariableDataResource extends BaseExecutionVariableResource {

  @Get
  public InputRepresentation getVariableData() {
    if (authenticate() == false)
      return null;

    try {
      InputStream dataStream = null;
      MediaType mediaType = null;
      RestVariable variable = getVariableFromRequest(true);
      if(RestResponseFactory.BYTE_ARRAY_VARIABLE_TYPE.equals(variable.getType())) {
        dataStream = new ByteArrayInputStream((byte[]) variable.getValue());
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      } else if(RestResponseFactory.SERIALIZABLE_VARIABLE_TYPE.equals(variable.getType())) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(buffer);
        outputStream.writeObject(variable.getValue());
        outputStream.close();
        dataStream = new ByteArrayInputStream(buffer.toByteArray());
        mediaType = MediaType.APPLICATION_JAVA_OBJECT;
        
      } else {
        throw new ActivitiObjectNotFoundException("The variable does not have a binary data stream.", null);
      }
      return new InputRepresentation(dataStream, mediaType);
    } catch(IOException ioe) {
      // Re-throw IOException
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ioe);
    }
  }
}
