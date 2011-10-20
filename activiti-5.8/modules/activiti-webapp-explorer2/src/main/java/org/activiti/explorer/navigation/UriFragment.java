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

package org.activiti.explorer.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activiti.explorer.util.UriUtility;

/**
 * Class containing parts of an uri-fragment (eg: "task", "inbox", "123") and
 * parameters based on the URL's query string.
 * 
 * @author Frederik Heremans
 */
public class UriFragment {

  private List<String> uriParts;
  private Map<String, String> parameters;

  public UriFragment(String fragment) {
    // Extract fragment parts
    String fragmentUri = UriUtility.extractUri(fragment);
    uriParts = UriUtility.getFragmentParts(fragmentUri);

    // Extract parameters
    String queryString = UriUtility.extractQueryString(fragment);
    parameters = UriUtility.parseQueryParameters(queryString, null);
  }

  public UriFragment(List<String> uriParts, Map<String, String> parameters) {
    this.uriParts = uriParts;
    this.parameters = parameters;
  }
  
  public UriFragment(Map<String, String> parameters, String... uriParts) {
    this.uriParts = new ArrayList<String>(Arrays.asList(uriParts));
    this.parameters = parameters;
  }
  
  public UriFragment(String... uriParts) {
    this(new LinkedHashMap<String, String>(), uriParts);
  }
  
  public void addParameter(String name, String value) {
    parameters.put(name, value);
  }
  
  public void addUriPart(String part) {
    uriParts.add(part);
  }

  public List<String> getUriParts() {
    return uriParts;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  
  public String getParameter(String name) {
    if (parameters != null) {
      return parameters.get(name);
    }
    return null;
  }

  public String getUriPart(int index) {
    if (index >= 0 && index < uriParts.size()) {
      return uriParts.get(index);
    }
    return null;
  }

  @Override
  public String toString() {
    return UriUtility.getPath(uriParts) + UriUtility.getQueryString(parameters);
  }
}
