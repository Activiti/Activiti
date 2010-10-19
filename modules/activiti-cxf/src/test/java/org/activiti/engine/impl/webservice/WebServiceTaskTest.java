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

import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.test.Deployment;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

/**
 * An integration test for CXF based web services
 *
 * @author <a href="mailto:gnodet@gmail.com">Guillaume Nodet</a>
 */
public class WebServiceTaskTest extends ActivitiInternalTestCase {

    private Counter counter;
    private Server server;

    @Override
    protected void initializeProcessEngine() {
        super.initializeProcessEngine();

        counter = new CounterImpl();
        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setServiceClass(Counter.class);
        svrFactory.setAddress("http://localhost:63081/counter");
        svrFactory.setServiceBean(counter);
        svrFactory.getInInterceptors().add(new LoggingInInterceptor());
        svrFactory.getOutInterceptors().add(new LoggingOutInterceptor());
        server = svrFactory.create();
        server.start();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        server.stop();
    }

    @Deployment
    public void testWebServiceInvocation() throws Exception {

        assertEquals(-1, counter.getCount());

        runtimeService.startProcessInstanceByKey("webServiceInvocation");
        waitForJobExecutorToProcessAllJobs(10000L, 250L);

        assertEquals(0, counter.getCount());
    }

}
