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
package org.activiti.test.ldap;

import java.util.Date;

import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.activiti.engine.test.Deployment;
import org.activiti.ldap.LDAPGroupCache;
import org.activiti.ldap.LDAPGroupCache.LDAPGroupCacheListener;
import org.activiti.ldap.LDAPGroupManagerFactory;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:activiti-context-ldap-group-cache.xml")
public class LdapGroupCacheTest extends LDAPTestCase {
  
  protected TestLDAPGroupCacheListener cacheListener;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    // Set test cache listener
    LDAPGroupManagerFactory ldapGroupManagerFactory = 
            (LDAPGroupManagerFactory) processEngineConfiguration.getSessionFactories().get(GroupIdentityManager.class);
    LDAPGroupCache ldapGroupCache = ldapGroupManagerFactory.getLdapGroupCache();
    ldapGroupCache.clear();
    
    cacheListener = new TestLDAPGroupCacheListener();
    ldapGroupCache.setLdapCacheListener(cacheListener);
    
    
  }
  
  @Deployment
  public void testLdapGroupCacheUsage() {
    runtimeService.startProcessInstanceByKey("testLdapGroupCache");
    
    // First task is for Kermit -> cache miss
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser("kermit").count());
    assertEquals("kermit", cacheListener.getLastCacheMiss());
    
    // Second task is for Pepe -> cache miss
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser("pepe").count());
    assertEquals("pepe", cacheListener.getLastCacheMiss());
    
    // Third task is again for kermit -> cache hit
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser("kermit").count());
    assertEquals("kermit", cacheListener.getLastCacheHit());
    
    // Foruth task is for fozzie -> cache miss + cache eviction of pepe (LRU)
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser("fozzie").count());
    assertEquals("fozzie", cacheListener.getLastCacheMiss());
    assertEquals("pepe", cacheListener.getLastCacheEviction());
  }
  
  public void testLdapGroupCacheExpiration() {
    assertEquals(0, taskService.createTaskQuery().taskCandidateUser("kermit").count());
    assertEquals("kermit", cacheListener.getLastCacheMiss());
    
    assertEquals(0, taskService.createTaskQuery().taskCandidateUser("pepe").count());
    assertEquals("pepe", cacheListener.getLastCacheMiss());
    
    assertEquals(0, taskService.createTaskQuery().taskCandidateUser("kermit").count());
    assertEquals("kermit", cacheListener.getLastCacheHit());

    // Test the expiration time of the cache
    Date now = new Date();
    processEngineConfiguration.getClock().setCurrentTime(now);
    
    assertEquals(0, taskService.createTaskQuery().taskCandidateUser("fozzie").count());
    assertEquals("fozzie", cacheListener.getLastCacheMiss());
    assertEquals("pepe", cacheListener.getLastCacheEviction()); 
    
    // Moving the clock forward two 45 minues should trigger cache eviction (configured to 30 mins)
    processEngineConfiguration.getClock().setCurrentTime(new Date(now.getTime() + (45 * 60 * 1000)));
    assertEquals(0, taskService.createTaskQuery().taskCandidateUser("fozzie").count());
    assertEquals("fozzie", cacheListener.getLastCacheExpiration());
    assertEquals("fozzie", cacheListener.getLastCacheEviction());
    assertEquals("fozzie", cacheListener.getLastCacheMiss());
  }
  
  // Test cache listener
  static class TestLDAPGroupCacheListener implements LDAPGroupCacheListener {
    
    protected String lastCacheMiss;
    protected String lastCacheHit;
    protected String lastCacheEviction;
    protected String lastCacheExpiration;

    public void cacheMiss(String userId) {
      this.lastCacheMiss = userId;
    }
    
    public void cacheHit(String userId) {
      this.lastCacheHit = userId;
    }
    
    public void cacheExpired(String userId) {
      this.lastCacheExpiration = userId;
    }
    
    public void cacheEviction(String userId) {
      this.lastCacheEviction = userId;
    }
    
    public String getLastCacheMiss() {
      return lastCacheMiss;
    }
    public void setLastCacheMiss(String lastCacheMiss) {
      this.lastCacheMiss = lastCacheMiss;
    }
    public String getLastCacheHit() {
      return lastCacheHit;
    }
    public void setLastCacheHit(String lastCacheHit) {
      this.lastCacheHit = lastCacheHit;
    }
    public String getLastCacheExpiration() {
      return lastCacheExpiration;
    }
    public void setLastCacheExpiration(String lastCacheExpiration) {
      this.lastCacheExpiration = lastCacheExpiration;
    }
    public String getLastCacheEviction() {
      return lastCacheEviction;
    }
    public void setLastCacheEviction(String lastCacheEviction) {
      this.lastCacheEviction = lastCacheEviction;
    }
    
  }
  
}
