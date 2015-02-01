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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tijs Rademakers
 */
public class DataGridRow {

  protected int index;
  protected List<DataGridField> fields = new ArrayList<DataGridField>();

  public int getIndex() {
    return index;
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public List<DataGridField> getFields() {
    return fields;
  }
  public void setFields(List<DataGridField> fields) {
    this.fields = fields;
  }
  
  public DataGridRow clone() {
    DataGridRow clone = new DataGridRow();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(DataGridRow otherRow) {
    setIndex(otherRow.getIndex());
    
    fields = new ArrayList<DataGridField>();
    if (otherRow.getFields() != null && !otherRow.getFields().isEmpty()) {
      for (DataGridField field : otherRow.getFields()) {
        fields.add(field.clone());
      }
    }
  }
}
