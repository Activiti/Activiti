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

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.interceptor.Fault;

/**
 * An integration test for CXF based web services
 *
 * @author <a href="mailto:gnodet@gmail.com">Guillaume Nodet</a>
 */
public class WebServiceTaskTest extends AbstractWebServiceTaskTest {

    @Deployment
    public void testWebServiceInvocation() throws Exception {

        assertEquals(-1, webServiceMock.getCount());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("webServiceInvocation");
        waitForJobExecutorToProcessAllJobs(10000L, 250L);

        assertEquals(0, webServiceMock.getCount());
        assertTrue(processInstance.isEnded());
    }

    @Deployment
    public void testWebServiceInvocationWithEndpointAddressConfigured() throws Exception {

        assertEquals(-1, webServiceMock.getCount());

        processEngineConfiguration.addWsEndpointAddress(
                new QName("http://webservice.impl.engine.activiti.org/", "CounterImplPort"),
                new URL(WEBSERVICE_MOCK_ADDRESS));

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
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("webServiceInvocationDataStructure",
                variables);
        waitForJobExecutorToProcessAllJobs(10000L, 250L);

        assertEquals(expectedDate, webServiceMock.getDataStructure().eltDate);
        assertTrue(processInstance.isEnded());
    }

    @Deployment
    public void testFaultManagement() throws Exception {

        assertEquals(-1, webServiceMock.getCount());

        // Expected fault catched with a boundary error event

        webServiceMock.setTo(Integer.MAX_VALUE);
        ProcessInstance processInstanceWithExpectedFault = runtimeService
                .startProcessInstanceByKey("webServiceInvocation");
        waitForJobExecutorToProcessAllJobs(10000L, 250L);
        assertTrue(processInstanceWithExpectedFault.isEnded());
        final List<HistoricProcessInstance> historicProcessInstanceWithExpectedFault = historyService
                .createHistoricProcessInstanceQuery().processInstanceId(processInstanceWithExpectedFault.getId())
                .list();
        assertEquals(1, historicProcessInstanceWithExpectedFault.size());
        assertEquals("theEndWithError", historicProcessInstanceWithExpectedFault.get(0).getEndActivityId());

        // Runtime exception occurring during processing of the web-service, so not catched in the process definition
        webServiceMock.setTo(123456);
        try {
            runtimeService.startProcessInstanceByKey("webServiceInvocation");
        } catch (ActivitiException e) {
            assertTrue(e.getCause() instanceof SoapFault);
        }

        // Unexpected fault invoking the web-service, so not catched in the process definition
        server.stop();
        try {
            runtimeService.startProcessInstanceByKey("webServiceInvocation");
        } catch (ActivitiException e) {
            assertTrue(e.getCause() instanceof Fault);
        } finally {
            server.start();
        }
    }

}
