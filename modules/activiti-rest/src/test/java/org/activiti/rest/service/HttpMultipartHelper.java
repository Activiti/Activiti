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

package org.activiti.rest.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 * A representation class that supports Multi-part form-upload.
 * 
 * Using {@link MultipartRequestEntity} to do the heavy lifting.
 * 
 * @author Tijs Rademakers
 */
public class HttpMultipartHelper {

  public static HttpEntity getMultiPartEntity(String fileName, String contentType, InputStream fileStream, 
      Map<String, String> additionalFormFields) throws IOException {
    
    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
    
    if (additionalFormFields != null && !additionalFormFields.isEmpty()) {
      for (Entry<String, String> field : additionalFormFields.entrySet()) {
        entityBuilder.addTextBody(field.getKey(), field.getValue());
      }
    }
    
    entityBuilder.addBinaryBody(fileName, IOUtils.toByteArray(fileStream), ContentType.create(contentType), fileName);
    
    return entityBuilder.build();
  }
}
