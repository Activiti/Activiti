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
 * Variable type capable of storing reference to JPA-entities. Only JPA-Entities which
 * are configured by annotations are supported. Use of compound primary keys is not supported.
 * 
 * @author Frederik Heremans
 */
public class JPAEntityVariableType implements VariableType {

  public static final String TYPE_NAME = "jpa-entity";
  
  private JPAEntityMappings mappings;
  
  public JPAEntityVariableType() {
    mappings = new JPAEntityMappings();
  }
  
  public String getTypeName() {
    return TYPE_NAME;
  }

  public boolean isCachable() {
    return false;
  }

  public boolean isAbleToStore(Object value) {
    if(value == null) {
      return true;
    }
    return mappings.isJPAEntity(value);      
  }

  public void setValue(Object value, ValueFields valueFields) {
    if(value != null) {
      String className = mappings.getJPAClassString(value);
      String idString = mappings.getJPAIdString(value);
      valueFields.setTextValue(className);
      valueFields.setTextValue2(idString);      
    } else {
      valueFields.setTextValue(null);
      valueFields.setTextValue2(null);            
    }
  }

  public Object getValue(ValueFields valueFields) {
    if(valueFields.getTextValue() != null && valueFields.getTextValue2() != null) {
      return mappings.getJPAEntity(valueFields.getTextValue(), valueFields.getTextValue2());      
    }
    return null;
  }

 
}
