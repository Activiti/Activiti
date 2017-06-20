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
package org.activiti.engine.impl.bpmn.parser.factory;

import java.util.HashMap;
import java.util.Map;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EventListener;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.CustomPropertiesResolver;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.TransactionDependentTaskListener;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.bpmn.helper.BaseDelegateEventListener;
import org.activiti.engine.impl.bpmn.helper.ClassDelegateFactory;
import org.activiti.engine.impl.bpmn.helper.DefaultClassDelegateFactory;
import org.activiti.engine.impl.bpmn.helper.DelegateActivitiEventListener;
import org.activiti.engine.impl.bpmn.helper.DelegateExpressionActivitiEventListener;
import org.activiti.engine.impl.bpmn.helper.ErrorThrowingEventListener;
import org.activiti.engine.impl.bpmn.helper.MessageThrowingEventListener;
import org.activiti.engine.impl.bpmn.helper.SignalThrowingEventListener;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionCustomPropertiesResolver;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionExecutionListener;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionTaskListener;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionTransactionDependentTaskListener;
import org.activiti.engine.impl.bpmn.listener.ExpressionCustomPropertiesResolver;
import org.activiti.engine.impl.bpmn.listener.ExpressionExecutionListener;
import org.activiti.engine.impl.bpmn.listener.ExpressionTaskListener;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionTransactionDependentExecutionListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;

/**
 * Default implementation of the {@link ListenerFactory}. Used when no custom {@link ListenerFactory} is injected on the {@link ProcessEngineConfigurationImpl}.
 * 

 */
public class DefaultListenerFactory extends AbstractBehaviorFactory implements ListenerFactory {
  private final ClassDelegateFactory classDelegateFactory;

  public DefaultListenerFactory(ClassDelegateFactory classDelegateFactory) {
    this.classDelegateFactory = classDelegateFactory;
  }

  public DefaultListenerFactory() {
    this(new DefaultClassDelegateFactory());
  }

  public static final Map<String, Class<?>> ENTITY_MAPPING = new HashMap<String, Class<?>>();
  static {
    ENTITY_MAPPING.put("attachment", Attachment.class);
    ENTITY_MAPPING.put("comment", Comment.class);
    ENTITY_MAPPING.put("execution", Execution.class);
    ENTITY_MAPPING.put("identity-link", IdentityLink.class);
    ENTITY_MAPPING.put("job", Job.class);
    ENTITY_MAPPING.put("process-definition", ProcessDefinition.class);
    ENTITY_MAPPING.put("process-instance", ProcessInstance.class);
    ENTITY_MAPPING.put("task", Task.class);
  }

  @Override
  public TaskListener createClassDelegateTaskListener(ActivitiListener activitiListener) {
    return classDelegateFactory.create(activitiListener.getImplementation(),
        createFieldDeclarations(activitiListener.getFieldExtensions()));
  }

  @Override
  public TaskListener createExpressionTaskListener(ActivitiListener activitiListener) {
    return new ExpressionTaskListener(expressionManager.createExpression(activitiListener.getImplementation()));
  }

  @Override
  public TaskListener createDelegateExpressionTaskListener(ActivitiListener activitiListener) {
    return new DelegateExpressionTaskListener(expressionManager.createExpression(activitiListener.getImplementation()), createFieldDeclarations(activitiListener.getFieldExtensions()));
  }

  @Override
  public TransactionDependentTaskListener createTransactionDependentDelegateExpressionTaskListener(ActivitiListener activitiListener) {
    return new DelegateExpressionTransactionDependentTaskListener(expressionManager.createExpression(activitiListener.getImplementation()));
  }

  @Override
  public ExecutionListener createClassDelegateExecutionListener(ActivitiListener activitiListener) {
    return classDelegateFactory.create(activitiListener.getImplementation(), createFieldDeclarations(activitiListener.getFieldExtensions()));
  }

  @Override
  public ExecutionListener createExpressionExecutionListener(ActivitiListener activitiListener) {
    return new ExpressionExecutionListener(expressionManager.createExpression(activitiListener.getImplementation()));
  }

  @Override
  public ExecutionListener createDelegateExpressionExecutionListener(ActivitiListener activitiListener) {
    return new DelegateExpressionExecutionListener(expressionManager.createExpression(activitiListener.getImplementation()), createFieldDeclarations(activitiListener.getFieldExtensions()));
  }

  @Override
  public DelegateExpressionTransactionDependentExecutionListener createTransactionDependentDelegateExpressionExecutionListener(ActivitiListener activitiListener) {
    return new DelegateExpressionTransactionDependentExecutionListener(expressionManager.createExpression(activitiListener.getImplementation()));
  }

  @Override
  public ActivitiEventListener createClassDelegateEventListener(EventListener eventListener) {
    return new DelegateActivitiEventListener(eventListener.getImplementation(), getEntityType(eventListener.getEntityType()));
  }

  @Override
  public ActivitiEventListener createDelegateExpressionEventListener(EventListener eventListener) {
    return new DelegateExpressionActivitiEventListener(expressionManager.createExpression(eventListener.getImplementation()), getEntityType(eventListener.getEntityType()));
  }

  @Override
  public ActivitiEventListener createEventThrowingEventListener(EventListener eventListener) {
    BaseDelegateEventListener result = null;
    if (ImplementationType.IMPLEMENTATION_TYPE_THROW_SIGNAL_EVENT.equals(eventListener.getImplementationType())) {
      result = new SignalThrowingEventListener();
      ((SignalThrowingEventListener) result).setSignalName(eventListener.getImplementation());
      ((SignalThrowingEventListener) result).setProcessInstanceScope(true);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_GLOBAL_SIGNAL_EVENT.equals(eventListener.getImplementationType())) {
      result = new SignalThrowingEventListener();
      ((SignalThrowingEventListener) result).setSignalName(eventListener.getImplementation());
      ((SignalThrowingEventListener) result).setProcessInstanceScope(false);
    } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_MESSAGE_EVENT.equals(eventListener.getImplementationType())) {
      result = new MessageThrowingEventListener();
      ((MessageThrowingEventListener) result).setMessageName(eventListener.getImplementation());
    } else if (ImplementationType.IMPLEMENTATION_TYPE_THROW_ERROR_EVENT.equals(eventListener.getImplementationType())) {
      result = new ErrorThrowingEventListener();
      ((ErrorThrowingEventListener) result).setErrorCode(eventListener.getImplementation());
    }

    if (result == null) {
      throw new ActivitiIllegalArgumentException("Cannot create an event-throwing event-listener, unknown implementation type: " + eventListener.getImplementationType());
    }

    result.setEntityClass(getEntityType(eventListener.getEntityType()));
    return result;
  }

  @Override
  public CustomPropertiesResolver createClassDelegateCustomPropertiesResolver(ActivitiListener activitiListener) {
    return classDelegateFactory.create(activitiListener.getCustomPropertiesResolverImplementation(), null);
  }

  @Override
  public CustomPropertiesResolver createExpressionCustomPropertiesResolver(ActivitiListener activitiListener) {
    return new ExpressionCustomPropertiesResolver(expressionManager.createExpression(activitiListener.getCustomPropertiesResolverImplementation()));
  }

  @Override
  public CustomPropertiesResolver createDelegateExpressionCustomPropertiesResolver(ActivitiListener activitiListener) {
    return new DelegateExpressionCustomPropertiesResolver(expressionManager.createExpression(activitiListener.getCustomPropertiesResolverImplementation()));
  }

  /**
   * @param entityType
   *          the name of the entity
   * @return
   * @throws ActivitiIllegalArgumentException
   *           when the given entity name
   */
  protected Class<?> getEntityType(String entityType) {
    if (entityType != null) {
      Class<?> entityClass = ENTITY_MAPPING.get(entityType.trim());
      if (entityClass == null) {
        throw new ActivitiIllegalArgumentException("Unsupported entity-type for an ActivitiEventListener: " + entityType);
      }
      return entityClass;
    }
    return null;
  }
}
