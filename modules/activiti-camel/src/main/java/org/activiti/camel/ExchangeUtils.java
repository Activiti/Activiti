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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;

/**
 * This class contains one method - prepareVariables - that is used to copy variables from Camel into Activiti.
 * 
 * @author Ryan Johnston (@rjfsu), Tijs Rademakers
 */
public class ExchangeUtils {

  /**
   * Copies variables from Camel into Activiti.
   * 
   * This method will conditionally copy the Camel body to the "camelBody" variable if it is of type java.lang.String, OR it will copy the Camel body to
   * individual variables within Activiti if it is of type Map<String,Object>.
   * If the copyVariablesFromProperties parameter is set on the endpoint, the properties are copied instead
   * 
   * @param exchange The Camel Exchange object
   * @param activitiEndpoint The ActivitiEndpoint implementation
   * @return A Map<String,Object> containing all of the variables to be used in Activiti
   */
  
  public static Map<String, Object> prepareVariables(Exchange exchange, ActivitiEndpoint activitiEndpoint) {
    boolean shouldReadFromProperties = activitiEndpoint.isCopyVariablesFromProperties();
    Map<String, Object> camelVarMap = null;
    
    if (shouldReadFromProperties) {
      camelVarMap = exchange.getProperties();
    } else {
      camelVarMap = new HashMap<String, Object>();
      Object camelBody = exchange.getIn().getBody();
      if(camelBody instanceof String) {
        camelVarMap.put("camelBody", camelBody);
      }
      else if(camelBody instanceof Map<?,?>) {
        Map<?,?> camelBodyMap = (Map<?,?>)camelBody;
        for (@SuppressWarnings("rawtypes") Map.Entry e : camelBodyMap.entrySet()) {
          if (e.getKey() instanceof String) {
            camelVarMap.put((String) e.getKey(), e.getValue());
          }
        }
      }
    }
    
    return camelVarMap;
  }
}
