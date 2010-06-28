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
package org.activiti.impl.tx;

import java.lang.reflect.Constructor;

import org.activiti.ActivitiException;
import org.activiti.impl.interceptor.CommandContext;


/**
 * @author Tom Baeyens
 */
public class TypeDescriptor implements Descriptor {
  
  String typeName;

  public Object getObject(CommandContext commandContext, TransactionalObjectFactory transactionalObjectFactory) {
    try {
      Constructor< ? > constructor = getType().getDeclaredConstructor(parameterTypes);    
      return constructor.newInstance(new Object[]{commandContext});
    } catch (Exception e) {
      throw new ActivitiException("couldn't instantiate class "+typeName, e);
    }
  }

  static Class<?>[] parameterTypes = new Class<?>[]{CommandContext.class};
  public Class< ? > getType() {
    try {
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      return Class.forName(typeName, true, contextClassLoader);
    } catch (Exception e) {
      throw new ActivitiException("couldn't load class "+typeName, e);
    }
  }

  public String getTypeName() {
    return typeName;
  }
  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }
}
