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
package org.activiti.engine.impl.util;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;


/**
 * @author Tom Baeyens
 */
public abstract class ReflectUtil {

  private static final Logger LOG = Logger.getLogger(ReflectUtil.class.getName());
  
  public static ClassLoader getClassLoader() {
    ClassLoader loader = getCustomClassLoader();
    if(loader == null) {
      loader = Thread.currentThread().getContextClassLoader();
    }
    return loader;
  }
  
  public static Class<?> loadClass(String className) {
   Class<?> clazz = null;
   ClassLoader classLoader = getCustomClassLoader();
   if(classLoader != null) {
     LOG.finest("Trying to load class with custom classloader: " + className);
     clazz = loadClassSilent(className, classLoader);
   }
   
   if(clazz == null) {
     // Try the current Thread context classloader
     LOG.finest("Trying to load class with current thread context classloader: " + className);
     classLoader = Thread.currentThread().getContextClassLoader();
     clazz = loadClassSilent(className, classLoader);
     if(clazz == null) {
       // Finally, try the classloader for this class
       LOG.finest("Trying to load class with local classloader: " + className);
       classLoader = ReflectUtil.class.getClassLoader();
       clazz = loadClassSilent(className, classLoader);
     }
   }
  
   if(clazz == null) {
     throw new ActivitiException("Couldn't load class "+className);     
   }
   return clazz;
  }
  
  public static InputStream getResourceAsStream(String name) {
    InputStream resourceStream = null;
    ClassLoader classLoader = getCustomClassLoader();
    if(classLoader != null) {
      resourceStream = classLoader.getResourceAsStream(name);
    }
    
    if(resourceStream == null) {
      // Try the current Thread context classloader
      classLoader = Thread.currentThread().getContextClassLoader();
      resourceStream = classLoader.getResourceAsStream(name);
      if(resourceStream == null) {
        // Finally, try the classloader for this class
        classLoader = ReflectUtil.class.getClassLoader();
        resourceStream = classLoader.getResourceAsStream(name);
      }
    }
    return resourceStream;
   }
  
  public static URL getResource(String name) {
    URL url = null;
    ClassLoader classLoader = getCustomClassLoader();
    if(classLoader != null) {
      url = classLoader.getResource(name);
    }
    if(url == null) {
      // Try the current Thread context classloader
      classLoader = Thread.currentThread().getContextClassLoader();
      url = classLoader.getResource(name);
      if(url == null) {
        // Finally, try the classloader for this class
        classLoader = ReflectUtil.class.getClassLoader();
        url = classLoader.getResource(name);
      }
    }
   
    return url;
   }

  public static Object instantiate(String className) {
    try {
      Class< ? > clazz = loadClass(className);
      return clazz.newInstance();
    } catch (Exception e) {
      throw new ActivitiException("couldn't instantiate class "+className, e);
    }
  }

  public static Object invoke(Object target, String methodName, Object[] args) {
    try {
      Class<? extends Object> clazz = target.getClass();
      Method method = findMethod(clazz, methodName, args);
      method.setAccessible(true);
      return method.invoke(target, args);
    } catch (Exception e) {
      throw new ActivitiException("couldn't invoke "+methodName+" on "+target, e);
    }
  }
  
  /**
   * Returns the field of the given object or null if it doesnt exist.
   */
  public static Field getField(String fieldName, Object object) {
    return getField(fieldName, object.getClass());
  }
  
  /**
   * Returns the field of the given class or null if it doesnt exist.
   */
  public static Field getField(String fieldName, Class<?> clazz) {
    Field field = null;
    try {
      field = clazz.getDeclaredField(fieldName);
    } catch (SecurityException e) {
      throw new ActivitiException("not allowed to access field " + field + " on class " + clazz.getCanonicalName());
    } catch (NoSuchFieldException e) {
      // for some reason getDeclaredFields doesnt search superclasses
      // (which getFields() does ... but that gives only public fields)
      Class<?> superClass = clazz.getSuperclass();
      if (superClass != null) {
        return getField(fieldName, superClass);
      }
    }
    return field;
  }
  
  public static void setField(Field field, Object object, Object value) {
    try {
      field.setAccessible(true);
      field.set(object, value);
    } catch (IllegalArgumentException e) {
      throw new ActivitiException("Could not set field " + field.toString(), e);
    } catch (IllegalAccessException e) {
      throw new ActivitiException("Could not set field " + field.toString(), e);
    }
  }
  
  /**
   * Returns the setter-method for the given field name or null if no setter exists.
   */
  public static Method getSetter(String fieldName, Class<?> clazz, Class<?> fieldType) {
    String setterName = "set" + Character.toTitleCase(fieldName.charAt(0)) +
      fieldName.substring(1, fieldName.length());
    try {
      // Using getMathods(), getMathod(...) expects exact parameter type
      // matching and ignores inheritance-tree.
      Method[] methods = clazz.getMethods();
      for(Method method : methods) {
        if(method.getName().equals(setterName)) {
          Class<?>[] paramTypes = method.getParameterTypes();
          if(paramTypes != null && paramTypes.length == 1 && paramTypes[0].isAssignableFrom(fieldType)) {
            return method;
          }
        }
      }
      return null;
    } catch (SecurityException e) {
      throw new ActivitiException("Not allowed to access method " + setterName + " on class " + clazz.getCanonicalName());
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

  public static Object instantiate(String className, Object[] args) {
    Class< ? > clazz = loadClass(className);
    Constructor< ? > constructor = findMatchingConstructor(clazz, args);
    if (constructor==null) {
      throw new ActivitiException("couldn't find constructor for "+className+" with args "+Arrays.asList(args));
    } 
    try {
      return constructor.newInstance(args);
    } catch (Exception e) {
      throw new ActivitiException("couldn't find constructor for "+className+" with args "+Arrays.asList(args), e);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
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
  
  private static ClassLoader getCustomClassLoader() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if(processEngineConfiguration != null) {
      final ClassLoader classLoader = processEngineConfiguration.getClassLoader();
      if(classLoader != null) {
        return classLoader;
      }
    }
    return null;
  }
  
  private static Class<?> loadClassSilent(String className, ClassLoader classLoader) {
    try {
      return Class.forName(className, true, classLoader);
    } catch (ClassNotFoundException cnfe) {
      // Ignore exception, we return null;
      return null;
    }
  }
}
