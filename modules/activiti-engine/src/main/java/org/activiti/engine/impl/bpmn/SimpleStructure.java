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
package org.activiti.engine.impl.bpmn;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simple in memory structure
 * 
 * @author Esteban Robles Luna
 */
public class SimpleStructure implements Structure {

  protected String id;
  
  protected List<String> fieldNames;
  
  public SimpleStructure(String id) {
    this.id = id;
    this.fieldNames = new ArrayList<String>();
  }
  
  public int getFieldSize() {
    return this.fieldNames.size();
  }

  public String getId() {
    return this.id;
  }
  
  public void setFieldName(int index, String fieldName) {
    this.growFieldNamesToContain(index);
    this.fieldNames.set(index, fieldName);
  }

  private void growFieldNamesToContain(int index) {
    if (!(this.fieldNames.size() - 1 >= index)) {
      for (int i = this.fieldNames.size(); i <= index; i++) {
        this.fieldNames.add(null);
      }
    }
  }
}
