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

import java.util.logging.Logger;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.LogUtil;


/**
 * Note that this class is not autowired through @Component, 
 * as we want it to be easy to remove the demo data.
 * 
 * @author Joram Barrez
 */
public class DemoDataGenerator {
  
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  protected static final Logger LOGGER = Logger.getLogger(DemoDataGenerator.class.getName());

  protected ProcessEngine processEngine;
  
  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
    init();
  }

  public void init() {
    processEngine.getIdentityService().setAuthenticatedUserId("kermit");
    initDemoData();
  }

  protected void initDemoData() {
    initKermit(processEngine);
    initAlfrescoMembers(processEngine);
    initProcessDefinitions(processEngine);
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
    
    // Assignment Groups
    Group management = identityService.newGroup("management");
    management.setName("Management");
    management.setType("assignment");
    identityService.saveGroup(management);
    
    Group sales = identityService.newGroup("sales");
    sales.setName("Sales");
    sales.setType("assignment");
    identityService.saveGroup(sales);
    
    Group marketing = identityService.newGroup("marketing");
    marketing.setName("Marketing");
    marketing.setType("assignment");
    identityService.saveGroup(marketing);
    
    Group engineering = identityService.newGroup("engineering");
    engineering.setType("assignment");
    engineering.setName("Engineering");
    identityService.saveGroup(engineering);
    
    // Security groups
    
    Group sysAdmin = identityService.newGroup("admin");
    sysAdmin.setType("security-role");
    identityService.saveGroup(sysAdmin);
    
    Group user = identityService.newGroup("user");
    user.setType("security-role");
    identityService.saveGroup(user);
    
    
    // Membership
    identityService.createMembership("kermit", "management");
    identityService.createMembership("kermit", "sales");
    identityService.createMembership("kermit", "marketing");
    identityService.createMembership("kermit", "engineering");
    identityService.createMembership("kermit", "user");
    identityService.createMembership("kermit", "admin");
    
    // Additional details
    identityService.setUserInfo("kermit", "birthDate", "10-10-1955");
    identityService.setUserInfo("kermit", "jobTitle", "Activiti core mascot");
    identityService.setUserInfo("kermit", "location", "Muppetland");
    identityService.setUserInfo("kermit", "phone", "+1312323424");
    identityService.setUserInfo("kermit", "twitterName", "kermit007");
    identityService.setUserInfo("kermit", "skype", "kermit.frog");
    
    // Accounts
    identityService.setUserAccount("kermit", "kermit", "imap", "kermit.frog@gmail.com", "kermit123", null);
    identityService.setUserAccount("kermit", "kermit", "alfresco", "kermit_alf", "kermit_alf_123", null);
    
    // Picture
    byte[] pictureBytes = IoUtil.readInputStream(this.getClass().getClassLoader().getResourceAsStream("org/activiti/explorer/images/kermit.jpg"), null);
    Picture picture = new Picture(pictureBytes, "image/jpeg");
    identityService.setUserPicture("kermit", picture);
    
    // Create other muppets 
    createUser(identityService, "gonzo", "Gonzo", "The Great", "gonzo", "gonzo@muppets.con", null);
    createUser(identityService, "fozzie", "Fozzie", "Bear", "fozzie", "fozzie@muppets.com", "org/activiti/explorer/images/fozzie.jpg");
  }
  
  protected void initAlfrescoMembers(ProcessEngine processEngine) {
    IdentityService identityService = processEngine.getIdentityService();
    createUser(identityService, "joram", "Joram", "Barrez", "joram", "joram.barrez@alfresco.com", "org/activiti/explorer/images/joram.jpg");
    createUser(identityService, "frederik", "Frederik", "Heremans", "frederik", "pluisje@alfresco.com", "org/activiti/explorer/images/fred.jpg");
    createUser(identityService, "tom", "Tom", "Baeyens", "tom", "tom@alfresco.com", "org/activiti/explorer/images/tom.jpg");
    createUser(identityService, "tijs", "Tijs", "Rademakers", "tijs", "tijs.rademakers@alfresco.com", "org/activiti/explorer/images/tijs.jpg");
    createUser(identityService, "linton", "Linton", "Baddeley", "linton", "linton.baddeley@alfresco.com", "org/activiti/explorer/images/linton.jpg");
    
    createUser(identityService, "david", "David", "Caruana", "david", "david.caruana@alfresco.com", "org/activiti/explorer/images/david.jpg");
    createUser(identityService, "gavin", "Gavin", "Cornwell", "gavin", "gavin.cornwell@alfresco.com", "org/activiti/explorer/images/gavin.jpg");
    createUser(identityService, "johnN", "John", "Newton", "johnN", "john.newton@alfresco.com", "org/activiti/explorer/images/john_newton.jpg");
    createUser(identityService, "johnP", "John", "Powell", "johnP", "john.powell@alfresco.com", "org/activiti/explorer/images/john_powell.jpg");
    createUser(identityService, "paul", "Paul", "Holmes-Higgin", "paul", "paulhh@alfresco.com", "org/activiti/explorer/images/paul.jpg");
    createUser(identityService, "julie", "Julie", "Hall", "julie", "julie.hall@alfresco.com", "org/activiti/explorer/images/julie.jpg");
    createUser(identityService, "erik", "Erik", "Winlof", "erik", "erik.witloof@alfresco.com", "org/activiti/explorer/images/erik.jpg");
    
    // Skype
    identityService.setUserInfo("joram", "skype", "joram.barrez");
    identityService.setUserInfo("frederik", "skype", "frederik.heremans");
    identityService.setUserInfo("david", "skype", "david.caruana");
    identityService.setUserInfo("gavin", "skype", "gavincornwell");
    identityService.setUserInfo("johnN", "skype", "john_newton_uk");
    identityService.setUserInfo("johnP", "skype", "john_n_powell");
    identityService.setUserInfo("paul", "skype", "paulhh");
    identityService.setUserInfo("erik", "skype", "wineleaf");
    identityService.setUserInfo("tom", "skype", "tombaeyens");
    
    
    // Joram
    identityService.setUserInfo("joram", "birthDate", "10-10-1985");
    identityService.setUserInfo("joram", "jobTitle", "Activiti core developer");
    identityService.setUserInfo("joram", "location", "Welle, Belgium");
    identityService.setUserInfo("joram", "phone", "+32485869655");
    identityService.setUserInfo("joram", "twitterName", "jbarrez");
    
    // Tim
//    String accountUsername = System.getProperty("user");
//    String accountPassword = System.getProperty("pwd");
//    if (accountUsername == null || accountPassword == null) {
//      throw new RuntimeException("'user' and 'pwd' system property must be set");
//    }
//    Map<String, String> accountDetails = new HashMap<String, String>();
//    accountDetails.put("toDoFolderName", "Cases");
//    accountDetails.put("toDoInActivitiFolderName", "CasesInActiviti");
//    accountDetails.put("imapHost", "imap.gmail.com");
//    accountDetails.put("imapProtocol", "imaps");
//    identityService.setUserAccount("tom", null, "mailscan", accountUsername, accountPassword, accountDetails);
  }
  
  protected void createUser(IdentityService identityService, String userId, String firstName, String lastName, 
          String password, String email, String imageResource) {
    User user = identityService.newUser(userId);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setPassword(password);
    user.setEmail(email);
    identityService.saveUser(user);
    
    if (imageResource != null) {
      byte[] pictureBytes = IoUtil.readInputStream(this.getClass().getClassLoader().getResourceAsStream(imageResource), null);
      Picture picture = new Picture(pictureBytes, "image/jpeg");
      identityService.setUserPicture(userId, picture);
    }
    
    identityService.createMembership(userId, "management");
    identityService.createMembership(userId, "sales");
    identityService.createMembership(userId, "marketing");
    identityService.createMembership(userId, "engineering");
    identityService.createMembership(userId, "user");
    identityService.createMembership(userId, "admin");
  }
  
  protected void initProcessDefinitions(ProcessEngine processEngine) {
   processEngine.getRepositoryService()
     .createDeployment()
     .name("Demo processes")
     .addClasspathResource("org/activiti/explorer/demo/process/testProcess.bpmn20.xml")
     .addClasspathResource("org/activiti/explorer/demo/process/oneTaskProcess.bpmn20.xml")
     .addClasspathResource("org/activiti/explorer/demo/process/createTimersProcess.bpmn20.xml")
     .deploy();
  }


}
