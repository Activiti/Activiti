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


/**
 * @author Joram Barrez
 */
public interface Constant {
  
  // Resource bundle name
  static final String RESOURCE_BUNDLE = "messages";
  
  // Date formatting
 static final DateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("dd MMM yyyy - hh:mm");
  
  // Default diagram image extension, when name cannot be deducted from resource name
 static final String DEFAULT_DIAGRAM_IMAGE_EXTENSION = "png";

}
