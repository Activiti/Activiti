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
package org.activiti.engine.impl.variable;



/**
 * @author Martin Grofcik
 */
public class LongStringType extends SerializableType {

  private final int minLength;

  public LongStringType(int minLength) {this.minLength = minLength;}

  public String getTypeName() {
    return "longString";
  }

  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return false;
    }
    if (String.class.isAssignableFrom(value.getClass())) {
      String stringValue = (String) value;
      return stringValue.length() >= minLength;
    }
    return false;
  }
}
