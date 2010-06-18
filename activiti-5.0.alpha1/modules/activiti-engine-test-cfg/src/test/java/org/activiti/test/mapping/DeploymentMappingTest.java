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
package org.activiti.test.mapping;

import junit.framework.TestCase;

import org.activiti.impl.repository.DeploymentImpl;

/**
 * @author Joram Barrez
 */
public class DeploymentMappingTest extends TestCase {
	
//	private EntityManagerFactory emf;
	
//	private JpaStandaloneTemplate template;
	
	@Override
	protected void setUp() throws Exception {
//	  this.emf = Persistence.createEntityManagerFactory("activitiPU");
//	  this.template = new JpaStandaloneTemplate(emf);
	}
	
	@Override
	protected void tearDown() throws Exception {
//	  emf.close();
	}
	
	public void testDeploymentImplMapping() {
		
		// First transaction: persist a deployment
//		final long deploymentId = (Long) template.execute(new JpaStandaloneCallback() {
//			
//			public Object executeInJpaTransaction(EntityManager entityManager) {
//				DeploymentImpl deployment = new DeploymentImpl();
//				deployment.setName("myDeployment");
//				deployment.addResourceFromString("testResource", "theTestResource");
//				entityManager.persist(deployment);
//				assertTrue(deployment.getDbid() != 0);
//				return deployment.getDbid();
//			}
//		});
//		
//		// Second transaction: load the deployment and verify values
//		template.execute(new JpaStandaloneCallback() {
//			
//			public Object executeInJpaTransaction(EntityManager entityManager) {
//				DeploymentImpl deployment = entityManager.find(DeploymentImpl.class, deploymentId);
//				assertEquals("myDeployment", deployment.getName());
//				assertEquals("theTestResource", new String(deployment.getResource("testResource").getBytes()));
//				return null;
//			}
//		});
		
	}

}
