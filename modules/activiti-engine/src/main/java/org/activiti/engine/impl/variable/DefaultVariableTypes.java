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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;

/**
 * @author Tom Baeyens
 */
public class DefaultVariableTypes implements VariableTypes, Serializable {

  private static final long serialVersionUID = 1L;

  private final List<VariableType> typesList = new ArrayList<VariableType>();
  private final Map<String, VariableType> typesMap = new HashMap<String, VariableType>();

  public DefaultVariableTypes addType(VariableType type) {
    return addType(type, typesList.size());
  }
  
  public DefaultVariableTypes addType(VariableType type, int index) {
    typesList.add(index, type);
    typesMap.put(type.getTypeName(), type);      
    return this;
  }

  public void setTypesList(List<VariableType> typesList) {
    this.typesList.clear();
    this.typesList.addAll(typesList);
    this.typesMap.clear();
    for (VariableType type : typesList) {
      typesMap.put(type.getTypeName(), type);
    }
  }

  public VariableType getVariableType(String typeName) {
    return typesMap.get(typeName);
  }

  public VariableType findVariableType(Object value) {
    for (VariableType type : typesList) {
      if (type.isAbleToStore(value)) {
        return type;
      }
    }
    throw new ActivitiException("couldn't find a variable type that is able to serialize " + value);
  }

  public int getTypeIndex(VariableType type) {
    return typesList.indexOf(type);
  }

  public int getTypeIndex(String typeName) {
    VariableType type = typesMap.get(typeName);
    if(type != null) {
      return getTypeIndex(type);
    } else {
      return -1;
    }
  }

  public VariableTypes removeType(VariableType type) {
    typesList.remove(type);
    typesMap.remove(type.getTypeName());
    return this;
  }
}
