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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import javax.xml.namespace.QName;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A CXF's synchronous web service client
 * 
 * @author Esteban Robles Luna
 */
public class CxfWebServiceClient implements SyncWebServiceClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(CxfWebServiceClient.class);
    
  protected Client client;
  
  public CxfWebServiceClient(String wsdl) {
    JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Enumeration<URL> xjcBindingUrls;
        try {
            xjcBindingUrls = Thread.currentThread().getContextClassLoader()
                    .getResources(CxfWSDLImporter.JAXB_BINDINGS_RESOURCE);
            if (xjcBindingUrls.hasMoreElements()) {
                final URL xjcBindingUrl = xjcBindingUrls.nextElement();
                if (xjcBindingUrls.hasMoreElements()) {
                    throw new ActivitiException("Several JAXB binding definitions found for activiti-cxf: "
                            + CxfWSDLImporter.JAXB_BINDINGS_RESOURCE);
                }
                this.client = dcf.createClient(wsdl, Arrays.asList(new String[] { xjcBindingUrl.toString() }));
                this.client.getRequestContext().put("org.apache.cxf.stax.force-start-document", Boolean.TRUE);
            } else {
                throw new ActivitiException("The JAXB binding definitions are not found for activiti-cxf: "
                        + CxfWSDLImporter.JAXB_BINDINGS_RESOURCE);
            }
        } catch (IOException e) {
            throw new ActivitiException("An error occurs creating a web-service client for WSDL '" + wsdl + "'.", e);
        }
  }
  
  /**
   * {@inheritDoc}}
   */
  public Object[] send(String methodName, Object[] arguments, java.util.concurrent.ConcurrentMap<QName,URL> overridenEndpointAddresses) throws Exception {
    try {
	    URL newEndpointAddress = null;
	    if (overridenEndpointAddresses != null) {
  	    newEndpointAddress = overridenEndpointAddresses
               .get(this.client.getEndpoint().getEndpointInfo().getName());
	    }
	    
    	if (newEndpointAddress != null) {
       		this.client.getRequestContext().put(Message.ENDPOINT_ADDRESS, newEndpointAddress.toExternalForm());
    	}
    	return client.invoke(methodName, arguments);
    } catch (Fault e) {
       LOGGER.debug("Technical error calling WS", e);
       throw new ActivitiException(e.getMessage(), e);
    } catch (Exception e) {
       // Other exceptions should be associated to business fault defined in the service WSDL
       LOGGER.debug("Business error calling WS", e);
       throw new BpmnError(e.getClass().getName(), e.getMessage());
    }
  }
}
