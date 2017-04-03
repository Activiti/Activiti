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
package org.activiti.dmn.engine.test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.dmn.api.DmnDeployment;
import org.activiti.dmn.api.DmnDeploymentBuilder;
import org.activiti.dmn.api.DmnRepositoryService;
import org.activiti.dmn.engine.ActivitiDmnObjectNotFoundException;
import org.activiti.dmn.engine.DmnEngine;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.impl.deployer.ParsedDeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public abstract class DmnTestHelper {

    private static Logger log = LoggerFactory.getLogger(DmnTestHelper.class);

    public static final String EMPTY_LINE = "\n";

    static Map<String, DmnEngine> dmnEngines = new HashMap<String, DmnEngine>();

    // Test annotation support /////////////////////////////////////////////

    public static String annotationDeploymentSetUp(DmnEngine dmnEngine, Class<?> testClass, String methodName) {
        String deploymentId = null;
        Method method = null;
        try {
            method = testClass.getMethod(methodName, (Class<?>[]) null);
        } catch (Exception e) {
            log.warn("Could not get method by reflection. This could happen if you are using @Parameters in combination with annotations.", e);
            return null;
        }
        DmnDeploymentAnnotation deploymentAnnotation = method.getAnnotation(DmnDeploymentAnnotation.class);
        if (deploymentAnnotation != null) {
            log.debug("annotation @Deployment creates deployment for {}.{}", testClass.getSimpleName(), methodName);
            String[] resources = deploymentAnnotation.resources();
            if (resources.length == 0) {
                String name = method.getName();
                String resource = getDmnDecisionResource(testClass, name);
                resources = new String[] { resource };
            }

            DmnDeploymentBuilder deploymentBuilder = dmnEngine.getDmnRepositoryService().createDeployment().name(testClass.getSimpleName() + "." + methodName);

            for (String resource : resources) {
                deploymentBuilder.addClasspathResource(resource);
            }

            deploymentId = deploymentBuilder.deploy().getId();
        }

        return deploymentId;
    }

    public static void annotationDeploymentTearDown(DmnEngine dmnEngine, String deploymentId, Class<?> testClass, String methodName) {
        log.debug("annotation @Deployment deletes deployment for {}.{}", testClass.getSimpleName(), methodName);
        if (deploymentId != null) {
            try {
                dmnEngine.getDmnRepositoryService().deleteDeployment(deploymentId);
            } catch (ActivitiDmnObjectNotFoundException e) {
                // Deployment was already deleted by the test case. Ignore.
            }
        }
    }

    /**
     * get a resource location by convention based on a class (type) and a
     * relative resource name. The return value will be the full classpath
     * location of the type, plus a suffix built from the name parameter:
     * <code>DmnDeployer.DMN_RESOURCE_SUFFIXES</code>. The first resource
     * matching a suffix will be returned.
     */
    public static String getDmnDecisionResource(Class<?> type, String name) {
        for (String suffix : ParsedDeploymentBuilder.DMN_RESOURCE_SUFFIXES) {
            String resource = type.getName().replace('.', '/') + "." + name + "." + suffix;
            InputStream inputStream = DmnTestHelper.class.getClassLoader().getResourceAsStream(resource);
            if (inputStream == null) {
                continue;
            } else {
                return resource;
            }
        }
        return type.getName().replace('.', '/') + "." + name + "." + ParsedDeploymentBuilder.DMN_RESOURCE_SUFFIXES[0];
    }

    // Engine startup and shutdown helpers
    // ///////////////////////////////////////////////////

    public static DmnEngine getDmnEngine(String configurationResource) {
        DmnEngine dmnEngine = dmnEngines.get(configurationResource);
        if (dmnEngine == null) {
            log.debug("==== BUILDING DMN ENGINE ========================================================================");
            dmnEngine = DmnEngineConfiguration.createDmnEngineConfigurationFromResource(configurationResource).setDatabaseSchemaUpdate(DmnEngineConfiguration.DB_SCHEMA_UPDATE_DROP_CREATE).buildDmnEngine();
            log.debug("==== DMN ENGINE CREATED =========================================================================");
            dmnEngines.put(configurationResource, dmnEngine);
        }
        return dmnEngine;
    }

    public static void closeDmnEngines() {
        for (DmnEngine dmnEngine : dmnEngines.values()) {
            dmnEngine.close();
        }
        dmnEngines.clear();
    }

    /**
     * Each test is assumed to clean up all DB content it entered. After a test
     * method executed, this method scans all tables to see if the DB is
     * completely clean. It throws AssertionFailed in case the DB is not clean.
     * If the DB is not clean, it is cleaned by performing a create a drop.
     */
    public static void assertAndEnsureCleanDb(DmnEngine dmnEngine) {
        log.debug("verifying that db is clean after test");
        DmnRepositoryService repositoryService = dmnEngine.getDmnEngineConfiguration().getDmnRepositoryService();
        List<DmnDeployment> deployments = repositoryService.createDeploymentQuery().list();
        if (deployments != null && deployments.isEmpty() == false) {
            throw new AssertionError("DmnDeployments is not empty");
        }
    }

}
