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
package org.activiti.form.engine.test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.form.api.FormDeployment;
import org.activiti.form.api.FormDeploymentBuilder;
import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.engine.ActivitiFormObjectNotFoundException;
import org.activiti.form.engine.FormEngine;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.deployer.ParsedDeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public abstract class FormTestHelper {

  private static Logger log = LoggerFactory.getLogger(FormTestHelper.class);

  public static final String EMPTY_LINE = "\n";

  static Map<String, FormEngine> formEngines = new HashMap<String, FormEngine>();

  // Test annotation support /////////////////////////////////////////////

  public static String annotationDeploymentSetUp(FormEngine formEngine, Class<?> testClass, String methodName) {
    String deploymentId = null;
    Method method = null;
    try {
      method = testClass.getMethod(methodName, (Class<?>[]) null);
    } catch (Exception e) {
      log.warn("Could not get method by reflection. This could happen if you are using @Parameters in combination with annotations.", e);
      return null;
    }
    FormDeploymentAnnotation deploymentAnnotation = method.getAnnotation(FormDeploymentAnnotation.class);
    if (deploymentAnnotation != null) {
      log.debug("annotation @Deployment creates deployment for {}.{}", testClass.getSimpleName(), methodName);
      String[] resources = deploymentAnnotation.resources();
      if (resources.length == 0) {
        String name = method.getName();
        String resource = getFormResource(testClass, name);
        resources = new String[] { resource };
      }

      FormDeploymentBuilder deploymentBuilder = formEngine.getFormRepositoryService().createDeployment().name(testClass.getSimpleName() + "." + methodName);

      for (String resource : resources) {
        deploymentBuilder.addClasspathResource(resource);
      }

      deploymentId = deploymentBuilder.deploy().getId();
    }

    return deploymentId;
  }

  public static void annotationDeploymentTearDown(FormEngine formEngine, String deploymentId, Class<?> testClass, String methodName) {
    log.debug("annotation @Deployment deletes deployment for {}.{}", testClass.getSimpleName(), methodName);
    if (deploymentId != null) {
      try {
        formEngine.getFormRepositoryService().deleteDeployment(deploymentId);
      } catch (ActivitiFormObjectNotFoundException e) {
        // Deployment was already deleted by the test case. Ignore.
      }
    }
  }

  /**
   * get a resource location by convention based on a class (type) and a relative resource name. The return value will be the full classpath location of the type, plus a suffix built from the name
   * parameter: <code>DmnDeployer.FORM_RESOURCE_SUFFIXES</code>. The first resource matching a suffix will be returned.
   */
  public static String getFormResource(Class<?> type, String name) {
    for (String suffix : ParsedDeploymentBuilder.FORM_RESOURCE_SUFFIXES) {
      String resource = type.getName().replace('.', '/') + "." + name + "." + suffix;
      InputStream inputStream = FormTestHelper.class.getClassLoader().getResourceAsStream(resource);
      if (inputStream == null) {
        continue;
      } else {
        return resource;
      }
    }
    return type.getName().replace('.', '/') + "." + name + "." + ParsedDeploymentBuilder.FORM_RESOURCE_SUFFIXES[0];
  }

  // Engine startup and shutdown helpers
  // ///////////////////////////////////////////////////

  public static FormEngine getFormEngine(String configurationResource) {
    FormEngine formEngine = formEngines.get(configurationResource);
    if (formEngine == null) {
      log.debug("==== BUILDING FORM ENGINE ========================================================================");
      formEngine = FormEngineConfiguration.createFormEngineConfigurationFromResource(configurationResource)
          .setDatabaseSchemaUpdate(FormEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE)
          .buildFormEngine();
      log.debug("==== FORM ENGINE CREATED =========================================================================");
      formEngines.put(configurationResource, formEngine);
    }
    return formEngine;
  }

  public static void closeFormEngines() {
    for (FormEngine formEngine : formEngines.values()) {
      formEngine.close();
    }
    formEngines.clear();
  }

  /**
   * Each test is assumed to clean up all DB content it entered. After a test method executed, this method scans all tables to see if the DB is completely clean. It throws AssertionFailed in case the
   * DB is not clean. If the DB is not clean, it is cleaned by performing a create a drop.
   */
  public static void assertAndEnsureCleanDb(FormEngine formEngine) {
    log.debug("verifying that db is clean after test");
    FormRepositoryService repositoryService = formEngine.getFormEngineConfiguration().getFormRepositoryService();
    List<FormDeployment> deployments = repositoryService.createDeploymentQuery().list();
    if (deployments != null && deployments.isEmpty() == false) {
      throw new AssertionError("FormDeployments is not empty");
    }
  }

}
