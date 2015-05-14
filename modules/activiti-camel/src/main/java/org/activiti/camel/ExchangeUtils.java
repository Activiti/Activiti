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

import org.activiti.engine.ActivitiException;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.springframework.util.StringUtils;

/**
 * This class contains one method - prepareVariables - that is used to copy variables from Camel into Activiti.
 * 
 * @author Ryan Johnston (@rjfsu), Tijs Rademakers, Arnold Schrijver
 */
public class ExchangeUtils {

  public static final String CAMELBODY = "camelBody";
  protected static final String IGNORE_MESSAGE_PROPERTY = "CamelMessageHistory";
	
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
      // filter camel property that can't be serializable for camel version after 2.12.x+
      Map<String, Object> newCamelVarMap = new HashMap<String, Object>();
      for (String s : camelVarMap.keySet()) {
        if (IGNORE_MESSAGE_PROPERTY.equalsIgnoreCase(s) == false) {
          newCamelVarMap.put(s, camelVarMap.get(s));
        }
      }
      camelVarMap = newCamelVarMap;
      
    } else {
      camelVarMap = new HashMap<String, Object>();
      Object camelBody = null;
      if (exchange.hasOut()) {
    	  camelBody = exchange.getOut().getBody();
      } else {
    	  camelBody = exchange.getIn().getBody();
      }
      
      if (camelBody instanceof Map<?,?>) {
        Map<?,?> camelBodyMap = (Map<?,?>)camelBody;
        for (@SuppressWarnings("rawtypes") Map.Entry e : camelBodyMap.entrySet()) {
          if (e.getKey() instanceof String) {
            camelVarMap.put((String) e.getKey(), e.getValue());
          }
        }
      } else {
        if (activitiEndpoint.isCopyCamelBodyToBodyAsString() && !(camelBody instanceof String)) {
          camelBody = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, camelBody);
        }
        if (camelBody != null) {
          camelVarMap.put(CAMELBODY, camelBody);
        }
      }

      if (activitiEndpoint.isCopyVariablesFromHeader()) {
        boolean isSetProcessInitiator = activitiEndpoint.isSetProcessInitiator();
        for (Map.Entry<String, Object> header : exchange.getIn().getHeaders().entrySet()) {
          // Don't pass the process initiator header as a variable.
          if (isSetProcessInitiator && activitiEndpoint.getProcessInitiatorHeaderName().equals(header.getKey())) {
              continue;
          }
          camelVarMap.put(header.getKey(), header.getValue());
        }
      }
    }
    
    return camelVarMap;
  }
  
  /**
   * Gets the value of the Camel header that contains the userId to be set as the process initiator.
   * Returns null if no header name was specified on the Camel route.
   * 
   * @param exchange The Camel Exchange object
   * @param activitiEndpoint The ActivitiEndpoint implementation
   * @return The userId of the user to be set as the process initiator
   */
  public static String prepareInitiator(Exchange exchange, ActivitiEndpoint activitiEndpoint) {
     
      String initiator = null;
      if (activitiEndpoint.isSetProcessInitiator()) {
          try {
              initiator = exchange.getIn().getHeader(activitiEndpoint.getProcessInitiatorHeaderName(), String.class);
          }
          catch (TypeConversionException e) {
              throw new ActivitiException("Initiator header '" + 
                      activitiEndpoint.getProcessInitiatorHeaderName() + "': Value must be of type String.", e);
          }
          
          if (!StringUtils.hasText(initiator)) {
              throw new RuntimeException("Initiator header '" + 
                      activitiEndpoint.getProcessInitiatorHeaderName() + "': Value must be provided");
          }
      }
      return initiator;
  }
}
