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
package org.activiti.engine.impl.webservice;

import org.activiti.engine.test.Deployment;

/**
 * An integration test for CXF based web services
 *
 * @author Christophe DENEUX
 */
public class WebServiceImportTest extends AbstractWebServiceTaskTest {

    @Deployment
    public void testImport() throws Exception {
      
      processEngine.getRuntimeService().startProcessInstanceByKey("webServiceInvocationImport");
      waitForJobExecutorToProcessAllJobs(10000L, 250L);

    }

    @Deployment
    public void testImport_DifferentDirectories() throws Exception {

        processEngine.getRuntimeService().startProcessInstanceByKey("webServiceInvocationImport_DifferentDirectories");
        waitForJobExecutorToProcessAllJobs(10000L, 250L);

    }

}
