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
package org.activiti.impl.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;


/**
 * @author Tom Baeyens
 */
public abstract class ReflectUtil {

  public static ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
  
  public static Class<?> loadClass(String className) {
    try {
      ClassLoader classLoader = getClassLoader();
      return Class.forName(className, true, classLoader);
    } catch (Exception e) {
      throw new ActivitiException("couldn't load class "+className, e);
    }
  }

  public static Object instantiate(String className) {
    try {
      Class< ? > clazz = loadClass(className);
      return clazz.newInstance();
    } catch (Exception e) {
      throw new ActivitiException("couldn't instantiate class "+className, e);
    }
  }

  public static void invoke(Object target, String methodName, Object[] args) {
    try {
      Class<? extends Object> clazz = target.getClass();
      Method method = findMethod(clazz, methodName, args);
      method.setAccessible(true);
      method.invoke(target, args);
    } catch (Exception e) {
      throw new ActivitiException("couldn't invoke "+methodName+" on "+target, e);
    }
  }

  private static Method findMethod(Class< ? extends Object> clazz, String methodName, Object[] args) {
    for (Method method : clazz.getDeclaredMethods()) {
      // TODO add parameter matching
      if ( method.getName().equals(methodName)
           && matches(method.getParameterTypes(), args)
         ) {
        return method;
      }
    }
    Class< ? > superClass = clazz.getSuperclass();
    if (superClass!=null) {
      return findMethod(superClass, methodName, args);
    }
    return null;
  }

  public static ProcessEngine instantiate(String className, Object[] args) {
    Class< ? > clazz = loadClass(className);
    Constructor< ? > constructor = findMatchingConstructor(clazz, args);
    if (constructor==null) {
      throw new ActivitiException("couldn't find constructor for "+className+" with args "+Arrays.asList(args));
    } 
    try {
      return (ProcessEngine) constructor.newInstance(args);
    } catch (Exception e) {
      throw new ActivitiException("couldn't find constructor for "+className+" with args "+Arrays.asList(args), e);
    }
  }

  private static <T> Constructor<T> findMatchingConstructor(Class<T> clazz, Object[] args) {
    for (Constructor constructor: clazz.getDeclaredConstructors()) { // cannot use <?> or <T> due to JDK 5/6 incompatibility
      if (matches(constructor.getParameterTypes(), args)) {
        return constructor;
      }
    }
    return null;
  }

  private static boolean matches(Class< ? >[] parameterTypes, Object[] args) {
    if ( (parameterTypes==null)
         || (parameterTypes.length==0)
       ) {
      return ( (args==null)
               || (args.length==0)
             );
    }
    if ( (args==null)
         || (parameterTypes.length!=args.length)
       ) {
      return false;
    }
    for (int i=0; i<parameterTypes.length; i++) {
      if ( (args[i]!=null)
           && (! parameterTypes[i].isAssignableFrom(args[i].getClass()))
         ) {
        return false;
      }
    }
    return true;
  }
}
