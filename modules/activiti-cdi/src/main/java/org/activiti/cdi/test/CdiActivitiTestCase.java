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
package org.activiti.cdi.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.BeanManager;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.jboss.weld.Container;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.SessionContext;
import org.jboss.weld.context.beanstore.HashMapBeanStore;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * Abstract base class for executing activiti-cdi tests in a Java SE
 * environment, using Weld-SE.
 * 
 * @author Daniel Meyer
 */
public abstract class CdiActivitiTestCase extends PluggableActivitiTestCase {

  protected Logger logger = Logger.getLogger(getClass().getName());

  protected WeldContainer weldContainer;

  protected BeanManager beanManager;

  protected Weld weld;

  @Override
  protected void setUp() throws Exception {
    // set the process engine in the TestProcessEngineLookup-bean.
    ProcessEngineLookupForTestsuite.processEngine = processEngine;
    // bootstrap the CDI container
    weld = new Weld();
    weldContainer = weld.initialize();

    activateRequestContext();
    activateConversationContext();
    activateSessionContext();

    beanManager = weldContainer.getBeanManager();
    BeanManagerLookup.localInstance = beanManager;
  }

  protected void activateSessionContext() {
    SessionContext sessionContext = Container.instance().services().get(ContextLifecycle.class).getSessionContext();
    sessionContext.setBeanStore(new HashMapBeanStore());
    sessionContext.setActive(true);
  }

  protected void activateConversationContext() {
    ConversationContext conversationContext = getConversationContext();
    conversationContext.setBeanStore(new HashMapBeanStore());
    conversationContext.setActive(true);
  }

  protected void activateRequestContext() {
    RequestContext requestContext = Container.instance().services().get(ContextLifecycle.class).getRequestContext();
    requestContext.setBeanStore(new HashMapBeanStore());
    requestContext.setActive(true);
  }

  @Override
  protected void tearDown() throws Exception {
    weld.shutdown();
  }

  protected void beginConversation() {
    if (getBeanInstance(Conversation.class).isTransient()) {
      getBeanInstance(Conversation.class).begin();
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("---------------------------------------- Started a new Conversation -----------------------");
      }
    }
  }

  protected void endConversation() {
    if (!getBeanInstance(Conversation.class).isTransient()) {
      getBeanInstance(Conversation.class).end();
      getConversationContext().getBeanStore().clear();
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("---------------------------------------- Ended the current Conversation -----------------------");
      }
    }
  }

  protected void beginRequest() {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("     ----------------------------- Started a new Request -----------------------");
    }

  }

  protected void endRequest() {
    getRequestContext().getBeanStore().clear();
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("     ----------------------------- Ended the current Request -----------------------");
    }
  }

  protected void endConversationAndBeginNew() {
    endConversation();
    beginConversation();
  }

  protected void endConversationAndBeginNew(String processInstanceId) {
    endConversation();
    beginConversation();
    getBeanInstance(BusinessProcess.class).resumeProcessById(processInstanceId);
  }

  protected ConversationContext getConversationContext() {
    return Container.instance().services().get(ContextLifecycle.class).getConversationContext();
  }

  protected RequestContext getRequestContext() {
    return Container.instance().services().get(ContextLifecycle.class).getRequestContext();
  }

  protected <T> T getBeanInstance(Class<T> clazz) {
    return ProgrammaticBeanLookup.lookup(clazz);
  }
  
  protected Object getBeanInstance(String name) {
    return ProgrammaticBeanLookup.lookup(name);
  }
}
