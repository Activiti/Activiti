package org.activiti.cdi.test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.bound.MutableBoundRequest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Baseclass for cdi plugin testcases boots CDI and offers convenience methods
 * for starting/ending requests,conversations etc.
 * 
 * Uses {@link CdiTestEnvironment} by default
 * 
 * Most of this is copied from org.activiti.cdi.test.CdiActivitiTestCase
 * 
 * @author Daniel Meyer
 */
public abstract class CdiPluginTestCase {

  private static Logger logger = Logger.getLogger(CdiPluginTestCase.class.getName());

  protected static CdiTestEnvironment testEnvironment;

  // hide from subclasses
  private HashMap<String, Object> currentRequestMap;
  private HashMap<String, Object> currentSessionMap;

  @BeforeClass
  public static void beforeClass() throws Exception {
    CdiTestSuiteExtension.isPluginTest = true;
    testEnvironment = CdiTestUtils.loadCycleTestEnvironment(CdiTestEnvironment.class);
    testEnvironment.setUpEnvironment();
  }

  @Before
  public void before() throws Exception {
    beginRequest();
    beginSession();
    beginConversation();
  }

  @After
  public void after() throws Exception {
    endConversation();
    endRequest();
    endSession();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    testEnvironment.tearDownEnvironment();
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
      throw new RuntimeException("Cannot start conversation: no request active.");
    }
    if (currentSessionMap == null) {
      throw new RuntimeException("Cannot start conversation: no session active.");
    }
    currentSessionMap = new HashMap<String, Object>();
    startTransientConversation(currentRequestMap, currentSessionMap);
  }

  public void endConversation() {
    if (currentRequestMap == null) {
      throw new RuntimeException("Cannot end conversation: no request active.");
    }
    if (currentSessionMap == null) {
      throw new RuntimeException("Cannot end conversation: no session active.");
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

  protected void endConversationAndBeginNew() {
    endConversation();
    beginConversation();
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
