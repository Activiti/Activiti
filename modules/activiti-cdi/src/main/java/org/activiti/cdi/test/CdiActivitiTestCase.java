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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.bound.MutableBoundRequest;
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

  // hide from subclasses
  private HashMap<String, Object> currentRequestMap;
  private HashMap<String, Object> currentSessionMap;

  @Override
  protected void setUp() throws Exception {
    // set the process engine in the TestProcessEngineLookup-bean.
    ProcessEngineLookupForTestsuite.processEngine = processEngine;
    // bootstrap the CDI container
    weld = new Weld();
    weldContainer = weld.initialize();
    beanManager = weldContainer.getBeanManager();
    BeanManagerLookup.localInstance = beanManager;
    
    beginRequest();
    beginSession();
    beginConversation();    
    
  }
  
  public void beginSession() {
    currentSessionMap = new HashMap<String, Object>();
    beginSession(currentSessionMap);
  }

  public void beginSession(Map<String, Object> sessionDataMap) {    
    BoundSessionContext sessionContext = getSessionContext();
    sessionContext.associate(currentSessionMap);
    sessionContext.activate();
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("     ----------------------------- Started a new Session -----------------------");
    }
  }
  
  public void endSession() {
    endSession(currentSessionMap);
    currentSessionMap = null;
  }

  public void endSession(Map<String, Object> sessionDataMap) {
    BoundSessionContext sessionContext = getSessionContext();
    try {
      sessionContext.invalidate();
      sessionContext.deactivate();
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("     ----------------------------- Ended the current Session -----------------------");
      }
    } finally {
      sessionContext.dissociate(sessionDataMap);
    }
  }

  public void beginRequest() {
    currentRequestMap = new HashMap<String, Object>();
    beginRequest(currentRequestMap);
  }

  public void beginRequest(Map<String, Object> requestDataStore) {
    // Associate the store with the context and acticate the context
    BoundRequestContext requestContext = getRequestContext();
    requestContext.associate(requestDataStore);
    requestContext.activate();
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("     ----------------------------- Started a new Request -----------------------");
    }
  }

  public void endRequest() {
    endRequest(currentRequestMap);
    currentRequestMap = null;
  }

  public void endRequest(Map<String, Object> requestDataStore) {
    BoundRequestContext requestContext = getRequestContext();
    try {
      requestContext.invalidate();
      requestContext.deactivate();
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("     ----------------------------- Ended the current Request -----------------------");
      }
    } finally {
      requestContext.dissociate(requestDataStore);
    }
  }

  public void beginConversation() {
    if (currentRequestMap == null) {
      throw new ActivitiException("Cannot start conversation: no request active.");
    }
    if(currentSessionMap == null) {
      throw new ActivitiException("Cannot start conversation: no session active.");
    }
    currentSessionMap = new HashMap<String, Object>();
    startTransientConversation(currentRequestMap, currentSessionMap);
  }

  public void endConversation() {
    if (currentRequestMap == null) {
      throw new ActivitiException("Cannot end conversation: no request active.");
    }
    if(currentSessionMap == null) {
      throw new ActivitiException("Cannot end conversation: no session active.");
    }
    endOrPassivateConversation(currentRequestMap, currentSessionMap);    
  }

  public void startTransientConversation(Map<String, Object> requestDataStore, Map<String, Object> sessionDataStore) {
    resumeOrStartConversation(requestDataStore, sessionDataStore, null);
  }

  public void resumeOrStartConversation(Map<String, Object> requestDataStore, Map<String, Object> sessionDataStore, String cid) {
    BoundConversationContext conversationContext = getConversationContext();
    conversationContext.associate(new MutableBoundRequest(requestDataStore, sessionDataStore));
    conversationContext.activate(cid);
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("---------------------------------------- Started a new Conversation -----------------------");
    }
  }

  public void endOrPassivateConversation(Map<String, Object> requestDataStore, Map<String, Object> sessionDataStore) {
    BoundConversationContext conversationContext = getConversationContext();
    try {
      conversationContext.invalidate();
      conversationContext.deactivate();
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("---------------------------------------- Ended the current Conversation -----------------------");
      }
    } finally {
      conversationContext.dissociate(new MutableBoundRequest(requestDataStore, sessionDataStore));
    }
  }

  @Override
  protected void tearDown() throws Exception {
    endConversation();
    endRequest();
    endSession();
//   https://issues.jboss.org/browse/WELD-891
    weld.shutdown();    
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

  protected BoundConversationContext getConversationContext() {
    return getBeanInstance(BoundConversationContext.class);
  }

  protected BoundSessionContext getSessionContext() {
    return getBeanInstance(BoundSessionContext.class);
  }

  protected BoundRequestContext getRequestContext() {
    return getBeanInstance(BoundRequestContext.class);
  }

  protected <T> T getBeanInstance(Class<T> clazz) {
    return ProgrammaticBeanLookup.lookup(clazz);
  }

  protected Object getBeanInstance(String name) {
    return ProgrammaticBeanLookup.lookup(name);
  }
}
