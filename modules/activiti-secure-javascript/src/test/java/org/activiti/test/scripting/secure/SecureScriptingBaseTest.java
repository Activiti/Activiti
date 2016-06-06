package org.activiti.test.scripting.secure;

import java.util.Arrays;
import java.util.HashSet;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.impl.scripting.secure.SecureJavascriptTaskActivityBehaviorConfigurator;
import org.activiti.impl.scripting.secure.behavior.SecureJavascriptCapableActivityBehaviorFactory;
import org.activiti.impl.scripting.secure.rhino.SecureScriptClassShutter;
import org.junit.After;
import org.junit.Before;

/**
 * @author Joram Barrez
 */
public abstract class SecureScriptingBaseTest {

    protected ProcessEngine processEngine;
    protected RuntimeService runtimeService;
    protected RepositoryService repositoryService;
    protected TaskService taskService;

    @Before
    public void initProcessEngine() {

        SecureJavascriptTaskActivityBehaviorConfigurator configurator = new SecureJavascriptTaskActivityBehaviorConfigurator()
                .setWhiteListedClasses(new HashSet<String>(Arrays.asList("java.util.ArrayList")))
                .setMaxStackDepth(10)
                .setMaxScriptExecutionTime(3000L)
                .setMaxMemoryUsed(3145728L);

        this.processEngine = new StandaloneInMemProcessEngineConfiguration()
                .addConfigurator(configurator)
                .setDatabaseSchemaUpdate("create-drop")
                .buildProcessEngine();

        this.runtimeService = processEngine.getRuntimeService();
        this.repositoryService = processEngine.getRepositoryService();
        this.taskService = processEngine.getTaskService();
    }

    @After
    public void shutdownProcessEngine() {

        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }

        this.taskService = null;
        this.repositoryService = null;
        this.runtimeService = null;

        this.processEngine.close();
        this.processEngine = null;
    }

    protected void deployProcessDefinition(String classpathResource) {
        repositoryService.createDeployment().addClasspathResource(classpathResource).deploy();
    }

    protected void enableSysoutsInScript() {
        addWhiteListedClass("java.lang.System");
        addWhiteListedClass("java.io.PrintStream");
    }

    protected void addWhiteListedClass(String whiteListedClass) {
        SecureScriptClassShutter secureScriptClassShutter =
                SecureJavascriptCapableActivityBehaviorFactory.getSecureScriptContextFactory().getClassShutter();
        secureScriptClassShutter.addWhiteListedClass(whiteListedClass);
    }

}
