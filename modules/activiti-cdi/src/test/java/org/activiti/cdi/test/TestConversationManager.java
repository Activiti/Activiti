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

import java.io.Serializable;
import java.util.UUID;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;

import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.beanstore.HashMapBeanStore;
import org.jboss.weld.conversation.AbstractConversationManager;
import org.jboss.weld.conversation.ConversationConcurrentAccessTimeout;
import org.jboss.weld.conversation.ConversationIdGenerator;
import org.jboss.weld.conversation.ConversationIdName;
import org.jboss.weld.conversation.ConversationInactivityTimeout;

/**
 * ConversationManager for testcases, adopted from
 * org.jboss.weld.conversation.ServletConversationManager
 * 
 * @author Daniel Meyer
 */
@SessionScoped
public class TestConversationManager extends AbstractConversationManager implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final long CONVERSATION_TIMEOUT_IN_MS = 10 * 60 * 1000;
  private static final long CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS = 1 * 1000;
  private static final String CONVERSATION_ID_NAME = "cid";

  @Produces
  @ConversationInactivityTimeout
  public static long getConversationTimeoutInMilliseconds() {
    return CONVERSATION_TIMEOUT_IN_MS;
  }

  @Produces
  @ConversationConcurrentAccessTimeout
  public static long getConversationConcurrentAccessTimeout() {
    return CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS;
  }

  @Produces
  @ConversationIdName
  public static String getConversationIdName() {
    return CONVERSATION_ID_NAME;
  }

  @Override
  protected BeanStore getBeanStore(String cid) {
    return new HashMapBeanStore();
  }

  protected static class UUIDConversationIdGenerator implements ConversationIdGenerator, Serializable {
    private static final long serialVersionUID = 1L;
    public String nextId() {
      return UUID.randomUUID().toString();
    }
  }

}