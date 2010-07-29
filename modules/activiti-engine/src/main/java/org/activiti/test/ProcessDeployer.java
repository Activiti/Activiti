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

package org.activiti.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.Deployment;
import org.activiti.engine.DeploymentBuilder;
import org.activiti.impl.bpmn.BpmnDeployer;
import org.junit.runners.model.FrameworkMethod;

/**
 * <p>
 * a JUnit &#64;Rule that can be used to deploy and automatically undeploy
 * processes. The easiest way to use it is to simply declare it as a rule and
 * then use the {@link Deployment} annotation on your test method. For
 * example:
 * </p>
 * 
 * <pre>
 * &#64;Rule
 * public ProcessDeployer deployer = new ProcessDeployer();
 * 
 * &#64;Test
 * &#64;ProcessDeclared
 * public void testTaskAssignee() {    
 *   ...
 * }
 * </pre>
 * 
 * <p>
 * This will load a process definition from a resource on the classpath with a
 * name equal to
 * {@link ProcessDeployer#getBpmnProcessDefinitionResource(Class, String)
 * &lt;className&gt;.&lt;methodName&gt;.bpmn20.xml)}.
 * <p/>
 * 
 * <p>
 * The annotation can also be used to specify a list of resources explicitly.
 * See the {@link Deployment javadocs} for more information.
 * <p/>
 * 
 * <p>
 * An alternative to the annotation, giving more control over the deployment, is
 * the {@link #createDeployment()} method. This can be used to create a builder
 * that registers its deployment with the test case, so that it can be
 * automatically cleaned up after the test method completes.
 * </p>
 * 
 * @see Deployment
 * 
 * @author Dave Syer
 */
public class ProcessDeployer extends ProcessEngineTestWatchman {

  private static Logger log = Logger.getLogger(ProcessDeployer.class.getName());

  private Set<String> registeredDeploymentIds = new HashSet<String>();

  /**
   * get a resource location by convention based on a class (type) and a
   * relative resource name. The return value will be the full classpath
   * location of the type, plus a suffix built from the name parameter:
   * <code>.&lt;name&gt;.bpmn20.xml</code>.
   */
  public static String getBpmnProcessDefinitionResource(Class< ? > type, String name) {
    return type.getName().replace('.', '/') + "." + name + "." + BpmnDeployer.BPMN_RESOURCE_SUFFIX;
  }

  @Override
  public void starting(FrameworkMethod method) {

    super.starting(method);

//    Deployment process = method.getAnnotation(Deployment.class);
//    if (process != null) {
//      String[] resources = process.resources();
//      resources = resources.length == 0 ? process.value() : resources;
//      if (resources.length == 0) {
//        String name = method.getName();
//        String resource = ProcessDeployer.getBpmnProcessDefinitionResource(method.getMethod().getDeclaringClass(), name);
//        log.fine("deploying bpmn process resource: " + resource);
//        createDeployment().name(resource).addClasspathResource(resource).deploy();
//      } else {
//        DeploymentBuilder builder = getRepositoryService().createDeployment();
//        for (String resource : resources) {
//          if (resource.startsWith("/")) {
//            resource = resource.substring(1);
//          } else {
//            resource = getProcessDefinitionResource(method.getMethod().getDeclaringClass(), resource);
//          }
//          builder.addClasspathResource(resource);
//        }
//        Deployment deployment = builder.deploy();
//        registerDeployment(deployment.getId());
//      }
//    }

  }

  @Override
  public void finished(FrameworkMethod method) {
    for (String deploymentId : registeredDeploymentIds) {
      getRepositoryService().deleteDeployment(deploymentId);
    }
    super.finished(method);
  }

  /**
   * create a {@link DeploymentBuilder} that is aware of the test context. All
   * deployments built this way in a test case will be automatically removed
   * from the engine when the test method finishes.
   */
  public DeploymentBuilder createDeployment() {
    final DeploymentBuilder builder = getRepositoryService().createDeployment();
    return getDeploymentBuilderProxy(builder);
  }

  /**
   * convenience method for deploying a string literal (XML) as a process
   * definition. It will be deployed with the name
   * <code>xmlString.bpmn20.xml</code>
   */
  public void deployProcessString(String xmlString) {
    String resourceName = "xmlString." + createKey(xmlString.getBytes()) + BpmnDeployer.BPMN_RESOURCE_SUFFIX;
    createDeployment().name(resourceName).addString(resourceName, xmlString).deploy();
  }

  private String createKey(byte[] bytes) {
    if (bytes == null) {
      return "";
    }
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
    }
    bytes = digest.digest(bytes);
    return String.format("%032x", new BigInteger(1, bytes));
  }

  private DeploymentBuilder getDeploymentBuilderProxy(final DeploymentBuilder builder) {
    return (DeploymentBuilder) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class< ? >[] { DeploymentBuilder.class },
            new DeploymentBuilderInvoker(builder));
  }

  /**
   * Registers the given deployment for post-test clean up. All the related data
   * such as process instances, tasks, etc will be deleted when the test case
   * has run.
   */
  private void registerDeployment(String deploymentId) {
    if (deploymentId == null) { // common error
      throw new ActivitiException("Trying to add a deploymentid which is null." + "This is not possible and probably due to not using a resource name "
              + "with a recognized extension.");
    }
    registeredDeploymentIds.add(deploymentId);
  }

  private static String getProcessDefinitionResource(Class< ? > type, String name) {
    String path = type.getName();
    path = path.substring(0, path.lastIndexOf(type.getSimpleName()) - 1);
    System.err.println(path);
    return path.replace('.', '/') + "/" + name;
  }

  /**
   * Method invoker wrapping a {@link DeploymentBuilder} and intercepting the
   * <code>deploy</code> method to register the deployment for automatic removal
   * after a test.
   */
  private final class DeploymentBuilderInvoker implements InvocationHandler {

    private final DeploymentBuilder builder;

    private DeploymentBuilderInvoker(DeploymentBuilder builder) {
      this.builder = builder;
    }
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String name = method.getName();
      if (name.equals("deploy")) {
        Deployment deployment = builder.deploy();
        registerDeployment(deployment.getId());
        return deployment;
      }
      Object result = method.invoke(builder, args);
      if (result instanceof DeploymentBuilder) {
        return getDeploymentBuilderProxy(builder);
      }
      return result;
    }
  }

}