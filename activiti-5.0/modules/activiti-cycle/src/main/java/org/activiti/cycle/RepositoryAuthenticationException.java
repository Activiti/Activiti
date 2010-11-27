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

package org.activiti.cycle;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class RepositoryAuthenticationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private HashMap<String, String> connectors;

  public RepositoryAuthenticationException(String message, Map<String, String> connectors, Throwable cause) {
    super(message, cause);
    this.connectors = new HashMap<String, String>(connectors);
  }

  public RepositoryAuthenticationException(String message, Map<String, String> connectors) {
    super(message);
    this.connectors = new HashMap<String, String>(connectors);
  }

  public HashMap<String, String> getConnectors() {
    return connectors;
  }

}
