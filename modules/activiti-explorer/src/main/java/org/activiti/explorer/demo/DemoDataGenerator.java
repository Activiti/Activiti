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

package org.activiti.explorer.demo;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Joram Barrez
 */
public class DemoDataGenerator implements ModelDataJsonConstants {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(DemoDataGenerator.class);

  protected ProcessEngine processEngine;
  protected IdentityService identityService;
  protected RepositoryService repositoryService;
  
  protected boolean createDemoUsersAndGroups;
  protected boolean createDemoProcessDefinitions;
  protected boolean createDemoModels;
  
  public void init() {
    this.identityService = processEngine.getIdentityService();
    this.repositoryService = processEngine.getRepositoryService();
    
    if (createDemoUsersAndGroups) {
      initDemoGroups();
      initDemoUsers();
    }
    
    if (createDemoProcessDefinitions) {
      initProcessDefinitions();
    }
    
    if (createDemoModels) {
      initModelData();
    }
  }
  
  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
  
  public void setCreateDemoUsersAndGroups(boolean createDemoUsersAndGroups) {
    this.createDemoUsersAndGroups = createDemoUsersAndGroups;
  }

  public void setCreateDemoProcessDefinitions(boolean createDemoProcessDefinitions) {
    this.createDemoProcessDefinitions = createDemoProcessDefinitions;
  }

  public void setCreateDemoModels(boolean createDemoModels) {
    this.createDemoModels = createDemoModels;
  }

  protected void initDemoGroups() {
    String[] assignmentGroups = new String[] {"management", "sales", "marketing", "engineering"};
    for (String groupId : assignmentGroups) {
      createGroup(groupId, "assignment");
    }
    
    String[] securityGroups = new String[] {"user", "admin"}; 
    for (String groupId : securityGroups) {
      createGroup(groupId, "security-role");
    }
  }
  
  protected void createGroup(String groupId, String type) {
    if (identityService.createGroupQuery().groupId(groupId).count() == 0) {
      Group newGroup = identityService.newGroup(groupId);
      newGroup.setName(groupId.substring(0, 1).toUpperCase() + groupId.substring(1));
      newGroup.setType(type);
      identityService.saveGroup(newGroup);
    }
  }

  protected void initDemoUsers() {
    createUser("kermit", "Kermit", "The Frog", "kermit", "kermit@activiti.org", 
            "org/activiti/explorer/images/kermit.jpg",
            Arrays.asList("management", "sales", "marketing", "engineering", "user", "admin"),
            Arrays.asList("birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord",
                          "phone", "+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog"));
    
    createUser("gonzo", "Gonzo", "The Great", "gonzo", "gonzo@activiti.org", 
            "org/activiti/explorer/images/gonzo.jpg",
            Arrays.asList("management", "sales", "marketing", "user"),
            null);
    createUser("fozzie", "Fozzie", "Bear", "fozzie", "fozzie@activiti.org", 
            "org/activiti/explorer/images/fozzie.jpg",
            Arrays.asList("marketing", "engineering", "user"),
            null);
  }
  
  protected void createUser(String userId, String firstName, String lastName, String password, 
          String email, String imageResource, List<String> groups, List<String> userInfo) {
    
    if (identityService.createUserQuery().userId(userId).count() == 0) {
      
      // Following data can already be set by demo setup script
      
      User user = identityService.newUser(userId);
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setPassword(password);
      user.setEmail(email);
      identityService.saveUser(user);
      
      if (groups != null) {
        for (String group : groups) {
          identityService.createMembership(userId, group);
        }
      }
    }
    
    // Following data is not set by demo setup script
      
    // image
    if (imageResource != null) {
      byte[] pictureBytes = IoUtil.readInputStream(this.getClass().getClassLoader().getResourceAsStream(imageResource), null);
      Picture picture = new Picture(pictureBytes, "image/jpeg");
      identityService.setUserPicture(userId, picture);
    }
      
    // user info
    if (userInfo != null) {
      for(int i=0; i<userInfo.size(); i+=2) {
        identityService.setUserInfo(userId, userInfo.get(i), userInfo.get(i+1));
      }
    }
    
  }
  
  protected void initProcessDefinitions() {
    
    List<Deployment> deploymentList = repositoryService.createDeploymentQuery().deploymentName("Demo processes").list();
    
    if (deploymentList == null || deploymentList.size() == 0) {
      repositoryService.createDeployment()
        .name("Demo processes")
        .addClasspathResource("org/activiti/explorer/demo/process/createTimersProcess.bpmn20.xml")
        .addClasspathResource("org/activiti/explorer/demo/process/VacationRequest.bpmn20.xml")
        .addClasspathResource("org/activiti/explorer/demo/process/VacationRequest.png")
        .addClasspathResource("org/activiti/explorer/demo/process/FixSystemFailureProcess.bpmn20.xml")
        .addClasspathResource("org/activiti/explorer/demo/process/FixSystemFailureProcess.png")
        .addClasspathResource("org/activiti/explorer/demo/process/Helpdesk.bpmn20.xml")
        .addClasspathResource("org/activiti/explorer/demo/process/Helpdesk.png")
        .addClasspathResource("org/activiti/explorer/demo/process/reviewSalesLead.bpmn20.xml")
        .deploy();
    }
  }
  
  protected void initModelData() {
    createModelData("Demo model", "This is a demo model", "org/activiti/explorer/demo/model/test.model.json");
  }
  
  protected void createModelData(String name, String description, String jsonFile) {
    List<Model> modelList = repositoryService.createModelQuery().modelName("Demo model").list();
    
    if (modelList == null || modelList.size() == 0) {
    
      Model model = repositoryService.newModel();
      model.setName(name);
      
      ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
      modelObjectNode.put(MODEL_NAME, name);
      modelObjectNode.put(MODEL_DESCRIPTION, description);
      model.setMetaInfo(modelObjectNode.toString());
      
      repositoryService.saveModel(model);
      
      try {
        InputStream svgStream = this.getClass().getClassLoader().getResourceAsStream("org/activiti/explorer/demo/model/test.svg");
        repositoryService.addModelEditorSourceExtra(model.getId(), IOUtils.toByteArray(svgStream));
      } catch(Exception e) {
        LOGGER.warn("Failed to read SVG", e);
      }
      
      try {
        InputStream editorJsonStream = this.getClass().getClassLoader().getResourceAsStream(jsonFile);
        repositoryService.addModelEditorSource(model.getId(), IOUtils.toByteArray(editorJsonStream));
      } catch(Exception e) {
        LOGGER.warn("Failed to read editor JSON", e);
      }
    }
  }

}
