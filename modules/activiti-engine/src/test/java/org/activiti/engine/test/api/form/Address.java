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

package org.activiti.engine.test.api.form;

import java.io.Serializable;


/**
 * @author Tom Baeyens
 */
public class Address implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private String street;

  public String getStreet() {
    return street;
  }
  public void setStreet(String street) {
    this.street = street;
  }
}
