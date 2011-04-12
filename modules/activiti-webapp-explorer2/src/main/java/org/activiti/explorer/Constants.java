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
package org.activiti.explorer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;


/**
 * @author Joram Barrez
 */
public interface Constants {
  
  // Resource bundle name
  static final String RESOURCE_BUNDLE = "messages";
  
  // Security roles
  static final String SECURITY_ROLE = "security-role";
  static final String SECURITY_ROLE_USER = "user";
  static final String SECURITY_ROLE_ADMIN = "admin";
  
  // Date formatting
 static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy";
 static final DateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
  
  // Default diagram image extension, when name cannot be deducted from resource name
 static final String DEFAULT_DIAGRAM_IMAGE_EXTENSION = "png";
 
 // MIMETYPES
 static final String MIMETYPE_BPM = "image/bpm";
 static final String MIMETYPE_GIF = "image/gif";
 static final String MIMETYPE_JPEG = "image/jpeg";
 static final String MIMETYPE_PNG = "image/png";
 static final Collection<String> DEFAULT_IMAGE_MIMETYPES = Arrays.asList(
         MIMETYPE_BPM, MIMETYPE_GIF, MIMETYPE_JPEG, MIMETYPE_PNG);
 

}
