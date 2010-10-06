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
public class DefaultVariableTypes implements Serializable, VariableTypes {

  private static final long serialVersionUID = 1L;

  private final List<Type> typesList = new ArrayList<Type>();
  private final Map<String, Type> typesMap = new HashMap<String, Type>();

  public DefaultVariableTypes() {
    addType(new NullType());
    addType(new StringType());
    addType(new ShortType());
    addType(new IntegerType());
    addType(new LongType());
    addType(new DateType());
    addType(new DoubleType());
    addType(new ByteArrayType());
    addType(new SerializableType());
  }

  public DefaultVariableTypes addType(Type type) {
    typesList.add(type);
    typesMap.put(type.getTypeName(), type);
    return this;
  }

  public void setTypesList(List<Type> typesList) {
    this.typesList.clear();
    this.typesList.addAll(typesList);
    this.typesMap.clear();
    for (Type type : typesList) {
      typesMap.put(type.getTypeName(), type);
    }
  }

  public Type getVariableType(String typeName) {
    Type type = typesMap.get(typeName);
    if (type == null) {
      throw new ActivitiException("unknown variable type name " + typeName);
    }
    return type;
  }

  public Type findVariableType(Object value) {
    for (Type type : typesList) {
      if (type.isAbleToStore(value)) {
        return type;
      }
    }
    throw new ActivitiException("couldn't find type for " + value);
  }
}
