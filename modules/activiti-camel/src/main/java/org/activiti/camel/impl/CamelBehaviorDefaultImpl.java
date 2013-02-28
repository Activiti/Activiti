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

package org.activiti.camel.impl;

import java.util.Map;

import org.activiti.camel.ActivitiComponent;
import org.activiti.camel.ActivitiEndpoint;
import org.activiti.camel.CamelBehavior;
import org.apache.camel.Exchange;

/**
 * This implementation of the CamelBehavior abstract class works just like CamelBehaviour does; it copies variables 
 * into Camel as properties.
 * 
 * @author Ryan Johnston (@rjfsu), Tijs Rademakers
 */
public class CamelBehaviorDefaultImpl extends CamelBehavior {
	
	private static final long serialVersionUID = 003L;
	
	@Override
	protected void modifyActivitiComponent(ActivitiComponent component) {
		//Set the copy method for new endpoints created using this component.
		component.setCopyVariablesToProperties(true);
		component.setCopyVariablesToBodyAsMap(false);
		component.setCopyCamelBodyToBody(false);
	}
	
	@Override
  protected void copyVariables(Map<String, Object> variables, Exchange exchange, ActivitiEndpoint endpoint) {
	  if (endpoint.isCopyVariablesToBodyAsMap()) {
	    copyVariablesToBodyAsMap(variables, exchange);
	  } else if (endpoint.isCopyCamelBodyToBody()) {
	    copyVariablesToBody(variables, exchange);
	  } else {
	    copyVariablesToProperties(variables, exchange);
	  }
	}
}
