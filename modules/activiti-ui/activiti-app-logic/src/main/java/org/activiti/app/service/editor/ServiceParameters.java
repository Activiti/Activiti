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
package org.activiti.app.service.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Wrapper around parameters that should be passed trough to the delegated service call.
 * 
 * @author Frederik Heremans
 *
 */
public class ServiceParameters {
 
	protected Map<String, Object> parameters;
	protected Set<String> validParameterNames;
	
	public ServiceParameters() {
		parameters = new HashMap<String, Object>();
  }
	
	public void addParameter(String name, Object value) {
		parameters.put(name, value);
	}
	
	/**
	 * Adds a parameter and marks is as valid.
	 */
	public void addValidParameter(String name, Object value) {
		parameters.put(name, value);
		validParameterNames.add(name);
	}
	
	public Object getParameter(String name) {
		return parameters.get(name);
	}
	
	public boolean isParameterSet(String name) {
		return parameters.containsKey(name);
	}
	
	public void addValidParameterNames(String[] validParameters) {
		if(validParameterNames == null) {
			validParameterNames = new HashSet<String>();
		}
		this.validParameterNames.addAll(Arrays.asList(validParameters));
	}
	
	/**
	 * @return all valid parameters which are set in this instance. If no {@link #addValidParameterNames(String[])} has been
	 * called, ALL parameters will be returned.
	 */
	public Map<String, Object> getValidParameterMap() {
		if(validParameterNames == null) {
			// All parameters are valid, no need to filter
			return Collections.unmodifiableMap(parameters);
		} else {
			// Only return valid parameters
			Map<String, Object> result = new HashMap<String, Object>();
			for(Entry<String, Object> parameter : parameters.entrySet()) {
				if(validParameterNames.contains(parameter.getKey())) {
					result.put(parameter.getKey(), parameter.getValue());
				}
			}
			return result;
		}
	}

	/**
	 * Creates a new {@link ServiceParameters} instance based on all non-empty query-parameters
	 * in the given request.
	 */
	public static ServiceParameters fromHttpRequest(HttpServletRequest request) {
		ServiceParameters parameters = new ServiceParameters();
		
		String value = null;
		String name = null;
		Enumeration<String> params = request.getParameterNames();
		while(params.hasMoreElements()) {
			name = params.nextElement();
			value = request.getParameter(name);
			
			if(value != null && StringUtils.isNotEmpty(value)) {
				parameters.addParameter(name, value);
			}
		}
	  return parameters;
  }
	
	/**
	 * Creates a new {@link ServiceParameters} instance based on all properties in the given
	 * object node. Only numbers, text and boolean values are added, nested object structures
	 * are ignored.
	 */
	public static ServiceParameters fromObjectNode(ObjectNode node) {
		ServiceParameters parameters = new ServiceParameters();
		
		Iterator<String> ir = node.fieldNames();
		String name = null;
		JsonNode value = null; 
		while(ir.hasNext()) {
			name = ir.next();
			value = node.get(name);
			
			// Depending on the type, extract the raw value object
			if(value != null) {
				if(value.isNumber()) {
					parameters.addParameter(name, value.numberValue());
				} else if(value.isBoolean()) {
					parameters.addParameter(name, value.booleanValue());
				} else if(value.isTextual()) {
					parameters.addParameter(name, value.textValue());
				}
			}
		}
	  return parameters;
  }
}