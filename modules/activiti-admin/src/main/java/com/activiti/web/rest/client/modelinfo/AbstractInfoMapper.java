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
package com.activiti.web.rest.client.modelinfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractInfoMapper implements InfoMapper {

	protected DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	protected ObjectMapper objectMapper = new ObjectMapper();
	protected ArrayNode propertiesNode;

	public ArrayNode map(Object element) {
		propertiesNode = objectMapper.createArrayNode();
		mapProperties(element);
		return propertiesNode;
	}

	protected abstract void mapProperties(Object element);

	protected void createPropertyNode(String name, String value) {
		if (StringUtils.isNotEmpty(value)) {
			ObjectNode propertyNode = objectMapper.createObjectNode();
			propertyNode.put("name", name);
			propertyNode.put("value", value);
			propertiesNode.add(propertyNode);
		}
	}

	protected void createPropertyNode(String name, Date value) {
		if (value != null) {
			createPropertyNode(name, dateFormat.format(value));
		}
	}

	protected void createPropertyNode(String name, Boolean value) {
		if (value != null) {
			createPropertyNode(name, value.toString());
		}
	}

	protected void createPropertyNode(String name, List<String> values) {
		if (CollectionUtils.isNotEmpty(values)) {
			StringBuilder commaSeperatedString = new StringBuilder();
			for (String value : values) {
				if (commaSeperatedString.length() > 0) {
					commaSeperatedString.append(", ");
				}
				commaSeperatedString.append(value);
			}
			createPropertyNode(name, commaSeperatedString.toString());
		}
	}
}
