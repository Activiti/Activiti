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
package org.activiti.examples.bpmn.usertask.taskcandidate;

import java.io.Serializable;

/**

 */

public class TestBean implements Serializable {

  private static final long serialVersionUID = -8840933240517620693L;

  public String twoParametersMethod(String param1, String param2) {
    return "kermit";
  }

  public String returnParamAsGroupMethod(String param1) {
    return param1;
  }

  public String oneParameterMethod(String param1) {
    return "kermit";
  }

  public String getTestProperty() {
    return "kermit";
  }

}
