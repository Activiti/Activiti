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
package org.activiti.explorer.servlet;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.task.Task;

/**
 * @author Joram Barrez
 */
public class BootProcessEngineContextListener implements ServletContextListener {
  
  protected static final Logger LOGGER = Logger.getLogger(BootProcessEngineContextListener.class.getName());

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ProcessEngines.init();
    
    if (ProcessEngines.getDefaultProcessEngine() == null) {
      throw new ActivitiException("Could not construct a process engine. " 
              + "Please verify that your activiti.cfg.xml configuration is correct.");
    }
    
    initDemoData();
  }
  
  protected void initDemoData() {
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    
    initKermit(processEngine);
    initTasks(processEngine);
  }

  protected void initKermit(ProcessEngine processEngine) {
    // Create Kermit demo user
    IdentityService identityService = processEngine.getIdentityService();
    User kermit = identityService.newUser("kermit");
    kermit.setEmail("kermit@muppets.com");
    kermit.setFirstName("Kermit");
    kermit.setLastName("The Frog");
    kermit.setPassword("kermit");
    identityService.saveUser(kermit);
    
    // Additional details
    identityService.setUserInfo("kermit", "birthDate", "01/01/1955");
    identityService.setUserInfo("kermit", "jobTitle", "Activiti core mascot");
    identityService.setUserInfo("kermit", "location", "Muppetland");
    identityService.setUserInfo("kermit", "phone", "+1312323424");
    identityService.setUserInfo("kermit", "twitterName", "kermit007");
    identityService.setUserInfo("kermit", "skype", "kermit.frog");
    
    // Accounts
    identityService.setUserAccount("kermit", "kermit", "google", "kermit.frog@gmail.com", "kermit123", null);
    identityService.setUserAccount("kermit", "kermit", "alfresco", "kermit_alf", "kermit_alf_123", null);
    
    // Picture
    byte[] pictureBytes = IoUtil.readInputStream(this.getClass().getClassLoader().getResourceAsStream("org/activiti/explorer/images/kermit.jpg"), null);
    Picture picture = new Picture(pictureBytes, "image/jpg");
    identityService.setUserPicture("kermit", picture);
  }
  
  protected void initTasks(ProcessEngine processEngine) {
    TaskService taskService = processEngine.getTaskService();
    for (int i=0; i<100; i++) {
      Task task = taskService.newTask();
      task.setAssignee("kermit");
      task.setDescription("This is task nr " + i);
      task.setName("Task [" + i + "]");
      task.setPriority(Task.PRIORITY_NORMAL);
      taskService.saveTask(task);
    }
  }

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ProcessEngines.destroy();
  }

}