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
package org.activiti5.engine.impl.webservice;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

/**
 * An integration test for CXF based web services
 *
 * @author <a href="mailto:gnodet@gmail.com">Guillaume Nodet</a>
 */
public class WebServiceTaskTest extends PluggableActivitiTestCase {

    private WebServiceMock webServiceMock;
    private Server server;

    @Override
    protected void initializeProcessEngine() {
        super.initializeProcessEngine();

        webServiceMock = new WebServiceMockImpl();
        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setServiceClass(WebServiceMock.class);
        svrFactory.setAddress("http://localhost:63081/webservicemock");
        svrFactory.setServiceBean(webServiceMock);
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

        assertEquals(-1, webServiceMock.getCount());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("webServiceInvocation");
        waitForJobExecutorToProcessAllJobs(10000L, 250L);

        assertEquals(0, webServiceMock.getCount());
        assertTrue(processInstance.isEnded());
    }

    @Deployment
    public void testWebServiceInvocationDataStructure() throws Exception {

        final Calendar calendar = Calendar.getInstance();
        calendar.set(2015, Calendar.APRIL, 23, 0, 0, 0);
        final Date expectedDate = calendar.getTime();
        final Map<String, Object> variables = new HashMap<String, Object>(1);
        variables.put("startDate", expectedDate);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("webServiceInvocationDataStructure", variables);
        waitForJobExecutorToProcessAllJobs(10000L, 250L);

        assertEquals(expectedDate, webServiceMock.getDataStructure().eltDate);
        assertTrue(processInstance.isEnded());
    }

}
