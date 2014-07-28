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

package org.activiti.rest.common.application;

import org.restlet.data.MediaType;


/**
 * Default implementation of a {@link MediaTypeResolver}, resolving a limited set
 * of well-known media-types used in the engine. 
 * 
 * @author Frederik Heremans
 */
public class DefaultMediaTypeResolver implements MediaTypeResolver {

  @Override
  public MediaType resolveMediaType(String resourceName) {
    MediaType mediaType = null;
    if(resourceName != null && !resourceName.isEmpty()) {
      String lowerResourceName = resourceName.toLowerCase();
      
      if (lowerResourceName.endsWith("png")) {
        mediaType = MediaType.IMAGE_PNG;
      } else if (lowerResourceName.endsWith("xml") || lowerResourceName.endsWith("bpmn")) {
        mediaType = MediaType.TEXT_XML;
      } 
    }
    return mediaType;
  }
}
