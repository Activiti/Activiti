package org.activiti.spring.impl.test;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Removes all deployments at the end of a complete test class.
 * <p>
 * Use this as follows in a Spring test:
 *
 * @author jbarrez
 * @RunWith(SpringJUnit4ClassRunner.class)
 * @TestExecutionListeners(CleanTestExecutionListener.class)
 * @ContextConfiguration("...")
 */
public class CleanTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        RepositoryService repositoryService = testContext.getApplicationContext().getBean(RepositoryService.class);
        for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

}
