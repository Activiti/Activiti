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

package org.activiti.rest.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

/**
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class MultipartRequestObject implements ActivitiRequestObject {

  private WebScriptServletRequest request;

  public MultipartRequestObject(WebScriptRequest req) throws IOException {
    this.request = (WebScriptServletRequest) req;
  }

  public MultipartRequestObject(WebScriptServletRequest req) throws IOException {
    this.request = req;
  }

  public ActivitiRequestObject getBodyObject(String param) {
    return null;
  }

  public Boolean getBoolean(String param) {
    return Boolean.valueOf(this.request.getParameter(param));
  }

  public Map<String, Object> getFormVariables() {
    return null;
  }

  public Integer getInt(String param) {
    String value = this.request.getParameter(param);
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw ActivitiRequest.getInvalidTypeException(param, value.toString(), INTEGER);
    }
  }

  @SuppressWarnings("unchecked")
  public List getList(String param) {
    return null;
  }

  public String getString(String param) {
    return this.request.getParameter(param);
  }

}
