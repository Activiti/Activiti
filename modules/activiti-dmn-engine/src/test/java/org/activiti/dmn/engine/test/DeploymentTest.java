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
package org.activiti.dmn.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.activiti.dmn.api.DmnDecisionTable;
import org.activiti.dmn.api.DmnDeployment;
import org.junit.Test;

public class DeploymentTest extends AbstractActivitiDmnTest {

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/multiple_conclusions.dmn")
    public void deploySingleDecision() {
        DmnDecisionTable decision = repositoryService.createDecisionTableQuery()
                .latestVersion()
                .decisionTableKey("decision")
                .singleResult();
        assertNotNull(decision);
        assertEquals("decision", decision.getKey());
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/multiple_conclusions.dmn")
    public void deploySingleDecisionAndValidateCache() {
        DmnDecisionTable decision = repositoryService.createDecisionTableQuery()
                .latestVersion()
                .decisionTableKey("decision")
                .singleResult();
        assertNotNull(decision);
        assertEquals("decision", decision.getKey());

        assertTrue(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision.getId()));
        dmnEngineConfiguration.getDeploymentManager().getDecisionCache().clear();
        assertFalse(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision.getId()));

        decision = repositoryService.getDecisionTable(decision.getId());
        assertNotNull(decision);
        assertEquals("decision", decision.getKey());
    }

    @Test
    @DmnDeploymentAnnotation(resources = "org/activiti/dmn/engine/test/deployment/multiple_conclusions.dmn")
    public void deploySingleDecisionAndValidateVersioning() {
        DmnDecisionTable decision = repositoryService.createDecisionTableQuery()
                .latestVersion()
                .decisionTableKey("decision")
                .singleResult();

        assertEquals(1, decision.getVersion());

        repositoryService.createDeployment().name("secondDeployment")
                .addClasspathResource("org/activiti/dmn/engine/test/deployment/multiple_conclusions.dmn")
                .deploy();

        decision = repositoryService.createDecisionTableQuery()
                .latestVersion()
                .decisionTableKey("decision")
                .singleResult();

        assertEquals(2, decision.getVersion());
    }

    @Test
    public void deploySingleDecisionInTenantAndValidateCache() throws Exception {
        repositoryService.createDeployment().name("secondDeployment")
                .addClasspathResource("org/activiti/dmn/engine/test/deployment/multiple_conclusions.dmn")
                .tenantId("testTenant")
                .deploy();

        DmnDecisionTable decision = repositoryService.createDecisionTableQuery()
                .latestVersion()
                .decisionTableKey("decision")
                .decisionTableTenantId("testTenant")
                .singleResult();
        assertNotNull(decision);
        assertEquals("decision", decision.getKey());
        assertEquals("testTenant", decision.getTenantId());
        assertEquals(1, decision.getVersion());

        assertTrue(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision.getId()));
        dmnEngineConfiguration.getDeploymentManager().getDecisionCache().clear();
        assertFalse(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision.getId()));

        decision = repositoryService.getDecisionTable(decision.getId());
        assertNotNull(decision);
        assertEquals("decision", decision.getKey());

        deleteDeployments();
    }

    @Test
    public void deploySingleDecisionInTenantAndValidateVersioning() throws Exception {
        repositoryService.createDeployment().name("secondDeployment")
                .addClasspathResource("org/activiti/dmn/engine/test/deployment/multiple_conclusions.dmn")
                .tenantId("testTenant")
                .deploy();

        DmnDecisionTable decision = repositoryService.createDecisionTableQuery()
                .latestVersion()
                .decisionTableKey("decision")
                .decisionTableTenantId("testTenant")
                .singleResult();

        assertEquals(1, decision.getVersion());

        repositoryService.createDeployment().name("secondDeployment")
                .addClasspathResource("org/activiti/dmn/engine/test/deployment/multiple_conclusions.dmn")
                .tenantId("testTenant")
                .deploy();

        decision = repositoryService.createDecisionTableQuery()
                .latestVersion()
                .decisionTableKey("decision")
                .decisionTableTenantId("testTenant")
                .singleResult();

        assertEquals(2, decision.getVersion());

        deleteDeployments();
    }

    @Test
    public void numberTest1() {
        BigDecimal bigDecimal1 = new BigDecimal("3");
        BigDecimal bigDecimal2 = bigDecimal1.divide(new BigDecimal("2")).setScale(0, BigDecimal.ROUND_HALF_UP);
        System.out.println(bigDecimal2);

        BigDecimal bigDecimal3 = new BigDecimal("3");
        BigDecimal bigDecimal4 = new BigDecimal("1.5");
    }

    @Test
    @DmnDeploymentAnnotation(resources="org/activiti/dmn/engine/test/deployment/multiple_dmndecisions.dmn")
    public void deployMultipleDmnDecisions() throws Exception{

        DmnDecisionTable  decision = null;

        decision = repositoryService.createDecisionTableQuery().latestVersion().decisionTableKey("decision").singleResult();
        assertNotNull(decision);
        assertEquals("decision", decision.getKey());

        assertTrue(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision.getId()));
        dmnEngineConfiguration.getDeploymentManager().getDecisionCache().clear();
        assertFalse(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision.getId()));

        decision = repositoryService.getDecisionTable(decision.getId());
        assertNotNull(decision);
        assertEquals("decision", decision.getKey());

        DmnDecisionTable decision2 = repositoryService.createDecisionTableQuery().latestVersion().decisionTableKey("decision2").singleResult();
        assertNotNull(decision2);
        assertEquals("decision2", decision2.getKey());

        assertTrue(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision2.getId()));
        dmnEngineConfiguration.getDeploymentManager().getDecisionCache().clear();
        assertFalse(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision2.getId()));

        decision2 = repositoryService.getDecisionTable(decision2.getId());
        assertNotNull(decision2);
        assertEquals("decision2", decision2.getKey());

        DmnDecisionTable decision3 = repositoryService.createDecisionTableQuery().latestVersion().decisionTableKey("decision3").singleResult();
        assertNotNull(decision3);
        assertEquals("decision3", decision3.getKey());

        assertTrue(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision3.getId()));
        dmnEngineConfiguration.getDeploymentManager().getDecisionCache().clear();
        assertFalse(dmnEngineConfiguration.getDeploymentManager().getDecisionCache().contains(decision3.getId()));

        decision3 = repositoryService.getDecisionTable(decision3.getId());
        assertNotNull(decision3);
        assertEquals("decision3", decision3.getKey());

    }


    protected void deleteDeployments() {
        List<DmnDeployment> deployments = repositoryService.createDeploymentQuery().list();
        for (DmnDeployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId());
        }
    }
}
