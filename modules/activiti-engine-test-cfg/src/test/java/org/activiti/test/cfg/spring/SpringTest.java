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
package org.activiti.test.cfg.spring;

import org.activiti.impl.util.LogUtil;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Tom Baeyens
 */
public class SpringTest {

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }

  @Test
  public void testSpringApi() {

    ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("org/activiti/test/cfg/spring/applicationContext.xml");
    UserBean userBean = (UserBean) applicationContext.getBean("userBean");
    userBean.doTransactional();
    applicationContext.close();

  }

  // Temp test to try out some Spring stuff
  @Test
  public void testSaveDeployment() {
    // ApplicationContext applicationContext = new
    // ClassPathXmlApplicationContext("org/activiti/test/cfg/spring/applicationContext.xml");
    // PlatformTransactionManager ptm = (PlatformTransactionManager)
    // applicationContext.getBean("transactionManager");
    // final EntityManagerFactory emf = (EntityManagerFactory)
    // applicationContext.getBean("entityManagerFactory");
    // TransactionTemplate transactionTemplate = new TransactionTemplate(ptm);
    //		
    // // See logging: transaction is committed: '[DEBUG]: Committing JPA
    // transaction on EntityManager ...'
    // final Long dbId = (Long) transactionTemplate.execute(new
    // TransactionCallback() {
    //			
    // public Object doInTransaction(TransactionStatus transactionStatus) {
    //					
    // // IMPORTANT!!!
    // // cannot use EntityManager em = emf.createEntityManager();
    // //
    // // The JPATransactionManager will create a new entityManager and bind it
    // to the thread
    // // When you create an entityManager yourself, it will NOT participate in
    // the
    // // current transaction and the test will fail because the database will
    // remain empty.
    //					
    // EntityManager em =
    // EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
    //					
    // DeploymentImpl deployment = new DeploymentImpl();
    // deployment.setName("TEST_DEPLOYMENT");
    // assertEquals(0L, deployment.getDbid());
    //					
    // em.persist(deployment);
    // assertTrue(deployment.getDbid() != 0L);
    //					
    // return deployment.getDbid();
    // }
    // });
    //		
    // transactionTemplate.execute(new TransactionCallback() {
    //
    // public Object doInTransaction(TransactionStatus transactionStatus) {
    //
    // EntityManager em =
    // EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
    // DeploymentImpl deploymentFromDb = em.find(DeploymentImpl.class, dbId);
    //
    // assertEquals("TEST_DEPLOYMENT", deploymentFromDb.getName());
    //
    // return null;
    // }
    // });
    //
    // }
  }

  // TODO
  // @Test public void testActivitiApi() {
  // SpringProcessManagerFactory springProcessSessionFactory =
  // (SpringProcessManagerFactory) ProcessConfiguration.create()
  // .configureResource("org/activiti/test/cfg/spring/activiti.properties")
  // .buildProcessSessionFactory();
  //
  // AbstractApplicationContext applicationContext =
  // (AbstractApplicationContext)
  // springProcessSessionFactory.getApplicationContext();
  //    
  // UserBean userBean = (UserBean) applicationContext.getBean("userBean");
  //    
  // userBean.doTransactional();
  //
  // applicationContext.close();
  // }
}
