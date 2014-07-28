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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;

/**
 * A representation class that supports Multi-part form-upload, which is missing
 * from the current restlet libraries (see https://github.com/restlet/restlet-framework-java/issues/23).
 * 
 * Using {@link MultipartRequestEntity} to do the heavy lifting.
 * 
 * @author Frederik Heremans
 */
public class HttpMultipartRepresentation extends InputRepresentation {

  public HttpMultipartRepresentation(final String fileName, final InputStream fileStream, Map<String, String> additionalFormFields) throws IOException {
    super(null, MediaType.MULTIPART_FORM_DATA);
    processStreamAndSetMediaType(fileName, fileStream, additionalFormFields);
  }
  
  public HttpMultipartRepresentation(final String fileName, final InputStream fileStream) throws IOException {
    this(fileName, fileStream, null);
  }

  public void processStreamAndSetMediaType(final String fileName, InputStream fileStream, Map<String, String> additionalFormFields) throws IOException {
    // Copy the stream in a bytearray to get the length
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    IOUtils.copy(fileStream, output);

    final int length = output.toByteArray().length;
    final InputStream stream = new ByteArrayInputStream(output.toByteArray());

    List<Part> parts = new ArrayList<Part>();
    
    // Filepart is based on in-memory stream an file-name rather than an actual file
    FilePart filePart = new FilePart(fileName, new PartSource() {
      @Override
      public long getLength() {
        return length;
      }
      
      @Override
      public String getFileName() {
        return fileName;
      }

      @Override
      public InputStream createInputStream() throws IOException {
        return stream;
      }
    });
    parts.add(filePart);
    
    if(additionalFormFields != null && !additionalFormFields.isEmpty()) {
      for(Entry<String, String> field : additionalFormFields.entrySet()) {
        parts.add(new StringPart(field.getKey(), field.getValue()));
      }
    }
    
    MultipartRequestEntity entity = new MultipartRequestEntity(parts.toArray(new Part[] {}), new HttpMethodParams());

    // Let the entity write the raw multipart to a bytearray, which is used as a source
    // for the input-stream returned by this Representation
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    entity.writeRequest(os);
    setMediaType(new MediaType(entity.getContentType()));
    setStream(new ByteArrayInputStream(os.toByteArray()));
  }
}
