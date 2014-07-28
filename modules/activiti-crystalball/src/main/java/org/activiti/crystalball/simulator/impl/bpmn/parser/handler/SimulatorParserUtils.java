package org.activiti.crystalball.simulator.impl.bpmn.parser.handler;

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


import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.crystalball.simulator.delegate.AbstractSimulationActivityBehavior;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * This class provides basic utilities for other simulation parsers
 *
 * @author martin.grofcik
 */
final class SimulatorParserUtils {
  private static Logger LOG = LoggerFactory.getLogger(SimulatorParserUtils.class);

  /**
   * The namespace of the simulator custom BPMN extensions.
   */
  private static final String SIMULATION_BPMN_EXTENSIONS_NS = "http://crystalball.org/simulation";

  private static final String SIMULATION_BEHAVIOR = "behavior";
  private static final String SIMULATION_CLASS_NAME = "className";

  static void setSimulationBehavior(ScopeImpl scope, BaseElement baseElement) {

    String behaviorClassName = getBehaviorClassName(baseElement);
    if (behaviorClassName != null) {
      ProcessDefinitionImpl processDefinition = scope.getProcessDefinition();
      ActivityImpl activity = processDefinition.findActivity(baseElement.getId());

      LOG.debug("Scripting task [" + activity.getId() + "] setting behavior to [" + behaviorClassName + "]");
      try {
        @SuppressWarnings("unchecked")
        Class<AbstractSimulationActivityBehavior> behaviorClass = (Class<AbstractSimulationActivityBehavior>) Class.forName(behaviorClassName);
        Constructor<AbstractSimulationActivityBehavior> constructor = behaviorClass.getDeclaredConstructor(ScopeImpl.class, ActivityImpl.class);
        activity.setActivityBehavior(constructor.newInstance( scope, activity));
      } catch (Throwable t) {
        LOG.error("unable to set simulation behavior class[" + behaviorClassName + "]", t);
        throw new ActivitiException("unable to set simulation behavior class[" + behaviorClassName + "]");
      }
    }
  }

  private static String getBehaviorClassName(BaseElement baseElement) {
    final Map<String, List<ExtensionElement>> extensionElements = baseElement.getExtensionElements();
    if (extensionElements != null && !extensionElements.isEmpty()) {
      List<ExtensionElement> behaviorExtensionElements = extensionElements.get( SIMULATION_BEHAVIOR);

      if (behaviorExtensionElements != null && !behaviorExtensionElements.isEmpty()) {
        for (ExtensionElement extension : behaviorExtensionElements) {
          if (SIMULATION_BPMN_EXTENSIONS_NS.equals(extension.getNamespace()))
            return extension.getAttributeValue(null, SIMULATION_CLASS_NAME);
        }
      }
    }
    return null;
  }

}
