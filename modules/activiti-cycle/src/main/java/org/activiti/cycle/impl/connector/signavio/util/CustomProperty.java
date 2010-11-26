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

package org.activiti.cycle.impl.connector.signavio.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Falko Menge <falko.menge@camunda.com>
 */
public enum CustomProperty {

	ORIGINAL_NAME("Original Name"),
	ORIGINAL_ID("Original ID");

	private final String name;
	private final Pattern pattern;

	private CustomProperty(String name) {
		this.name = name;
		this.pattern = Pattern.compile("(.*)(" + name + ":\\s+\"((?:[^\"]|\"\")+)\")(.*)", Pattern.DOTALL);
	}

  public String getValue(String propertyContainer) {
    String propertyValue = null;
    if (propertyContainer != null && propertyContainer.length() > 0) {
      Matcher matcher = getPatternMatcher(propertyContainer);
      if (matcher.matches()) {
        propertyValue = matcher.group(3);
        propertyValue = propertyValue.replace("\"\"", "\"");
        propertyValue = propertyValue.replace("\\n", "\n");
      }
    }
    return propertyValue;
  }

  public String setValueUnlessPropertyExists(String propertyContainer, String propertyValue) {
    // check if property already exists in container string
    if (getValue(propertyContainer) == null) {
      propertyContainer = setValue(propertyContainer, propertyValue);
    }
    return propertyContainer;
  }

  public String setValue(String propertyContainer, String propertyValue) {
    remove(propertyContainer);
    propertyValue = propertyValue.replace("\"", "\"\"");
    propertyValue = propertyValue.replace("\n", "\\n");
    String propertySerialization = name + ": \"" + propertyValue + "\"";
    if (propertyContainer != null && propertyContainer.length() > 0) {
      propertyContainer = propertyContainer + " " + propertySerialization;
    } else {
      propertyContainer = propertySerialization;
    }
    return propertyContainer;
  }

  public String remove(String propertyContainer) {
    if (propertyContainer != null && propertyContainer.length() > 0) {
      Matcher matcher = getPatternMatcher(propertyContainer);
      if (matcher.matches()) {
        propertyContainer = (matcher.group(1) + matcher.group(4)).trim();
      }
    }
    return propertyContainer;
  }

  public String toString() {
		return name;
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
	public Matcher getPatternMatcher(String input) {
		return pattern.matcher(input);
	}
}