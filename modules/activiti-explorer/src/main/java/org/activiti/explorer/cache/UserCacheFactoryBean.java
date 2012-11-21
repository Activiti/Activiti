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

package org.activiti.explorer.cache;

import java.util.Collections;
import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.explorer.Environments;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Factory bean for creating the user-cache.
 * 
 * @author Frederik Heremans
 */
public class UserCacheFactoryBean implements FactoryBean<UserCache>, ApplicationContextAware {

  protected ApplicationContext applicationContext;
  
  protected String environment;
  
  protected UserCache userCache = null;
  
  public UserCache getObject() throws Exception {
    if(userCache == null) {
      initUserCache();
    }
    return userCache;
  }

  private void initUserCache() {
    if (environment.equals(Environments.ALFRESCO)) {
      userCache = new DummyUserCache(); // User cache is not needed in Alfresco since we hook in Alfresco's ID mgmt
    } else {
      userCache = new TrieBasedUserCache();
      
      // indentityService is not injected in this factory bean because 
      // we don't want to expose it in Alfresco context.xml. So we inject it manually here.
      ((TrieBasedUserCache)userCache).setIdentityService(applicationContext.getBean(IdentityService.class));
    }
  }

  public Class< ? > getObjectType() {
    return UserCache.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
  
  public void setEnvironment(String environment) {
    this.environment = environment;
  }
  
  private class DummyUserCache implements UserCache {

    public List<User> findMatchingUsers(String prefix) {
      return Collections.emptyList();
    }

    public User findUser(String userId) {
      return null;
    }

    public void notifyUserDataChanged(String userId) {
    }
    
  }

}
