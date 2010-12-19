package org.activiti.cycle;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.activiti.cycle.action.Action;
import org.activiti.cycle.action.CreateUrlAction;
import org.activiti.cycle.action.DownloadContentAction;
import org.activiti.cycle.action.ParameterizedAction;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.annotations.ExcludesCycleComponents;
import org.activiti.cycle.annotations.Interceptors;
import org.activiti.cycle.annotations.ListAfterComponents;
import org.activiti.cycle.annotations.ListBeforeComponents;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.CycleComponentInvocationHandler;
import org.activiti.cycle.impl.Interceptor;
import org.activiti.cycle.transform.ContentArtifactTypeTransformation;
import org.activiti.cycle.transform.ContentMimeTypeTransformation;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.scannotation.WarUrlFinder;

/**
 * Factory for cycle components. Components are classes annotated by
 * {@link CycleComponent}.
 * 
 * @author daniel.meyer@camunda.com
 */
public abstract class CycleComponentFactory {

  public static final class CycleComponentDescriptor {

    public final String name;
    public final CycleContextType contextType;
    public final Class< ? > clazz;

    private CycleComponentDescriptor(String name, CycleContextType type, Class< ? > clazz) {
      this.name = name;
      this.contextType = type;
      this.clazz = clazz;
    }
  }

  private Logger logger = Logger.getLogger(CycleComponentFactory.class.getName());

  /**
   * @Singleton instance
   */
  private static CycleComponentFactory INSTANCE;

  private ServletContext servletContext;

  /**
   * all available components by name
   */
  private Map<String, CycleComponentDescriptor> descriptorRegistry = new HashMap<String, CycleComponentFactory.CycleComponentDescriptor>();

  /**
   * map of component types as e.g. {@link Action}, {@link RepositoryConnector}
   * 
   * @see CycleComponentFactory#componentTypes
   */
  private Map<Class< ? >, Set<String>> componentTypeMap = new HashMap<Class< ? >, Set<String>>();

  /**
   * Set of recognized component types. Used as potential keys in
   * {@link #componentTypeMap}
   */
  private Set<Class< ? >> componentTypes = new HashSet<Class< ? >>();

  private boolean initialized = false;

  private void guaranteeInitialization() {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          init();
        }
      }
    }
  }

  private Object getComponentInstance(Class< ? > clazz) {
    Object obj = getComponentInstance(clazz.getCanonicalName());
    return obj;
  }

  private Object getComponentInstance(String name) {
    guaranteeInitialization();
    CycleComponentDescriptor descriptor = descriptorRegistry.get(name);
    if (descriptor == null) {
      throw new RuntimeException("No component with name '" + name + "' found.");
    }
    return getComponentInstance(descriptor);
  }

  private Object getComponentInstance(CycleComponentDescriptor descriptor) {
    guaranteeInitialization();
    if (descriptor == null) {
      throw new IllegalArgumentException("Descriptor must not be null");
    }

    // try to restore from context:
    Object instance = restoreFormContext(descriptor);
    if (instance != null)
      return instance;

    // create new instance
    instance = createInstance(descriptor.name, descriptor.clazz);
    // decorate instance
    instance = decorateInstance(instance, descriptor.clazz);
    // store in context
    storeInContext(instance, descriptor);
    return instance;
  }

  private CycleComponentDescriptor getComponentDescriptor(String name) {
    guaranteeInitialization();
    return descriptorRegistry.get(name);
  }

  private CycleComponentDescriptor getComponentDescriptor(Class< ? > clazz) {
    return getComponentDescriptor(clazz.getCanonicalName());
  }

  private Object restoreFormContext(CycleComponentDescriptor descriptor) {
    switch (descriptor.contextType) {
    case SESSION:
      CycleContext sessionContext = CycleSessionContext.getLocalContext();
      if (sessionContext != null) {
        return sessionContext.get(descriptor.name);
      }
      break;
    case APPLICATION:
      CycleContext applicationContext = CycleApplicationContext.getWrappedContext();
      if (applicationContext != null) {
        return applicationContext.get(descriptor.name);
      }
      break;
    }
    return null;
  }

  private void storeInContext(Object instance, CycleComponentDescriptor descriptor) {
    switch (descriptor.contextType) {
    case SESSION:
      CycleSessionContext.set(descriptor.name, instance);
      break;
    case APPLICATION:
      CycleApplicationContext.set(descriptor.name, instance);
      break;
    }
  }

  private <T> T createInstance(String name, Class<T> type) {
    try {
      return type.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Cannot create instance of component '" + name + "' of type '" + type + "': " + e.getMessage(), e);
    }
  }

  private Object decorateInstance(Object instanceToDecorate, Class< ? > type) {
    Interceptors interceptors = type.getAnnotation(Interceptors.class);
    if (interceptors == null) {
      return instanceToDecorate;
    }
    Class< ? extends Interceptor>[] interceptorClasses = interceptors.value();
    if (interceptorClasses.length == 0) {
      return instanceToDecorate;
    }
    try {
      return decorateInstance(instanceToDecorate, interceptorClasses);
    } catch (Exception e) {
      throw new RuntimeException("Cannot decorate instance '" + instanceToDecorate + "' with decorators: '" + e.getMessage() + "'.", e);
    }
  }
  private Object decorateInstance(Object instanceToDecorate, Class< ? extends Interceptor>[] interceptorClasses) throws Exception {
    Set<Class< ? >> interfaces = new HashSet<Class< ? >>();
    Class< ? > superClass = instanceToDecorate.getClass();
    // collect implemented interfaces of this instance
    while (superClass != null && !superClass.equals(Object.class)) {
      // TODO: cast necessary?
      interfaces.addAll((Collection<Class< ? >>) Arrays.asList(superClass.getInterfaces()));
      superClass = superClass.getSuperclass();
    }

    if (interfaces.size() == 0) {
      throw new RuntimeException("Cannot decorate instance '" + instanceToDecorate + "'. Implements no interfaces.");
    }

    Interceptor[] interceptorInstances = new Interceptor[interceptorClasses.length];
    for (int i = 0; i < interceptorClasses.length; i++) {
      Class< ? extends Interceptor> interceptorClass = interceptorClasses[i];
      Interceptor interceptorInstance = interceptorClass.newInstance();
      interceptorInstances[i] = interceptorInstance;
    }
    InvocationHandler invocationHandler = new CycleComponentInvocationHandler(instanceToDecorate, interceptorInstances);
    // TODO: which classloader do we use?
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return Proxy.newProxyInstance(classLoader, interfaces.toArray(new Class< ? >[0]), invocationHandler);
  }

  private void init() {
    // register component types:
    componentTypes.add(RepositoryConnector.class);
    componentTypes.add(Action.class);
    componentTypes.add(CreateUrlAction.class);
    componentTypes.add(ParameterizedAction.class);
    componentTypes.add(DownloadContentAction.class);
    componentTypes.add(ContentRepresentation.class);
    componentTypes.add(ContentArtifactTypeTransformation.class);
    componentTypes.add(ContentMimeTypeTransformation.class);
    componentTypes.add(MimeType.class);
    componentTypes.add(RepositoryArtifactType.class);

    // scan for components
    scanForComponents();
    initialized = true;
  }

  private void scanForComponents() {
    AnnotationDB db = new AnnotationDB();
    URL[] urls = null;
    if (servletContext == null) {
      urls = ClasspathUrlFinder.findClassPaths();
      logger.log(Level.INFO, "Activiti Cycle Plugin finder uses normal classpath");
    } else {
      urls = WarUrlFinder.findWebInfLibClasspaths(servletContext);
    }
    try {
      db.scanArchives(urls);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Cannot scan for components: " + e.getMessage(), e);
      return;
    }

    // look for cycle components
    Set<String> componentClassNames = db.getAnnotationIndex().get(CycleComponent.class.getName());

    // TODO: check correct classpath handling
    if (componentClassNames == null || componentClassNames.size() == 0) {
      logger.log(Level.WARNING, "Cannot find any Cycle plugins having annotation " + CycleComponent.class.getName());
    } else {
      for (String componentClassName : componentClassNames) {
        Class< ? > cycleComponentClass;
        try {
          cycleComponentClass = this.getClass().getClassLoader().loadClass(componentClassName);
          registerComponent(cycleComponentClass);
        } catch (ClassNotFoundException e) {
          logger.log(Level.WARNING, "Cannot find class for '" + componentClassName + "': " + e.getMessage(), e);
        }
      }
    }
  }

  public static String getComponentName(Class< ? > cycleComponentClass) {
    CycleComponent componentAnnotation = cycleComponentClass.getAnnotation(CycleComponent.class);
    String name = componentAnnotation.value();
    if (name == null) {
      name = componentAnnotation.name();
    }
    if (name == null || name.length() == 0) {
      name = cycleComponentClass.getCanonicalName();
    }
    return name;
  }

  private String registerComponent(Class< ? > cycleComponentClass) {
    String name = getComponentName(cycleComponentClass);
    if (descriptorRegistry.containsKey(name)) {
      logger.log(Level.SEVERE,
              "Cannot register component of type '" + cycleComponentClass + "', name '" + name + "', copmonent of type '" + descriptorRegistry.get(name)
                      + "', has same name.");
    }
    CycleComponent componentAnnotation = cycleComponentClass.getAnnotation(CycleComponent.class);
    // init component descriptor:
    CycleComponentDescriptor componentDescriptor = new CycleComponentDescriptor(name, componentAnnotation.context(), cycleComponentClass);
    descriptorRegistry.put(name, componentDescriptor);

    // put into type-map
    for (Class< ? > componentType : componentTypes) {
      if (componentType.isAssignableFrom(cycleComponentClass)) {
        Set<String> componentNamesForThisTypeList = componentTypeMap.get(componentType);
        if (componentNamesForThisTypeList == null) {
          componentNamesForThisTypeList = new HashSet<String>();
          componentTypeMap.put(componentType, componentNamesForThisTypeList);
        }
        componentNamesForThisTypeList.add(name);
      }
    }
    return name;
  }

  public String[] getComponentsForType(Class< ? > type) {
    Set<String> componentSet = componentTypeMap.get(type);
    if (componentSet == null) {
      return new String[0];
    }
    return componentSet.toArray(new String[componentSet.size()]);
  }

  private static CycleComponentFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (CycleComponentFactory.class) {
        if (INSTANCE == null) {
          INSTANCE = new CycleComponentFactory() {
          };
        }
      }
    }
    return INSTANCE;
  }

  public static void registerServletContext(ServletContext servletContext) {
    getInstance().servletContext = servletContext;
  }

  public static <T> T getCycleComponentInstance(Class< ? > clazz, Class<T> castTo) {
    Object instance = getInstance().getComponentInstance(clazz);
    return castSafely(instance, castTo);
  }

  @SuppressWarnings("unchecked")
  private static <T> T castSafely(Object instance, Class<T> to) {
    if (instance == null)
      return null;
    return (T) instance;
  }

  public static <T> T getCycleComponentInstance(String name, Class<T> castTo) {
    Object instance = getInstance().getComponentInstance(name);
    return castSafely(instance, castTo);
  }

  public static Object getCycleComponentInstance(Class< ? > clazz) {
    return getInstance().getComponentInstance(clazz);
  }

  public static Object getCycleComponentInstance(String name) {
    return getInstance().getComponentInstance(name);
  }

  public static CycleComponentDescriptor getCycleComponentDescriptor(String name) {
    return getInstance().getComponentDescriptor(name);
  }

  public static CycleComponentDescriptor getCycleComponentDescriptor(Class< ? > clazz) {
    return getInstance().getComponentDescriptor(clazz);
  }

  public static String[] getAvailableComponentsForType(Class< ? > type) {
    return getInstance().getComponentsForType(type);
  }

  @SuppressWarnings("unchecked")
  public static <T> Set<Class<T>> getAllImplementations(Class<T> componentType) {
    String[] componentnames = getAvailableComponentsForType(componentType);
    Set<Class<T>> resultSet = new HashSet<Class<T>>();
    for (String string : componentnames) {
      resultSet.add((Class<T>) getCycleComponentDescriptor(string).clazz);
    }
    return resultSet;
  }

  /**
   * Note: Q&D-implementation. Only works with
   * {@link CycleContextType#APPLICATION}-scoped components.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static void removeExcludedComponents(Set components) {
    Set<Object> excludedComponents = new HashSet<Object>();
    for (Object object : components) {
      ExcludesCycleComponents excludesAnnotation = object.getClass().getAnnotation(ExcludesCycleComponents.class);
      if (excludesAnnotation == null) {
        continue;
      }
      String[] componentNames = excludesAnnotation.value();
      if (componentNames != null) {
        for (String componentName : componentNames) {
          Object excludedComponentInstance = getCycleComponentInstance(componentName);
          if (excludedComponentInstance != null) {
            excludedComponents.add(excludedComponentInstance);
          }
        }
      }
      Class[] classes = excludesAnnotation.classes();
      if (classes != null) {
        for (Class excludedComponentClass : classes) {
          Object excludedComponentInstance = getCycleComponentInstance(excludedComponentClass);
          if (object != null) {
            excludedComponents.add(excludedComponentInstance);
          }
        }
      }
    }
    components.removeAll(excludedComponents);
  }

}
