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
package org.activiti.impl.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.ActivitiException;
import org.activiti.impl.variable.ByteArrayVariableInstance;
import org.activiti.impl.variable.LongVariableInstance;
import org.activiti.impl.variable.SerializableVariableInstance;
import org.activiti.impl.variable.StringVariableInstance;
import org.activiti.impl.variable.UnpersistableVariableInstance;
import org.activiti.impl.variable.VariableInstance;


/**
 * @author Tom Baeyens
 */
public class TypeSet implements Serializable {
  
  private static final long serialVersionUID = 1L;

  public static TypeSet INSTANCE = new TypeSet();

  protected List<Type> variableTypesList = new ArrayList<Type>();
  protected Map<String, Type> variableTypesMap = new HashMap<String, Type>();
  
  public TypeSet() {
    addType(StringVariableInstance.class);
    addType(LongVariableInstance.class);
    addType(ByteArrayVariableInstance.class);
    addType(SerializableVariableInstance.class);
  }

  public void addType(Class<? extends VariableInstance> typeClass) {
    Type type = new Type(typeClass);
    try {
      variableTypesMap.put(type.getName(), type);
      variableTypesList.add(type);
      
    } catch (Exception e) {
      throw new RuntimeException("couldn't add variable type '"+typeClass.getName()+"' to type set");
    }
  }

  public VariableInstance createVariableInstance(String typeName) {
    Type type = variableTypesMap.get(typeName);
    if (type==null) {
      throw new ActivitiException("unknown variable type name "+typeName);
    }
    return type.createVariableInstance();
  }

  public VariableInstance createVariableInstance(Object value) {
    for (Type type: variableTypesList) {
      if (type.isAbleToStore(value)) {
        return type.createVariableInstance();
      }
    }
    return new UnpersistableVariableInstance();
  }
}
