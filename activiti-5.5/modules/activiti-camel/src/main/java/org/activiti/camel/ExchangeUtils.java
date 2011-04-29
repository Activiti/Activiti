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
package org.activiti.camel;

import org.apache.camel.Exchange;

import java.util.HashMap;
import java.util.Map;

public class ExchangeUtils {

  static Map<String, Object> prepareVariables(Exchange exchange, ActivitiEndpoint activitiEndpoint) {
    Map<String, Object> ret = new HashMap<String, Object>();
    boolean shouldReadFromProperties = activitiEndpoint.isCopyVariablesFromProperties();
    Map<?, ?> m = shouldReadFromProperties ? exchange.getProperties() : exchange.getIn().getBody(Map.class);
    if (m != null) {
      for (Map.Entry e : m.entrySet()) {
        if (e.getKey() instanceof String) {
          ret.put((String) e.getKey(), e.getValue());
        }
      }
    }
    return ret;
  }
}
