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

package org.activiti.engine.impl.el;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Logger;

import javax.el.ExpressionFactory;

import org.activiti.engine.ActivitiException;


/**
 * Class used to get hold of a {@link ExpressionFactory} that works on EL v1.0 and EL v1.1.<br>
 * 
 * When EL 1.1 is used, the usual <code>newInstance()</code> on {@link ExpressionFactory} is called.<br>
 *  
 * When EL 1.0 is used, instance of factory is created using the same behaviour used in <code>javax.el.FactoryFinder</code>.
 * The implementation class-name of the factory is resolved in the following way:
 * <ul>
 *  <li>Try to find the service definition in the classpath, looks for the file <code>/META-INF/service/el.properties</code>.</li>
 *  <li>Try to read from <code>$java.home/lib/el.properties</code>, where the virtual-machine default can be set</li>
 *  <li>Check if a system property is set with the name <code>javax.el.ExpressionFactory</code>.</li>
 * </ul>
 * 
 * @author Frederik Heremans
 */
public abstract class ExpressionFactoryResolver {
  
  private static final Logger LOG = Logger.getLogger(ExpressionFactoryResolver.class.getName());
  
  public static ExpressionFactory resolveExpressionFactory() {
    ExpressionFactory expressionFactory = null;
    try {
      expressionFactory = ExpressionFactory.newInstance();
      LOG.fine("Resolving ExpressionFactory using EL 1.1");
    } catch(NoSuchMethodError nsme) {
      // Since EL 1.1, newInstance method has been added. If the method doesn't exist, 
      // we have EL 1.0.
      LOG.fine("Resolving ExpressionFactory using EL 1.O");
      expressionFactory = getExpressionFactoryInstance();
    }
    return expressionFactory;
  }
  
  private static ExpressionFactory getExpressionFactoryInstance() {
    ClassLoader classLoader = getClassLoader();
    String serviceId = "META-INF/services/" + ExpressionFactory.class.getName();
    
    // Try to get the class-name from the classpath
    String implClassName = getFactoryClassNameFromClasspath(classLoader, serviceId);
    if(implClassName == null) {
      // Try to get the class-name from JVM
      implClassName = getFactoryClassNameFromJVM(); 
      if(implClassName == null) {
        // Finally, try to get the class-name from system property
        implClassName = getFactoryClassNameFromSystemProps();        
      }
    }
    
    if(implClassName == null) {
      throw new ActivitiException("Failed to resolve an implementation class for javax.el.ExpressionFactory");
    }
    
    return newInstance(implClassName, classLoader);
  }
  

  private static ClassLoader getClassLoader() {
    try {
      return Thread.currentThread().getContextClassLoader();
    } catch(Exception e) {
      // When exception occurs when getting context classloader, we ignore this
      return null;
    }
  }

  private static String getFactoryClassNameFromClasspath(ClassLoader classLoader, String serviceId) {
    try {
      InputStream inputStream=null;
      if (classLoader == null) {
        inputStream=ClassLoader.getSystemResourceAsStream(serviceId);
      } else {
        inputStream=classLoader.getResourceAsStream(serviceId);
      }
      
      if(inputStream!=null ) {
        BufferedReader rd =
            new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String factoryClassName = rd.readLine();
        rd.close();
        if (factoryClassName != null &&
            ! "".equals(factoryClassName)) {
            return factoryClassName;
        }
      }
    } catch( Exception ex ) {
      // Exception is ignored, subsequent steps can result in valid class-name.
    }
    return null;
  }
  
  private static String getFactoryClassNameFromJVM() {
    // Read from $java.home/lib/el.properties
    try {
    String javah=System.getProperty("java.home");
    String configFile = javah + File.separator +
       "lib" + File.separator + "el.properties";
    File f=new File( configFile );
    if( f.exists()) {
       Properties props=new Properties();
       props.load( new FileInputStream(f));
       String factoryClassName = props.getProperty(ExpressionFactory.class.getName());
       if(factoryClassName != null && !"".equals(factoryClassName)) {
         return factoryClassName;
       }
    }
    } catch(Exception ex ) {
      // Exception is ignored, subsequent steps can result in valid class-name.
    }
    return null;
  }
  
  private static String getFactoryClassNameFromSystemProps() {
    try {
      String factoryClassName =
         System.getProperty(ExpressionFactory.class.getName());
      if(factoryClassName!=null && !"".equals(factoryClassName)) {
        return factoryClassName;
      }
    } catch (SecurityException se) {
    }
    return null;
  }
  
  private static ExpressionFactory newInstance(String implClassName, ClassLoader classLoader) {
    Class<?> implClass = null;
      try {
        if (classLoader == null) {
        implClass = Class.forName(implClassName);
        } else {
          implClass = classLoader.loadClass(implClassName);
        }
        return (ExpressionFactory) implClass.newInstance();
      } catch (ClassNotFoundException cnfe) {
        throw new ActivitiException("ExpressionFactory class not found:" + implClassName, cnfe);
      } catch (InstantiationException ie) {
        throw new ActivitiException("Failed to instantiate ExpressionFactory: " + implClassName, ie);
      } catch (IllegalAccessException iae) {
        throw new ActivitiException("Failed to instantiate ExpressionFactory, illegal access: " + implClassName, iae);
      }
  }
}
