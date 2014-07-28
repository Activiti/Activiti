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
 * Interface describing a class that is capable of resolving the media-type of a resource/file
 * based on the resource name.
 * 
 * @author Frederik Heremans
 */
public interface MediaTypeResolver {

  /**
   * @return the {@link MediaType} resolved from the given resourcename. Returns null if the
   * media-type cannot be resolved.
   */
  MediaType resolveMediaType(String resourceName);
}
