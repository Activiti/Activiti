package org.activiti.cycle.impl.plugin;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.activiti.cycle.impl.conf.RepositoryConnectorConfiguration;
import org.activiti.cycle.impl.connector.demo.DemoConnectorPluginDefinition;
import org.activiti.cycle.impl.connector.fs.FileSystemPluginDefinition;
import org.activiti.cycle.impl.connector.signavio.SignavioPluginDefinition;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.scannotation.WarUrlFinder;

/**
 * Finder for Activiti Cycle Plugins based on finding annotations. The <a href=
 * "http://bill.burkecentral.com/2008/01/14/scanning-java-annotations-at-runtime/"
 * >Scannotation</a> library from bill Burke is used for that.
 * 
 * @author ruecker
 */
public class PluginFinder {

  protected Logger logger = Logger.getLogger(this.getClass().getName());

  private List<Class< ? extends ActivitiCyclePluginDefinition>> pluginDefinitionRegistry;

  /**
   * Servlet context is needed in wars to access classpath
   */
  private static ServletContext servletContext = null;

  private static PluginFinder instance;

  /**
   * current place to load plugins, should be moved to a correct bootstrapping
   * later
   */
  public static void checkPluginInitialization() {
    if (instance == null) {
      synchronized (PluginFinder.class) {
        if (instance == null) {
          instance = new PluginFinder();
          instance.publishAllPluginsToRegistry();
        }
      }
    }
  }

  /**
   * main method to find all existing plugins in the classpath and register them
   * in the {@link ActivitiCyclePluginRegistry}
   */
  public void publishAllPluginsToRegistry() {
    List<Class< ? extends ActivitiCyclePluginDefinition>> definitions = findPluginDefinitionClasses();
    eliminateOverwrittenPlugins(definitions);

    publishDefinitionsToRegistry(definitions);

    pluginDefinitionRegistry = definitions;
  }

  /**
   * Register the {@link ServletContext} on the {@link PluginFinder}, needed if
   * cycle runs in a Webapp, because there classpath must be retrieved from it
   * and cannot be easily retrieved in another way.
   */
  public static void registerServletContext(ServletContext ctx) {
    servletContext = ctx;
  }

  @SuppressWarnings("unchecked")
  private List<Class< ? extends ActivitiCyclePluginDefinition>> findPluginDefinitionClasses() {
    List<Class< ? extends ActivitiCyclePluginDefinition>> result = new ArrayList<Class< ? extends ActivitiCyclePluginDefinition>>();
    try {
      AnnotationDB db = new AnnotationDB();
      URL[] urls = null;
      if (servletContext == null) {
        urls = ClasspathUrlFinder.findClassPaths();
        logger.log(Level.INFO, "Activiti Cycle Plugin finder uses normal classpath");
      } else {
        urls = WarUrlFinder.findWebInfLibClasspaths(servletContext);
      }
      db.scanArchives(urls);

      Set<String> connectors = db.getAnnotationIndex().get(ActivitiCyclePlugin.class.getName());

      if (connectors == null) {
        // seems we currently have a classloading problem in the webapp (or
        // other environments?)
        logger.log(Level.WARNING, "Couldn't find any Cycle Plugin in the (used " + (servletContext == null ? "normal classpath" : "servlet context classpath")
                + "), use default plugins");
        result.add(SignavioPluginDefinition.class);
        result.add(FileSystemPluginDefinition.class);
        result.add(DemoConnectorPluginDefinition.class);
      } else {
        for (String pluginClass : connectors) {
          Class< ? > cyclePluginClass = this.getClass().getClassLoader().loadClass(pluginClass);
          if (ActivitiCyclePluginDefinition.class.isAssignableFrom(cyclePluginClass)) {
            logger.log(Level.INFO, "Found Activiti Cycle plugin class: " + cyclePluginClass.getName());
            result.add((Class< ? extends ActivitiCyclePluginDefinition>) cyclePluginClass);
          } else {
            logger.log(Level.SEVERE, "Found plugin class with " + ActivitiCyclePlugin.class.getSimpleName() + " annotation but without implementing the "
                    + ActivitiCyclePluginDefinition.class.getSimpleName() + " interface");
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return result;
  }

  /**
   * Remove PluginDefinitions that are overwritten by another one
   */
  private void eliminateOverwrittenPlugins(List<Class< ? extends ActivitiCyclePluginDefinition>> definitions) {
    List<Class< ? extends ActivitiCyclePluginDefinition>> definitionsForRemoval = new ArrayList<Class< ? extends ActivitiCyclePluginDefinition>>();
    for (Class< ? extends ActivitiCyclePluginDefinition> pluginClass1 : definitions) {
      for (Class< ? extends ActivitiCyclePluginDefinition> pluginClass2 : definitions) {
        if (pluginClass1 != pluginClass2 && pluginClass1.isAssignableFrom(pluginClass2)) {
          logger.log(Level.INFO, "remove plugin definition " + pluginClass1.getName() + " because it was overwritten by " + pluginClass2.getName());
          definitionsForRemoval.add(pluginClass1);
        }
      }
    }

    for (Class< ? extends ActivitiCyclePluginDefinition> pluginClass : definitionsForRemoval) {
      definitions.remove(pluginClass);
    }
  }

  private void publishDefinitionsToRegistry(List<Class< ? extends ActivitiCyclePluginDefinition>> definitions) {
    for (Class< ? extends ActivitiCyclePluginDefinition> pluginDefinitionClass : definitions) {
      try {
        ActivitiCyclePluginDefinition pluginDefinition = pluginDefinitionClass.newInstance();
        RepositoryConnectorConfiguration.addPluginDefinition(pluginDefinition);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error while reading plugin configuration from class " + pluginDefinitionClass, e);
      }
    }
  }

  public static void main(String[] args) {
    new PluginFinder().findPluginDefinitionClasses();
  }

  public static PluginFinder getInstance() {
    checkPluginInitialization();
    return instance;
  }

  public List<Class< ? extends ActivitiCyclePluginDefinition>> getPluginDefinitionRegistry() {
    return pluginDefinitionRegistry;
  }

  public Map<String, String> getAvailableConnectorConfigurations() {
    Map<String, String> result = new HashMap<String, String>();
    for (Class< ? extends ActivitiCyclePluginDefinition> pluginDefinitionClazz : pluginDefinitionRegistry) {
      try {
        ActivitiCyclePluginDefinition definitionInstance = pluginDefinitionClazz.newInstance();
        Class< ? extends RepositoryConnectorConfiguration> configurationClazz = definitionInstance.getRepositoryConnectorConfigurationType();
        String name = configurationClazz.getSimpleName().replace("Configuration", "");
        result.put(name, configurationClazz.getCanonicalName());
      } catch (Exception e) {
        logger.log(Level.WARNING, "Error while getting the configuration for class " + pluginDefinitionClazz, e);
      }
    }
    return result;
  }

}
