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

package org.activiti.engine.impl.context;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionInfoCacheObject;

import java.util.*;

/**



 */
public class Context {

  protected static ThreadLocal<Stack<CommandContext>> commandContextThreadLocal = new ThreadLocal<Stack<CommandContext>>();
  protected static ThreadLocal<Stack<ProcessEngineConfigurationImpl>> processEngineConfigurationStackThreadLocal = new ThreadLocal<Stack<ProcessEngineConfigurationImpl>>();
  protected static ThreadLocal<Stack<TransactionContext>> transactionContextThreadLocal = new ThreadLocal<Stack<TransactionContext>>();
  protected static ThreadLocal<Map<String, ObjectNode>> bpmnOverrideContextThreadLocal = new ThreadLocal<Map<String, ObjectNode>>();

  protected static ResourceBundle.Control resourceBundleControl = new ResourceBundleControl();

  public static CommandContext getCommandContext() {
    Stack<CommandContext> stack = getStack(commandContextThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  public static ActivitiEngineAgenda getAgenda() {
    return getCommandContext().getAgenda();
  }

  public static void setCommandContext(CommandContext commandContext) {
    getStack(commandContextThreadLocal).push(commandContext);
  }

  public static void removeCommandContext() {
    getStack(commandContextThreadLocal).pop();
  }

  public static ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    Stack<ProcessEngineConfigurationImpl> stack = getStack(processEngineConfigurationStackThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  public static void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    getStack(processEngineConfigurationStackThreadLocal).push(processEngineConfiguration);
  }

  public static void removeProcessEngineConfiguration() {
    getStack(processEngineConfigurationStackThreadLocal).pop();
  }

  public static TransactionContext getTransactionContext() {
    Stack<TransactionContext> stack = getStack(transactionContextThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  public static void setTransactionContext(TransactionContext transactionContext) {
    getStack(transactionContextThreadLocal).push(transactionContext);
  }

  public static void removeTransactionContext() {
    getStack(transactionContextThreadLocal).pop();
  }

  protected static <T> Stack<T> getStack(ThreadLocal<Stack<T>> threadLocal) {
    Stack<T> stack = threadLocal.get();
    if (stack == null) {
      stack = new Stack<T>();
      threadLocal.set(stack);
    }
    return stack;
  }

  public static ObjectNode getBpmnOverrideElementProperties(String id, String processDefinitionId) {
    ObjectNode definitionInfoNode = getProcessDefinitionInfoNode(processDefinitionId);
    ObjectNode elementProperties = null;
    if (definitionInfoNode != null) {
      elementProperties = getProcessEngineConfiguration().getDynamicBpmnService().getBpmnElementProperties(id, definitionInfoNode);
    }
    return elementProperties;
  }

  public static ObjectNode getLocalizationElementProperties(String language, String id, String processDefinitionId, boolean useFallback) {
    ObjectNode definitionInfoNode = getProcessDefinitionInfoNode(processDefinitionId);
    ObjectNode localizationProperties = null;
    if (definitionInfoNode != null) {
      if (!useFallback) {
        localizationProperties = getProcessEngineConfiguration().getDynamicBpmnService().getLocalizationElementProperties(
            language, id, definitionInfoNode);

      } else {
        HashSet<Locale> candidateLocales = new LinkedHashSet<Locale>();
        candidateLocales.addAll(resourceBundleControl.getCandidateLocales(id, Locale.forLanguageTag(language)));
        for (Locale locale : candidateLocales) {
          localizationProperties = getProcessEngineConfiguration().getDynamicBpmnService().getLocalizationElementProperties(
              locale.toLanguageTag(), id, definitionInfoNode);

          if (localizationProperties != null) {
            break;
          }
        }
      }
    }
    return localizationProperties;
  }

  public static void removeBpmnOverrideContext() {
    bpmnOverrideContextThreadLocal.remove();
  }

  protected static ObjectNode getProcessDefinitionInfoNode(String processDefinitionId) {
    Map<String, ObjectNode> bpmnOverrideMap = getBpmnOverrideContext();
    if (!bpmnOverrideMap.containsKey(processDefinitionId)) {
      ProcessDefinitionInfoCacheObject cacheObject = getProcessEngineConfiguration().getDeploymentManager()
          .getProcessDefinitionInfoCache()
          .get(processDefinitionId);

      addBpmnOverrideElement(processDefinitionId, cacheObject.getInfoNode());
    }

    return getBpmnOverrideContext().get(processDefinitionId);
  }

  protected static Map<String, ObjectNode> getBpmnOverrideContext() {
    Map<String, ObjectNode> bpmnOverrideMap = bpmnOverrideContextThreadLocal.get();
    if (bpmnOverrideMap == null) {
      bpmnOverrideMap = new HashMap<String, ObjectNode>();
    }
    return bpmnOverrideMap;
  }

  protected static void addBpmnOverrideElement(String id, ObjectNode infoNode) {
    Map<String, ObjectNode> bpmnOverrideMap = bpmnOverrideContextThreadLocal.get();
    if (bpmnOverrideMap == null) {
      bpmnOverrideMap = new HashMap<String, ObjectNode>();
      bpmnOverrideContextThreadLocal.set(bpmnOverrideMap);
    }
    bpmnOverrideMap.put(id, infoNode);
  }

  public static class ResourceBundleControl extends ResourceBundle.Control {
    @Override
    public List<Locale> getCandidateLocales(String baseName, Locale locale) {
      return super.getCandidateLocales(baseName, locale);
    }
  }
}
