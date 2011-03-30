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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Interface for hiding json or xml parsing of request body
 *
 * @author Erik Winlof
 */
public interface ActivitiRequestObject {

  public static final String STRING = "string";
  public static final String INTEGER = "integer";
  public static final String BOOLEAN = "boolean";
  public static final String DATE = "iso8601 date";
  public static final String OBJECT = "object";
  public static final String ARRAY = "array";

  public String getString(String param);
  public Integer getInt(String param);
  public Boolean getBoolean(String param);
  public ActivitiRequestObject getBodyObject(String param);
  public List getList(String param);
  public Map<String, Object> getFormVariables();

}
