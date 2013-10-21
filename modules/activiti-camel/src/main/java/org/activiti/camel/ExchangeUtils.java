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

  public static final String CAMELBODY = "camelBody";
	
  /**
   * Copies variables from Camel into Activiti.
   * 
   * This method will copy the Camel body to the "camelBody" variable. It will copy the Camel body to individual variables within Activiti if it is of type 
   * Map&lt;String, Object&gt; or it will copy the Object as it comes.
   * <ul>
   * <li>If the copyVariablesFromProperties parameter is set on the endpoint, the properties are copied instead</li>
   * <li>If the copyCamelBodyToBodyAsString parameter is set on the endpoint, the camelBody is converted to java.lang.String and added as a camelBody variable,
   * unless it is a Map&lt;String, Object&gt;</li>
   * <li>If the copyVariablesFromHeader parameter is set on the endpoint, each Camel Header will be copied to an individual variable within Activiti.</li>
   * </ul>
   * 
   * @param exchange The Camel Exchange object
   * @param activitiEndpoint The ActivitiEndpoint implementation
   * @return A Map&lt;String, Object&gt; containing all of the variables to be used in Activiti
   */
  
  public static Map<String, Object> prepareVariables(Exchange exchange, ActivitiEndpoint activitiEndpoint) {
    boolean shouldReadFromProperties = activitiEndpoint.isCopyVariablesFromProperties();
    Map<String, Object> camelVarMap = null;
    
    if (shouldReadFromProperties) {
      camelVarMap = exchange.getProperties();
    } else {
      camelVarMap = new HashMap<String, Object>();
      Object camelBody = exchange.getIn().getBody();
      
      if(camelBody instanceof Map<?,?>) {
        Map<?,?> camelBodyMap = (Map<?,?>)camelBody;
        for (@SuppressWarnings("rawtypes") Map.Entry e : camelBodyMap.entrySet()) {
          if (e.getKey() instanceof String) {
            camelVarMap.put((String) e.getKey(), e.getValue());
          }
        }
      } else {
        if(activitiEndpoint.isCopyCamelBodyToBodyAsString() && !(camelBody instanceof String)) {
          camelBody = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, camelBody);
        }
        camelVarMap.put(CAMELBODY, camelBody);
      }

      if(activitiEndpoint.isCopyVariablesFromHeader()) {
        for(Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
    	  camelVarMap.put(header.getKey(), header.getValue());
        }
      }
    }
    
    return camelVarMap;
  }
}
