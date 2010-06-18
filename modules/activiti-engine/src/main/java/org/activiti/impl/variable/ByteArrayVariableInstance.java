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
package org.activiti.impl.variable;

import org.activiti.impl.bytes.ByteArrayImpl;


/**
 * @author Tom Baeyens
 */
public class ByteArrayVariableInstance extends VariableInstance {

  private static final long serialVersionUID = 1L;
  
  protected ByteArrayImpl byteArray;

  public String getTypeName() {
    return "bytes";
  }

  public Object getValue() {
    if (byteArray==null) {
      return null;
    }
    return byteArray.getBytes();
  }

  public void setValue(Object value) {
    // TODO make sure the old one gets deleted (if there is one)
    if (value!=null) {
      byteArray = new ByteArrayImpl(this, (byte[])value);
    } else {
      byteArray = null;
    }
  }

  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return true;
    }
    return byte[].class.isAssignableFrom(value.getClass());
  }
}
